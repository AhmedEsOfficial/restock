package com.restock.feature.meals

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * JUnit 5 extension that replaces the Main dispatcher with a test dispatcher.
 *
 * This is required for testing ViewModels that use `viewModelScope.launch` which
 * dispatches to `Dispatchers.Main` by default. Without this rule, tests will fail
 * with "Module with the Main dispatcher had failed to initialize" error.
 *
 * @param testDispatcher The dispatcher to use for Main. Defaults to [UnconfinedTestDispatcher]
 *        which executes coroutines immediately without dispatching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}
