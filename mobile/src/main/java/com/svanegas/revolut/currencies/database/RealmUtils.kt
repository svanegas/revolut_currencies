package com.svanegas.revolut.currencies.database

import com.svanegas.revolut.currencies.base.CurrenciesBaseConfig
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException

/**
 * For production flavor, it doesn't crash and deletes/recreates realm
 * For all other flavors, it crashes when problem with migration
 */
@Suppress("ConstantConditionIf")
internal fun getSafeRealmInstance(config: RealmConfiguration): Realm {
    if (!CurrenciesBaseConfig.IS_PRODUCTION_FLAVOR_TYPE) {
        // in all flavors except production, we want it rather to crash, so that we can fix the migrations
        return Realm.getInstance(config)
    }

    return try {
        Realm.getInstance(config)
    } catch (e: RealmMigrationNeededException) {
        // in production, if there's a problem with migration, delete the realm and instantiate again, so that it doesn't crash for user
        Realm.deleteRealm(config)
        Realm.getInstance(config)
    }
}