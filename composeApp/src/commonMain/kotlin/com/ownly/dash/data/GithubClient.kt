package com.ownly.dash.data

import com.ownly.dash.config.GithubSecrets
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val GITHUB_API_VERSION = "2026-03-10"

internal expect fun createPlatformHttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

/** Shared Ktor client with GitHub auth headers. */
internal fun createGithubHttpClient(): HttpClient = createPlatformHttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 20_000L
        connectTimeoutMillis = 15_000L
        socketTimeoutMillis = 20_000L
    }
    defaultRequest {
        header(HttpHeaders.Accept, "application/vnd.github+json")
        if (GithubSecrets.TOKEN.isNotBlank()) {
            header(HttpHeaders.Authorization, "Bearer ${GithubSecrets.TOKEN}")
        }
        header("X-GitHub-Api-Version", GITHUB_API_VERSION)
    }
}
