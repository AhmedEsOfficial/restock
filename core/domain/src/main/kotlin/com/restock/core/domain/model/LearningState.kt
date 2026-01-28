package com.restock.core.domain.model

/**
 * Learning state for the suggestions feature.
 *
 * Determines what UI to show based on shopping trip history.
 * The state progresses as users complete more shopping trips.
 */
enum class LearningState {
    /**
     * No shopping history yet.
     * Don't show any learning UI - just hide the suggestions section.
     */
    NOT_STARTED,

    /**
     * 1 shopping trip completed.
     * Show: "Learning your patterns..."
     */
    LEARNING,

    /**
     * 2 shopping trips completed.
     * Show: "Almost ready to suggest..."
     */
    ALMOST_READY,

    /**
     * 3+ shopping trips completed.
     * Suggestions can appear (but may be empty if none are due).
     */
    READY,
    ;

    companion object {
        /**
         * Determines the learning state based on shopping trip count.
         *
         * @param tripCount Number of completed shopping trips.
         * @return The appropriate [LearningState] for the trip count.
         */
        fun fromTripCount(tripCount: Int): LearningState =
            when {
                tripCount >= 3 -> READY
                tripCount >= 2 -> ALMOST_READY
                tripCount >= 1 -> LEARNING
                else -> NOT_STARTED
            }

        /**
         * Number of trips required for each state transition.
         */
        const val TRIPS_FOR_LEARNING = 1
        const val TRIPS_FOR_ALMOST_READY = 2
        const val TRIPS_FOR_READY = 3
    }
}
