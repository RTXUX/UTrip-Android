package xyz.rtxux.utrip.android.utils

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.china.shift.ShiftForChina
import org.json.JSONObject

object CoordinateUtils {
    val shiftForChina = ShiftForChina()
    fun shiftChina(latLng: LatLng): LatLng {
        val shiftedJsonString = shiftForChina.shift(latLng.longitude, latLng.latitude)
        val shiftedJson = JSONObject(shiftedJsonString)
        return LatLng(shiftedJson.getDouble("lat"), shiftedJson.getDouble("lon"), latLng.altitude)
    }
}