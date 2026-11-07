package com.polyglot.academy.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun defaultHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    engine {
        config {
            retryOnConnectionFailure(false)
            followRedirects(false)
        }
    }
}
