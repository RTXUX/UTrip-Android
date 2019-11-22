package xyz.rtxux.utrip.android.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MyTrack(
    @field:PrimaryKey
    var id: Int,
    var name: String,
    var description: String,
    var timestamp: Long,
    var points: RealmList<MyPoint>,
    var completed: Boolean
) : RealmObject() {
    constructor() : this(0, "", "", 0L, RealmList(), false)
}