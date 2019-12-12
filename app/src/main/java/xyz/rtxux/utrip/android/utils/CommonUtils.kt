package xyz.rtxux.utrip.android.utils

import android.app.Activity
import android.util.TypedValue
import android.widget.Toast
import androidx.fragment.app.Fragment
import xyz.rtxux.utrip.android.App
import java.text.SimpleDateFormat
import java.util.*

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

fun Long.toTimeStr(): String {
    val time = System.currentTimeMillis() / 1000
    val seconds = time - this / 1000
    if (seconds < 10) {
        return "刚刚"
    }
    if (seconds < 60) {
        return "${seconds}秒前"
    }
    val minutes = seconds / 60
    if (minutes < 60) {
        return "${minutes}分钟前"
    }
    val calendar = Calendar.getInstance()
    val day1 = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date(this * 1000L)
    val day2 = calendar.get(Calendar.DAY_OF_MONTH)
    if (day1 == day2) {
        return SimpleDateFormat("HH:mm", Locale.CHINA).format(this * 1000L)
    }
    return SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(this * 1000L)
}

