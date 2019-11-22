package xyz.rtxux.utrip.android.model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MyPoint(
    @field:PrimaryKey
    var id: Int,
    var latitude: Double,
    var longitude: Double,
    var altitude: Double,
    var speed: Double,
    var pathPoint: Boolean,
    var imageUrls: RealmList<String>,
    var name: String,
    var description: String,
    var timestamp: Long
) : RealmObject() {
    constructor() : this(0, 0.0, 0.0, 0.0, 0.0, false, RealmList(), "", "", 0L)
}