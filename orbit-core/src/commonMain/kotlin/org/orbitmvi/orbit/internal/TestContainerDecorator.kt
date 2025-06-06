/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.concurrent.atomics.AtomicReference

public class TestContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    public val originalInitialState: STATE,
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    private val delegate = AtomicReference(actual)

    override val settings: RealSettings
        get() = delegate.load().settings

    override val stateFlow: StateFlow<STATE>
        get() = delegate.load().stateFlow
    override val refCountStateFlow: StateFlow<STATE>
        get() = delegate.load().refCountStateFlow
    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.load().sideEffectFlow
    override val refCountSideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.load().refCountSideEffectFlow

    override fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        return delegate.load().orbit(orbitIntent)
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.load().inlineOrbit(orbitIntent)
    }

    public fun test(
        initialState: STATE? = null,
        settings: RealSettings,
        testScope: CoroutineScope
    ) {
        val testDelegate = RealContainer<STATE, SIDE_EFFECT>(
            initialState = initialState ?: originalInitialState,
            parentScope = testScope,
            settings = settings,
            subscribedCounterOverride = AlwaysSubscribedCounter
        )

        val testDelegateSet = delegate.compareAndSet(
            expectedValue = actual,
            newValue = testDelegate
        )

        if (!testDelegateSet) {
            error("Can only call test() once")
        }
    }

    public override suspend fun joinIntents() {
        delegate.load().joinIntents()
    }

    public override fun cancel() {
        delegate.load().cancel()
    }
}
