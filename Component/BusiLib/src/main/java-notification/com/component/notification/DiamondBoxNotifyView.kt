package com.component.notification

import android.content.Context
import android.graphics.Color
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
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.zq.live.proto.broadcast.PartyDiamondbox
import java.io.Serializable

/**
 * 钻石宝箱通知
 */
class DiamondBoxNotifyView : ConstraintLayout {

    private val TAG = DiamondBoxNotifyView::class.java.simpleName
    lateinit var mBg: ExImageView
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

        mBg = findViewById<View>(R.id.bg) as ExImageView
        mEnterTv = findViewById<View>(R.id.enter_tv) as ExTextView
        mContentTv = findViewById<View>(R.id.content_tv) as ExTextView

    }

    fun bindData(diamondBoxData:Serializable, clickCallback:(() -> Unit)){
        MyLog.e("显示钻石宝箱 $diamondBoxData")
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = 600
        animation.repeatMode = Animation.REVERSE
        animation.interpolator = OvershootInterpolator()
        animation.fillAfter = true
        startAnimation(animation)

        val partyDiamondbox = diamondBoxData as PartyDiamondbox
        val roomID = partyDiamondbox.roomID
        val pBeginDiamondbox = partyDiamondbox.pBeginDiamondbox
        val user = pBeginDiamondbox.user
        val userInfo = user.userInfo
        val nickname = userInfo.nickName
        
        mEnterTv.visibility = View.VISIBLE
        mEnterTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.e("点击进入钻石宝箱房间")
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoDiamondBoxPartyRoom(roomID.toInt(), 1, 0, diamondBoxData)
                clickCallback.invoke()
            }
        })

        val contentStr = SpanUtils().append(nickname)
                .setForegroundColor(Color.parseColor("#FFFFC970"))
                .appendSpace(1)
                .append("在主题房送出【钻石大宝箱】")
                .setForegroundColor(Color.parseColor("#CCFFFFFF"))
                .create()
        mContentTv.text = contentStr

    }

}