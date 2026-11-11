package com.xaarlox.keytlin.di

import com.xaarlox.keytlin.data.remote.hibp.HibpBreachesApi
import com.xaarlox.keytlin.data.remote.hibp.HibpPasswordBreachChecker
import com.xaarlox.keytlin.data.remote.hibp.HibpSiteBreachChecker
import com.xaarlox.keytlin.data.remote.hibp.PwnedPasswordsApi
import com.xaarlox.keytlin.domain.breach.PasswordBreachChecker
import com.xaarlox.keytlin.domain.breach.SiteBreachChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Network: OkHttp + Retrofit and third-party Have I Been Pwned service clients */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** HIBP rejects requests without a User-Agent header (HTTP 403), so we add it to every request */
    private val userAgentInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()
        )
    }

    private const val USER_AGENT = "Keytlin-Android"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun providePwnedPasswordsApi(client: OkHttpClient): PwnedPasswordsApi = Retrofit.Builder()
        .baseUrl(PwnedPasswordsApi.BASE_URL)
        .client(client)
        .build()
        .create(PwnedPasswordsApi::class.java)

    @Provides
    @Singleton
    fun provideHibpBreachesApi(client: OkHttpClient, json: Json): HibpBreachesApi =
        Retrofit.Builder()
            .baseUrl(HibpBreachesApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HibpBreachesApi::class.java)

    @Provides
    @Singleton
    fun providePasswordBreachChecker(
        api: PwnedPasswordsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PasswordBreachChecker = HibpPasswordBreachChecker(api, ioDispatcher)

    @Provides
    @Singleton
    fun provideSiteBreachChecker(api: HibpBreachesApi): SiteBreachChecker =
        HibpSiteBreachChecker(api)
}