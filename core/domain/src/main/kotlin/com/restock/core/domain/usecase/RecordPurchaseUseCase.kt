package com.restock.core.domain.usecase

import com.restock.core.domain.di.ApplicationScope
import com.restock.core.domain.model.PurchaseEvent
import com.restock.core.domain.repository.PurchaseRepository
import com.restock.core.domain.repository.SuggestionRepository
import com.restock.core.domain.scheduler.PatternWorkScheduler
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Use case for recording purchase events asynchronously.
 *
 * This use case is triggered when an item is checked off the shopping list.
 * It records the purchase in the background without blocking the calling
 * coroutine, ensuring the UI remains responsive.
 *
 * Key behaviors:
 * - **Fire-and-forget**: Recording happens asynchronously using [ApplicationScope]
 * - **Debounce**: Purchases within 4 hours for the same item aggregate quantities
 * - **Pattern trigger**: On success, triggers pattern recalculation
 * - **Dismissal reset**: On success, clears any dismissal state for the item
 * - **Non-blocking**: Never slows down the main UI thread
 *
 * @property purchaseRepository Repository for persisting purchase events.
 * @property suggestionRepository Repository for managing suggestion dismissals.
 * @property patternWorkScheduler Scheduler for triggering pattern calculations.
 * @property scope Application-scoped coroutine scope that outlives ViewModels.
 */
class RecordPurchaseUseCase
    @Inject
    constructor(
        private val purchaseRepository: PurchaseRepository,
        private val suggestionRepository: SuggestionRepository,
        private val patternWorkScheduler: PatternWorkScheduler,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        /**
         * Records a purchase event asynchronously.
         *
         * This method returns immediately; the actual recording happens
         * in the background. Errors are silently handled to maintain
         * non-blocking behavior (the UI should not be affected by
         * recording failures).
         *
         * @param itemId The ID of the grocery item being purchased.
         * @param quantity The quantity being purchased.
         */
        operator fun invoke(
            itemId: String,
            quantity: Int,
        ) {
            scope.launch {
                val event =
                    PurchaseEvent(
                        itemId = itemId,
                        quantity = quantity,
                        purchasedAt = Instant.now(),
                    )

                purchaseRepository.recordPurchaseEvent(event)
                    .onSuccess {
                        // Clear any dismissal state for this item
                        // (allows dismissed items to be suggested again after purchase)
                        suggestionRepository.clearDismissalOnPurchase(itemId)

                        // Trigger pattern recalculation
                        patternWorkScheduler.triggerImmediateCalculation()
                    }
                // Errors are silently handled - purchase recording
                // should not affect the main user flow
            }
        }

        /**
         * Records a purchase event asynchronously with a custom timestamp.
         *
         * Used primarily for testing or importing historical data.
         *
         * @param itemId The ID of the grocery item being purchased.
         * @param quantity The quantity being purchased.
         * @param purchasedAt The timestamp of the purchase.
         */
        operator fun invoke(
            itemId: String,
            quantity: Int,
            purchasedAt: Instant,
        ) {
            scope.launch {
                val event =
                    PurchaseEvent(
                        itemId = itemId,
                        quantity = quantity,
                        purchasedAt = purchasedAt,
                    )

                purchaseRepository.recordPurchaseEvent(event)
                    .onSuccess {
                        // Clear any dismissal state for this item
                        suggestionRepository.clearDismissalOnPurchase(itemId)

                        patternWorkScheduler.triggerImmediateCalculation()
                    }
                // Errors are silently handled
            }
        }
    }
