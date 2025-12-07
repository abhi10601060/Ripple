package com.app.ripple.data.local.realm.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class TextMessageRealm: RealmObject {
    @PrimaryKey
    var id : ObjectId = ObjectId()
}