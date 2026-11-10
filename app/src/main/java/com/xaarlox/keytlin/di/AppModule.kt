package com.xaarlox.keytlin.di

import android.content.Context
import androidx.room.Room
import com.xaarlox.keytlin.data.DefaultVaultRepository
import com.xaarlox.keytlin.data.VaultRepository
import com.xaarlox.keytlin.data.local.RoomSyncStateLocalDataSource
import com.xaarlox.keytlin.data.local.RoomVaultLocalDataSource
import com.xaarlox.keytlin.data.local.SyncStateLocalDataSource
import com.xaarlox.keytlin.data.local.VaultLocalDataSource
import com.xaarlox.keytlin.data.local.db.KeytlinDatabase
import com.xaarlox.keytlin.data.local.db.SyncStateDao
import com.xaarlox.keytlin.data.local.db.VaultDao
import com.xaarlox.keytlin.data.remote.FakeSyncRemoteDataSource
import com.xaarlox.keytlin.data.remote.SyncRemoteDataSource
import com.xaarlox.keytlin.data.security.Argon2idKeyDerivation
import com.xaarlox.keytlin.data.security.KeystoreTokenCipher
import com.xaarlox.keytlin.data.security.TokenCipher
import com.xaarlox.keytlin.domain.crypto.AesGcmCipher
import com.xaarlox.keytlin.domain.crypto.KeyDerivationFunction
import com.xaarlox.keytlin.domain.crypto.VaultBlobCodec
import com.xaarlox.keytlin.domain.crypto.VaultCrypto
import com.xaarlox.keytlin.domain.service.PasswordGenerator
import com.xaarlox.keytlin.domain.service.PasswordStrengthEvaluator
import com.xaarlox.keytlin.domain.service.VaultMerger
import com.xaarlox.keytlin.domain.service.VaultSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/** Specifies the dispatcher for I/O operations (disk, network) */
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

/** Domain layer services. @Singleton — single instance for the entire application */
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

    @Provides
    @Singleton
    fun provideVaultMerger(): VaultMerger = VaultMerger()
}

/** Cryptography: Argon2id for key derivation, AES-256-GCM for encryption */
@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideKeyDerivationFunction(): KeyDerivationFunction = Argon2idKeyDerivation()

    @Provides
    @Singleton
    fun provideAesGcmCipher(): AesGcmCipher = AesGcmCipher()

    @Provides
    @Singleton
    fun provideVaultBlobCodec(): VaultBlobCodec = VaultBlobCodec()

    @Provides
    @Singleton
    fun provideVaultCrypto(
        kdf: KeyDerivationFunction,
        cipher: AesGcmCipher,
        serializer: VaultSerializer,
        codec: VaultBlobCodec
    ): VaultCrypto = VaultCrypto(kdf, cipher, serializer, codec)

    @Provides
    @Singleton
    fun provideTokenCipher(): TokenCipher = KeystoreTokenCipher()
}

/** Local Room database */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KeytlinDatabase =
        Room.databaseBuilder(context, KeytlinDatabase::class.java, KeytlinDatabase.NAME).build()

    @Provides
    fun provideVaultDao(database: KeytlinDatabase): VaultDao = database.vaultDao()

    @Provides
    fun provideSyncStateDao(database: KeytlinDatabase): SyncStateDao = database.syncStateDao()
}

/** Data layer: data sources and repository */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideVaultLocalDataSource(dao: VaultDao): VaultLocalDataSource =
        RoomVaultLocalDataSource(dao)

    @Provides
    @Singleton
    fun provideSyncStateLocalDataSource(
        dao: SyncStateDao,
        tokenCipher: TokenCipher
    ): SyncStateLocalDataSource = RoomSyncStateLocalDataSource(dao, tokenCipher)

    @Provides
    @Singleton
    fun provideSyncRemoteDataSource(
        crypto: VaultCrypto,
        codec: VaultBlobCodec
    ): SyncRemoteDataSource = FakeSyncRemoteDataSource(crypto, codec)

    @Provides
    @Singleton
    fun provideVaultRepository(
        localDataSource: VaultLocalDataSource,
        syncStateDataSource: SyncStateLocalDataSource,
        remoteDataSource: SyncRemoteDataSource,
        crypto: VaultCrypto,
        codec: VaultBlobCodec,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): VaultRepository = DefaultVaultRepository(
        localDataSource = localDataSource,
        syncStateDataSource = syncStateDataSource,
        remoteDataSource = remoteDataSource,
        crypto = crypto,
        codec = codec,
        ioDispatcher = ioDispatcher
    )
}