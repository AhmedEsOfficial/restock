package com.restock.core.domain.usecase

import com.restock.core.domain.model.GroceryItem
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Use case for searching items for autocomplete suggestions.
 *
 * Searches the item catalog for matches and filters out
 * items already on the current list.
 *
 * Supports debounced search for optimal performance during typing.
 */
class SearchItemsUseCase
    @Inject
    constructor(
        private val itemRepository: ItemRepository,
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Searches for items matching the query.
         *
         * @param query The search query (minimum 1 character).
         * @return Flow emitting matching items not already on the list.
         */
        operator fun invoke(query: String): Flow<List<GroceryItem>> =
            flow {
                val result =
                    if (query.isEmpty()) {
                        emptyList()
                    } else {
                        // Search by name prefix for autocomplete
                        val results = itemRepository.searchByNamePrefix(query, limit = MAX_SUGGESTIONS)

                        // Filter out items already on the current list
                        val currentItemIds = currentListRepository.getCurrentItemIds().toSet()
                        results.filter { it.id !in currentItemIds }
                    }
                emit(result)
            }

        /**
         * Creates a debounced search flow from a query flow.
         *
         * Use this for real-time search as the user types.
         *
         * @param queryFlow Flow of search queries from the text field.
         * @param debounceMs Debounce duration in milliseconds (default 200ms).
         * @return Flow of search results.
         */
        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        fun searchWithDebounce(
            queryFlow: Flow<String>,
            debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        ): Flow<List<GroceryItem>> =
            queryFlow
                .debounce(debounceMs)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    invoke(query)
                }

        companion object {
            /** Maximum number of suggestions to show */
            const val MAX_SUGGESTIONS = 5

            /** Default debounce duration for search */
            const val DEFAULT_DEBOUNCE_MS = 200L
        }
    }
