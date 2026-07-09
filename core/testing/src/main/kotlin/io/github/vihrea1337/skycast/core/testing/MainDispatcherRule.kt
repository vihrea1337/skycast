// Тестовая инфраструктура — легальное место для Dispatchers:
// правило подменяет Dispatchers.Main на тестовый (viewModelScope без него падает в JVM-тестах)
@file:Suppress("ForbiddenImport")

package io.github.vihrea1337.skycast.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * runTest автоматически подхватывает scheduler из Main, если тот установлен
 * через setMain — виртуальное время (advanceTimeBy) работает сквозь ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
