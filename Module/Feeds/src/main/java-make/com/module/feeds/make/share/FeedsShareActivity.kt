package com.module.feeds.make.share

import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.share.SharePlatform
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.umeng.socialize.ShareAction
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb


@Route(path = RouterConstants.ACTIVITY_FEEDS_SHARE)
class FeedsShareActivity : BaseActivity() {

    lateinit var shareBg: ImageView
    lateinit var avatarIv: BaseImageView
    lateinit var nameTv: TextView
    lateinit var songTv: TextView
    lateinit var sayEdit: EditText
    lateinit var shareTips: TextView
    lateinit var qqkongjianBtn: ImageView
    lateinit var qqBtn: ImageView
    lateinit var weixinhaoyouBtn: ImageView
    lateinit var pengyouquanBtn: ImageView


    var mFeedsMakeModel: FeedsMakeModel? = null


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_share_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = intent.getSerializableExtra("feeds_make_model") as FeedsMakeModel?

        shareBg = this.findViewById(R.id.share_bg)
        avatarIv = this.findViewById(R.id.avatar_iv)
        nameTv = this.findViewById(R.id.name_tv)
        songTv = this.findViewById(R.id.song_tv)
        sayEdit = this.findViewById(R.id.say_edit)
        shareTips = this.findViewById(R.id.share_tips)
        qqkongjianBtn = this.findViewById(R.id.qqkongjian_btn)
        qqBtn = this.findViewById(R.id.qq_btn)
        weixinhaoyouBtn = this.findViewById(R.id.weixinhaoyou_btn)
        pengyouquanBtn = this.findViewById(R.id.pengyouquan_btn)

        qqkongjianBtn.setOnClickListener {
            if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                U.getToastUtil().showShort("未安装QQ")
                return@setOnClickListener
            }
            shareUrl(SharePlatform.QZONE)
        }

        qqBtn.setOnClickListener {
            if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                U.getToastUtil().showShort("未安装QQ")
                return@setOnClickListener
            }
            shareUrl(SharePlatform.QQ)
        }

        pengyouquanBtn.setOnClickListener {
            if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                U.getToastUtil().showShort("未安装微信")
                return@setOnClickListener
            }
            shareUrl(SharePlatform.WEIXIN_CIRCLE)
        }
        weixinhaoyouBtn.setOnClickListener {
            if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                U.getToastUtil().showShort("未安装微信")
                return@setOnClickListener
            }
            shareUrl(SharePlatform.WEIXIN)
        }
    }

    private fun shareUrl(sharePlatform: SharePlatform) {
        var url = String.format("http://dev.app.inframe.mobi/feed/song?songID=2332", mFeedsMakeModel?.uploadFeedsId)
        url = ApiManager.getInstance().findRealUrlByChannel(url)
        if (!TextUtils.isEmpty(url)) {
            val web = UMWeb(url)
            web.title = "" + mFeedsMakeModel?.uploadSongName
            web.description = mFeedsMakeModel?.uploadSongDesc

            if (sharePlatform == SharePlatform.WEIXIN_CIRCLE) {
                web.setThumb(UMImage(this, R.drawable.share_app_weixin_circle_icon))
            } else {
                web.setThumb(UMImage(this, "http://res-static.inframe.mobi/common/skr_logo2.png"))
            }

            when (sharePlatform) {
                SharePlatform.QQ -> ShareAction(this).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .share()
                SharePlatform.QZONE -> ShareAction(this).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .share()
                SharePlatform.WEIXIN -> ShareAction(this).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .share()

                SharePlatform.WEIXIN_CIRCLE -> ShareAction(this).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .share()
            }
        } else {
            MyLog.w(TAG, "shareUrl sharePlatform=$sharePlatform")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun useEventBus(): Boolean {
        return false
    }
}
