package com.flipperstreaks.app.data.remote

import com.flipperstreaks.app.data.local.datastore.AuthTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Attaches user_email/user_token to every outgoing request IF a user has
// opted into connecting their Pinball Map account. If not connected, requests
// go out unauthenticated — which is all read endpoints ever need.
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { tokenStore.currentToken() }

        if (token == null) {
            return chain.proceed(original)
        }

        val newUrl: HttpUrl = original.url.newBuilder()
            .addQueryParameter("user_email", token.email)
            .addQueryParameter("user_token", token.authToken)
            .build()

        val authedRequest = original.newBuilder().url(newUrl).build()
        return chain.proceed(authedRequest)
    }
}
