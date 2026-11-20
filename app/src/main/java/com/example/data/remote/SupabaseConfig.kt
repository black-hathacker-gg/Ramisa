package com.example.data.remote

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(SupabaseInternal::class)
object SupabaseConfig {
    val clientOrNull: SupabaseClient? by lazy {
        val endpoint = ClientEndpoint.parse(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
        endpoint?.let {
            createSupabaseClient(supabaseUrl = it.url, supabaseKey = it.key) {
                httpEngine = Android.create()
                requestTimeout = 15.seconds
                httpConfig { followRedirects = false }
                install(Postgrest)
                // Email confirmations land in the browser. Keep the session exchange tied
                // to the Android client while the user returns to sign in.
                install(Auth) { flowType = FlowType.PKCE }
            }
        }
    }

    val client: SupabaseClient
        get() = clientOrNull ?: throw CloudAccountUnavailable()
}

internal class ClientEndpoint private constructor(val url: String, val key: String) {
    companion object {
        fun parse(url: String, key: String): ClientEndpoint? {
            val origin = runCatching { URI(url.trim()) }.getOrNull() ?: return null
            val host = origin.host ?: return null
            if (!origin.scheme.equals("https", ignoreCase = true) ||
                origin.userInfo != null || origin.query != null || origin.fragment != null ||
                origin.path !in listOf("", "/") || origin.port == 0 || origin.port > 65535 ||
                host.equals("your-project.supabase.co", ignoreCase = true)
            ) return null

            val clientKey = key.trim()
            if (!isPublicClientKey(clientKey)) return null
            return ClientEndpoint(origin.toASCIIString().trimEnd('/'), clientKey)
        }

        private fun isPublicClientKey(key: String): Boolean {
            if (key.matches(Regex("sb_publishable_[A-Za-z0-9_-]{12,}"))) return true
            val parts = key.split('.')
            if (parts.size != 3 || parts.any { it.isBlank() }) return false
            return runCatching {
                val claims = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                    .decode(parts[1]).decodeToString()
                Json.parseToJsonElement(claims).jsonObject["role"]?.jsonPrimitive?.content == "anon"
            }.getOrDefault(false)
        }
    }
}

internal class CloudAccountUnavailable : IllegalStateException(
    "Online accounts are unavailable in this build. You can continue as a guest."
)
