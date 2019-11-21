package com.common.image.debug

import android.content.Intent
import android.os.Bundle
import com.common.base.BaseActivity
import com.common.base.R
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.facebook.drawee.drawable.ScalingUtils

class ImageDebugActivity : BaseActivity() {
    lateinit var imgIv: BaseImageView
    lateinit var descTv: ExTextView
    lateinit var debugModel: ImageDebugModel


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.image_debug_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        imgIv = this.findViewById(R.id.img_iv)
        descTv = this.findViewById(R.id.desc_tv)
        debugModel = intent.getSerializableExtra("model") as ImageDebugModel
        descTv.text = debugModel.toString()
        FrescoWorker.loadImage(imgIv, ImageFactory.newPathImage(debugModel.uri)
                .setTipsWhenLarge(false)
                .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun finish() {
        super.finish()
        closeTs = System.currentTimeMillis()
    }

    companion object {
        var closeTs = 0L
        fun open(debugModel: ImageDebugModel) {
            if (System.currentTimeMillis() - closeTs > 10 * 60 * 1000) {
                val intent = Intent(U.getActivityUtils().topActivity, ImageDebugActivity::class.java)
                intent.putExtra("model", debugModel)
                U.getActivityUtils().topActivity!!.startActivity(intent)
            }
        }
    }
}