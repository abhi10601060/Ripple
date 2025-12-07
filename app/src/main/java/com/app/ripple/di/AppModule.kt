package com.app.ripple.di

import android.content.Context
import com.app.ripple.data.audio_recording.AudioRecordingManager
import com.app.ripple.data.local.contract.NearbyDevicePersistenceRepo
import com.app.ripple.data.local.realm.NearbyDeviceRealmRepo
import com.app.ripple.data.local.realm.model.NearbyDeviceRealm
import com.app.ripple.data.local.realm.model.TextMessageRealm
import com.app.ripple.data.nearby.NearbyShareManager
import com.app.ripple.data.repo.NearbyShareRepoImpl
import com.app.ripple.domain.repo.NearbyShareRepo
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
    fun providesNearbyShareManager(@ApplicationContext context: Context, nearbyDevicePersistenceRepo: NearbyDevicePersistenceRepo) : NearbyShareManager{
        return NearbyShareManager.getInstance(context, nearbyDevicePersistenceRepo)
    }

    @Provides
    @Singleton
    fun getRepo(nearbyShareManager: NearbyShareManager) : NearbyShareRepo{
        return NearbyShareRepoImpl(nearbyShareManager)
    }

    @Provides
    @Singleton
    fun providesAudioManager(@ApplicationContext context: Context): AudioRecordingManager {
        return AudioRecordingManager.getInstance(context)
    }
}

class Test(val name : String){}