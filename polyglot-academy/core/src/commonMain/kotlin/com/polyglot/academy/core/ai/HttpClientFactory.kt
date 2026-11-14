package com.polyglot.academy.core.ai

import io.ktor.client.HttpClient

/** Platform HTTP client (OkHttp on Android and JVM). TLS uses the platform trust store. */
expect fun defaultHttpClient(): HttpClient
