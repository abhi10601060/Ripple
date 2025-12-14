package com.app.ripple.di

import android.content.Context
import com.app.ripple.background.ComprehensiveCleanupManager
import com.app.ripple.data.audio_recording.AudioRecordingManager
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.contract.TextMessagePersistenceRepo
import com.app.ripple.data.local.realm.NearbyDeviceRealmRepo
import com.app.ripple.data.local.realm.TextMessageRealmRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.NearbyShareManager
import com.app.ripple.data.repo.NearbyDeviceRepoImpl
import com.app.ripple.data.repo.NearbyShareRepoImpl
import com.app.ripple.domain.repo.NearbyDeviceRepo
import com.app.ripple.domain.repo.NearbyShareRepo
import com.app.ripple.presentation.notification.ConnectionRequestNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun getHiltTestData() : Test{
        return Test("From di test...")
    }

    @Provides
    @Singleton
    fun providesRealmDB() : Realm{
        return Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    NearbyDeviceRealm::class,
                    TextMessageRealm::class
                )
            )
        )
    }

    @Provides
    @Singleton
    fun providesNearbyDeviceRealmRepo(realm: Realm) : NearbyDevicePersistenceRepo{
        return NearbyDeviceRealmRepo(realm)
    }

    @Provides
    @Singleton
    fun providesTextMessageRealmRepo(realm: Realm) : TextMessagePersistenceRepo{
        return TextMessageRealmRepo(realm)
    }


    @Singleton
    @Provides
    fun providesConnectionRequestNotificationManager(@ApplicationContext applicationContext: Context) : ConnectionRequestNotificationManager{
        return ConnectionRequestNotificationManager(context = applicationContext)
    }

    @Provides
    @Singleton
    fun providesNearbyShareManager(@ApplicationContext context: Context, nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo, textMessagePersistenceRepo: TextMessagePersistenceRepo, connectionRequestNotificationManager: ConnectionRequestNotificationManager) : NearbyShareManager{
        return NearbyShareManager.getInstance(context, nearbyDevicePersistenceRepo, textMessagePersistenceRepo, connectionRequestNotificationManager)
    }

    @Provides
    @Singleton
    fun getRepo(nearbyShareManager: NearbyShareManager) : NearbyShareRepo{
        return NearbyShareRepoImpl(nearbyShareManager)
    }

    @Provides
    @Singleton
    fun providesNearbyDeviceRepo(nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo): NearbyDeviceRepo{
        return NearbyDeviceRepoImpl(nearbyDevicePersistenceRepo = nearbyDevicePersistenceRepo)
    }

    @Provides
    @Singleton
    fun providesAudioManager(@ApplicationContext context: Context): AudioRecordingManager {
        return AudioRecordingManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providesComprehensiveCleanupManager(nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo, nearbyShareManager: NearbyShareManager) : ComprehensiveCleanupManager{
        return ComprehensiveCleanupManager(nearbyDevicePersistenceRepo, nearbyShareManager)
    }
}

class Test(val name : String){}