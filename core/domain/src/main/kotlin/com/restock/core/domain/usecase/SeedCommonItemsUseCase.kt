package com.restock.core.domain.usecase

import com.restock.core.domain.repository.ItemRepository
import javax.inject.Inject

/**
 * Use case for seeding the database with common grocery items.
 *
 * This populates the item catalog with frequently used items,
 * enabling autocomplete suggestions even before users have
 * added their own items.
 *
 * Should be run once on first app launch (controlled by preferences).
 */
class SeedCommonItemsUseCase
    @Inject
    constructor(
        private val itemRepository: ItemRepository,
    ) {
        /**
         * Seeds the database with common grocery items.
         *
         * Uses getOrCreate to avoid duplicates if called multiple times.
         */
        suspend operator fun invoke() {
            COMMON_ITEMS.forEach { name ->
                itemRepository.getOrCreate(name)
            }
        }

        companion object {
            /**
             * List of common grocery items to seed.
             *
             * Curated for broad appeal across different shopping habits.
             * Names are capitalized as they would appear in the UI.
             */
            val COMMON_ITEMS =
                listOf(
                    // Dairy & Eggs
                    "Milk",
                    "Eggs",
                    "Butter",
                    "Cheese",
                    "Yogurt",
                    // Proteins
                    "Chicken",
                    "Beef",
                    "Pork",
                    "Fish",
                    // Grains & Bread
                    "Bread",
                    "Rice",
                    "Pasta",
                    "Cereal",
                    "Oatmeal",
                    // Produce - Vegetables
                    "Tomatoes",
                    "Onions",
                    "Potatoes",
                    "Carrots",
                    "Lettuce",
                    "Cucumber",
                    "Bell peppers",
                    "Mushrooms",
                    "Spinach",
                    "Broccoli",
                    "Garlic",
                    // Produce - Fruits
                    "Apples",
                    "Bananas",
                    "Oranges",
                    "Lemons",
                    "Avocados",
                    "Berries",
                    // Beverages
                    "Coffee",
                    "Tea",
                    "Orange juice",
                    // Pantry Staples
                    "Sugar",
                    "Salt",
                    "Pepper",
                    "Olive oil",
                    "Vegetable oil",
                    "Flour",
                    "Honey",
                    // Snacks
                    "Chips",
                    "Crackers",
                    "Cookies",
                    "Nuts",
                    // Canned & Jarred
                    "Canned tomatoes",
                    "Beans",
                    "Peanut butter",
                    // Frozen
                    "Ice cream",
                    "Frozen vegetables",
                    // Household (commonly bought at grocery stores)
                    "Paper towels",
                    "Toilet paper",
                )
        }
    }
