package com.svanegas.revolut.currencies.database

import com.svanegas.revolut.currencies.entity.Currency
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.exceptions.RealmMigrationNeededException
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * When new realm migration needed, add new item to this list with specified index.
 * Index is version of migration (it could be without it, but we want to be sure when merging, something is not earlier than other automatically)
 * When user opens app, proper migrations are done automatically without the need of incrementing version or similar actions.
 */
val migrations = mutableListOf<(realm: DynamicRealm) -> Unit>().apply {
    add(0) { realm ->
        realm.schema
            .create(Currency::class.java.simpleName.toString())
            .addField(Currency.Keys.SYMBOL, String::class.java, FieldAttribute.PRIMARY_KEY)
            .addField(Currency.Keys.RATIO, Double::class.java)
            .addField(Currency.Keys.NAME, String::class.java)
            .addField(Currency.Keys.BASE_AT, Date::class.java)
            .addField(Currency.Keys.AMOUNT, String::class.java)
    }
}

/**
 * Indicates latest realm version.
 * Don't change, it's taken automatically based on how many migrations we have in [migrations] collection
 */
val DB_VERSION_LATEST = migrations.lastIndex.toLong()

class CurrenciesRealmMigration @Inject constructor() : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Timber.i("CurrenciesRealmMigration.migrate oldVersion[$oldVersion] to newVersion[$newVersion]")
        if (realm.schema == null) throw RealmMigrationNeededException(
            realm.path,
            "Attempt to migrate realm with null schema!"
        )
        if (realm.configuration == null) throw RealmMigrationNeededException(
            realm.path,
            "Attempt to migrate realm with null configuration!"
        )

        try {
            // WARNING: don't touch me - it takes proper indexes from [migrations] collection
            var installedVersion = oldVersion.toInt() + 1
            // WARNING: sublist takes exclusively the second parameter (that's why there's +1)
            migrations.subList(installedVersion, newVersion.toInt() + 1)
                .forEach { migrationFunction ->
                    Timber.i("Running realm migration [$installedVersion]")
                    migrationFunction.invoke(realm)
                    installedVersion++ // this is gonna be one more than [newVersion] which means we have to check `<=` for inconsistency
                }

            if (installedVersion <= newVersion) { // WARNING: here is on purpose `<=` (because [installedVersion] will be +1 than [newVersion] in the end
                throw RealmMigrationNeededException(
                    realm.path,
                    "Inconsistency between requirement migrate to version $newVersion and latest migrated version $installedVersion."
                )
            }
        } catch (e: Exception) {
            // log this exception, so that we can see it in release builds
            Timber.e(e)

            // if any exception happens, just rethrow with [RealmMigrationNeededException], so that realm is deleted instead of infinite crashing
            throw RealmMigrationNeededException(
                realm.path, e.message
                    ?: "Problem with migrating realm"
            )
        }
    }
}
