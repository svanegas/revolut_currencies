package com.svanegas.revolut.currencies.database

import io.realm.Realm

data class RealmConfigDescriptor(
    val additionalModules: Array<Any>
) {
    val databaseName: String = "currencies"

    val version: Long = DB_VERSION_LATEST

    val baseModule: Any = Realm.getDefaultModule()!!
}
