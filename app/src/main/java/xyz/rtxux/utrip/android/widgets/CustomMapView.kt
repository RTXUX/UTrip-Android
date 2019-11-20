package xyz.rtxux.utrip.android.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import com.mapbox.mapboxsdk.maps.MapView

class CustomMapView(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
) : MapView(context, attrs, defStyle) {
    var mParentView: ViewParent? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                mParentView?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                mParentView?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}
