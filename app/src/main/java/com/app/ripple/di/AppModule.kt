package com.app.ripple.di

import android.content.Context
import com.app.ripple.data.repo.NearbyShareRepoImpl
import com.app.ripple.domain.repo.NearbyShareRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun getRepo(@ApplicationContext context: Context) : NearbyShareRepo{
        return NearbyShareRepoImpl(context)
    }
}

class Test(val name : String){}