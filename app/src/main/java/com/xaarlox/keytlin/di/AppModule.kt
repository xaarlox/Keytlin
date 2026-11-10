package com.xaarlox.keytlin.di

import com.xaarlox.keytlin.data.DefaultVaultRepository
import com.xaarlox.keytlin.data.DemoVaultData
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.data.local.InMemoryVaultLocalDataSource
import com.xaarlox.keytlin.data.local.VaultLocalDataSource
import com.xaarlox.keytlin.data.remote.FakeSyncRemoteDataSource
import com.xaarlox.keytlin.data.remote.SyncRemoteDataSource
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import com.xaarlox.keytlin.domain.service.VaultSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/** Marks the dispatcher for I/O operations (disk, network) */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

/** Domain layer services. @Singleton — one instance per application */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun providePasswordGenerator(): PasswordGenerator = PasswordGenerator()

    @Provides
    @Singleton
    fun providePasswordStrengthEvaluator(): PasswordStrengthEvaluator = PasswordStrengthEvaluator()

    @Provides
    @Singleton
    fun provideVaultSerializer(): VaultSerializer = VaultSerializer()
}

/**
 * Data layer. To switch to a database or a real server,
 * it is enough to change the implementation returned by the corresponding method
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideVaultLocalDataSource(serializer: VaultSerializer): VaultLocalDataSource =
        InMemoryVaultLocalDataSource(
            serializer = serializer,
            initialMasterPassword = DemoVaultData.MASTER_PASSWORD,
            initialVault = DemoVaultData.vault
        )

    @Provides
    @Singleton
    fun provideSyncRemoteDataSource(): SyncRemoteDataSource = FakeSyncRemoteDataSource()

    @Provides
    @Singleton
    fun provideVaultRepository(
        localDataSource: VaultLocalDataSource,
        remoteDataSource: SyncRemoteDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): VaultRepository = DefaultVaultRepository(localDataSource, remoteDataSource, ioDispatcher)
}