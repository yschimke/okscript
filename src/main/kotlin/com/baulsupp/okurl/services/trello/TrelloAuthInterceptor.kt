package com.baulsupp.okurl.services.trello

import com.baulsupp.okurl.authenticator.AuthInterceptor
import com.baulsupp.okurl.authenticator.ValidatedCredentials
import com.baulsupp.okurl.completion.ApiCompleter
import com.baulsupp.okurl.completion.BaseUrlCompleter
import com.baulsupp.okurl.completion.CompletionVariableCache
import com.baulsupp.okurl.completion.UrlList
import com.baulsupp.okurl.credentials.CredentialsStore
import com.baulsupp.okurl.credentials.Token
import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.queryList
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.okurl.services.AbstractServiceDefinition
import com.baulsupp.okurl.services.trello.model.BoardResponse
import com.baulsupp.okurl.services.trello.model.MemberResponse
import com.baulsupp.okurl.services.trello.model.TokenResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TrelloAuthInterceptor : AuthInterceptor<TrelloToken>() {

  override fun intercept(chain: Interceptor.Chain, credentials: TrelloToken): Response {
    var request = chain.request()

    val signedUrl = request.url().newBuilder().addQueryParameter("token", credentials.token).addQueryParameter("key", credentials.apiKey).build()

    request = request.newBuilder().url(signedUrl).build()

    return chain.proceed(request)
  }

  override val serviceDefinition = object :
    AbstractServiceDefinition<TrelloToken>("api.trello.com", "Trello", "trello",
      "https://developers.trello.com/", "https://trello.com/app-key") {
    override fun parseCredentialsString(s: String): TrelloToken {
      val (token, key) = s.split(":", limit = 2)
      return TrelloToken(token, key)
    }

    override fun formatCredentialsString(credentials: TrelloToken): String =
      credentials.token + ":" + credentials.apiKey
  }

  override suspend fun validate(
    client: OkHttpClient,
    credentials: TrelloToken
  ): ValidatedCredentials {
    val tokenResponse = client.query<TokenResponse>("https://api.trello.com/1/tokens/${credentials.token}")
    val memberId = tokenResponse.idMember
    val userResponse = client.query<MemberResponse>("https://api.trello.com/1/members/$memberId")

    return ValidatedCredentials(userResponse.username, tokenResponse.identifier)
  }

  override suspend fun authorize(
    client: OkHttpClient,
    outputHandler: OutputHandler<Response>,
    authArguments: List<String>
  ): TrelloToken {
    return TrelloAuthFlow.login(outputHandler)
  }

  override fun apiCompleter(prefix: String, client: OkHttpClient, credentialsStore: CredentialsStore, completionVariableCache: CompletionVariableCache, tokenSet: Token): ApiCompleter {
    val urlList = UrlList.fromResource(name())

    val completer = BaseUrlCompleter(urlList!!, hosts(), completionVariableCache)

    completer.withCachedVariable(name(), "board_id", {
      credentialsStore.get(serviceDefinition, tokenSet)?.let {
        val tokenResponse = client.query<TokenResponse>("https://api.trello.com/1/tokens/${it.token}")

        client.queryList<BoardResponse>("https://api.trello.com/1/members/${tokenResponse.idMember}/boards", tokenSet).map { it.id }
      }
    })

    return completer
  }

  override fun hosts(): Set<String> = setOf("api.trello.com")
}