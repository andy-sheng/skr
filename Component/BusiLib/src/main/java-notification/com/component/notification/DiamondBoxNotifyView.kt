package com.component.notification

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.zq.live.proto.broadcast.PartyDiamondbox

class DiamondBoxNotifyView : ConstraintLayout {

    private val TAG = DiamondBoxNotifyView::class.simpleName
    lateinit var mBg: ExImageView
    lateinit var mGiftIv: BaseImageView
    lateinit var mEnterTv: ExTextView
    lateinit var mContentTv: ExTextView

    constructor(context: Context?) : super(context){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }


    fun init() {
        View.inflate(context, R.layout.diamon_box_notification_view_layout, this)

        View.inflate(context, com.component.busilib.R.layout.big_gift_notification_view_layout, this)
        mBg = findViewById<View>(com.component.busilib.R.id.bg) as ExImageView
        mGiftIv = findViewById<View>(com.component.busilib.R.id.gift_iv) as BaseImageView
        mEnterTv = findViewById<View>(com.component.busilib.R.id.enter_tv) as ExTextView
        mContentTv = findViewById<View>(com.component.busilib.R.id.content_tv) as ExTextView

    }

    fun bindData(diamondBoxData:String){

        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = 600
        animation.repeatMode = Animation.REVERSE
        animation.interpolator = OvershootInterpolator()
        animation.fillAfter = true
        startAnimation(animation)

        val partyDiamondbox = JSON.parseObject(diamondBoxData, PartyDiamondbox::class.java)


        mEnterTv.visibility = View.VISIBLE
        mEnterTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoDiamondBoxPartyRoom(partyDiamondbox.roomID, 1, 0, diamondBoxData)
            }
        })

        mContentTv.text = partyDiamondbox.pBeginDiamondbox.user.userInfo.nickName

    }

}