package com.svanegas.revolut.currencies.base.utility

import io.reactivex.Maybe
import io.realm.RealmObject
import io.realm.RealmResults

fun <T : RealmObject> RealmResults<T>.asMaybe(): Maybe<RealmResults<T>> = this
    .asFlowable()
    .filter { it.isLoaded }
    .firstElement()
    .filter { it.isValid }