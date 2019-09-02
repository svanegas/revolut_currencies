package com.svanegas.revolut.currencies.database

import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Singleton

@Module
object RealmModule {
    @Provides
    @JvmStatic
    @Singleton
    fun provideRealmConfigDescriptor() = RealmConfigDescriptor(
        arrayOf()
    )

    @Provides
    @JvmStatic
    @Singleton
    fun provideRealmMigrations() = CurrenciesRealmMigration()

    @Provides
    @JvmStatic
    @Singleton
    fun provideRealmBuilder(
        realmConfigDescriptor: RealmConfigDescriptor,
        migration: CurrenciesRealmMigration
    ): RealmConfiguration.Builder = RealmConfiguration.Builder()
        .name("${realmConfigDescriptor.databaseName}.realm")
        .schemaVersion(realmConfigDescriptor.version)
        .modules(realmConfigDescriptor.baseModule, *realmConfigDescriptor.additionalModules)
        .migration(migration)

    @Provides
    @JvmStatic
    @Singleton
    fun provideRealm(builder: RealmConfiguration.Builder): Realm =
        getSafeRealmInstance(builder.build())
}
