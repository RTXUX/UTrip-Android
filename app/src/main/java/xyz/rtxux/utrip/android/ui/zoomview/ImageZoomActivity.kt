package xyz.rtxux.utrip.android.ui.zoomview

import android.os.Bundle
import com.jsibbold.zoomage.ZoomageView
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseActivity
import xyz.rtxux.utrip.android.base.GlideApp

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ImageZoomActivity : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_image_zoom

    private lateinit var zoomageView: ZoomageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")
        zoomageView = findViewById(R.id.zoomView)
        GlideApp.with(this).load(url).into(zoomageView)
    }


}
