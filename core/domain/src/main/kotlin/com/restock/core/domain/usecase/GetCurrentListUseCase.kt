package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the current shopping list.
 *
 * Returns a Flow that emits the list whenever it changes,
 * enabling reactive UI updates.
 */
class GetCurrentListUseCase
    @Inject
    constructor(
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Observes the current shopping list.
         *
         * @return Flow emitting the current list of items.
         */
        operator fun invoke(): Flow<List<CurrentListItem>> = currentListRepository.observeCurrentList()

        /**
         * Observes only unchecked items (still to get).
         *
         * @return Flow emitting unchecked items sorted by position.
         */
        fun observeUnchecked(): Flow<List<CurrentListItem>> = currentListRepository.observeUncheckedItems()

        /**
         * Observes only checked items (in cart).
         *
         * @return Flow emitting checked items sorted by checkedAt.
         */
        fun observeChecked(): Flow<List<CurrentListItem>> = currentListRepository.observeCheckedItems()
    }
