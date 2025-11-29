package com.app.ripple.presentation.navigation.nav_type

import android.net.Uri
import androidx.navigation.NavType
import androidx.savedstate.SavedState
import com.app.ripple.data.nearby.model.NearbyDevice
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NearbyDeviceNavType : NavType<NearbyDevice>(
    isNullableAllowed = false
){
    override fun put(
        bundle: SavedState,
        key: String,
        value: NearbyDevice
    ) {
        bundle.putString(key, Json.encodeToString(value))
    }

    override fun get(
        bundle: SavedState,
        key: String
    ): NearbyDevice? {
        return Json.decodeFromString(bundle.getString(key) ?: return null)
    }

    override fun parseValue(value: String): NearbyDevice {
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun serializeAsValue(value: NearbyDevice): String {
        return Uri.encode(Json.encodeToString(value))
    }
}