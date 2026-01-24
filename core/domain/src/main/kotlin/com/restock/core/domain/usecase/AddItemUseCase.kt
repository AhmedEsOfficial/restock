package com.restock.core.domain.usecase

import com.restock.core.domain.model.CurrentListItem
import com.restock.core.domain.repository.CurrentListRepository
import com.restock.core.domain.repository.ItemRepository
import javax.inject.Inject

/**
 * Use case for adding an item to the current shopping list.
 *
 * Handles the business logic of:
 * - Validating item name is not blank
 * - Getting or creating the item in the catalog (deduplication by normalized name)
 * - Checking if the item is already on the list (unchecked)
 * - If already on list: incrementing quantity instead of adding duplicate
 * - If new to list: adding with specified quantity and unit
 */
class AddItemUseCase
    @Inject
    constructor(
        private val itemRepository: ItemRepository,
        private val currentListRepository: CurrentListRepository,
    ) {
        /**
         * Adds an item to the current shopping list.
         *
         * @param name The name of the item to add.
         * @param quantity The quantity to add (default 1).
         * @param unit Optional unit of measurement (e.g., "lb", "oz", "kg").
         * @return Result containing the CurrentListItem (new or updated) or an error.
         */
        suspend operator fun invoke(
            name: String,
            quantity: Int = 1,
            unit: String? = null,
        ): Result<CurrentListItem> {
            // Validate input
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Item name cannot be empty"))
            }

            if (quantity < 1) {
                return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
            }

            // Get or create the item in the catalog (handles deduplication)
            val item = itemRepository.getOrCreate(name)

            // Check if already on list (unchecked items only)
            val existingOnList = currentListRepository.findByItemId(item.id)
            if (existingOnList != null) {
                // Update quantity instead of adding duplicate
                val newQuantity = existingOnList.quantity + quantity
                val effectiveUnit = unit ?: existingOnList.unit

                return currentListRepository.updateQuantity(
                    id = existingOnList.id,
                    quantity = newQuantity,
                    unit = effectiveUnit,
                ).map {
                    existingOnList.copy(
                        quantity = newQuantity,
                        unit = effectiveUnit,
                    )
                }
            }

            // Add new item to list
            return currentListRepository.addItem(
                itemId = item.id,
                quantity = quantity,
                unit = unit,
            ).map { listItem ->
                // Ensure the name is set from the catalog item
                listItem.copy(name = item.name, normalizedName = item.normalizedName)
            }
        }
    }
