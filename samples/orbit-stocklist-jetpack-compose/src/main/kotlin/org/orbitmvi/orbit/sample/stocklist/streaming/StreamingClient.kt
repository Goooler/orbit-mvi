/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.stocklist.streaming

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import kotlin.concurrent.thread

class StreamingClient : DefaultLifecycleObserver {
    private var connectionWish = false

    private val lsClient = LightstreamerClient(
        "https://push.lightstreamer.com",
        "DEMO"
    ).apply {
        connect()
    }

    override fun onStart(owner: LifecycleOwner) {
        synchronized(lsClient) {
            connectionWish = true
            lsClient.connect()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        synchronized(lsClient) {
            connectionWish = false

            thread {
                @Suppress("MagicNumber")
                Thread.sleep(5000)
                synchronized(lsClient) {
                    if (!connectionWish) {
                        lsClient.disconnect()
                    }
                }
            }
        }
    }

    fun addSubscription(sub: Subscription) {
        lsClient.subscribe(sub)
    }

    fun removeSubscription(sub: Subscription) {
        lsClient.unsubscribe(sub)
    }
}
