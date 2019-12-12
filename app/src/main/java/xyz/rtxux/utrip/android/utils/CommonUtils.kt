package xyz.rtxux.utrip.android.utils

import android.app.Activity
import android.util.TypedValue
import android.widget.Toast
import androidx.fragment.app.Fragment
import xyz.rtxux.utrip.android.App

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

object CommonUtils {
    fun dp2px(dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            App.CONTEXT.resources.displayMetrics
        ).toInt()
    }

    fun randomKey(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length).map {
            chars.random()
        }.joinToString("")
    }
}

