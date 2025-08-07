package com.app.ripple.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun getHiltTestData() : Test{
        return Test("From di test...")
    }
}

class Test(val name : String){}