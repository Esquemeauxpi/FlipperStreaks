package com.flipperstreaks.app.di

import android.content.Context
import androidx.room.Room
import com.flipperstreaks.app.data.local.db.AppDatabase
import com.flipperstreaks.app.data.local.db.dao.LocationCacheDao
import com.flipperstreaks.app.data.local.db.dao.PlayLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()

    @Provides
    fun providePlayLogDao(db: AppDatabase): PlayLogDao = db.playLogDao()

    @Provides
    fun provideLocationCacheDao(db: AppDatabase): LocationCacheDao = db.locationCacheDao()
}
