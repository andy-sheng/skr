package com.module.feeds.make.share

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePlatform
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.umeng.socialize.ShareAction
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import com.umeng.socialize.media.UMusic


@Route(path = RouterConstants.ACTIVITY_FEEDS_SHARE)
class FeedsShareActivity : BaseActivity() {

    lateinit var shareBg: ImageView
    lateinit var avatarIv: BaseImageView
    lateinit var nameTv: TextView
    lateinit var songTv: TextView
    lateinit var shareTips: TextView
    lateinit var qqkongjianBtn: ImageView
    lateinit var qqBtn: ImageView
    lateinit var weixinhaoyouBtn: ImageView
    lateinit var pengyouquanBtn: ImageView
    lateinit var ivBack: ImageView


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
        shareTips = this.findViewById(R.id.share_tips)
        qqkongjianBtn = this.findViewById(R.id.qqkongjian_btn)
        qqBtn = this.findViewById(R.id.qq_btn)
        weixinhaoyouBtn = this.findViewById(R.id.weixinhaoyou_btn)
        pengyouquanBtn = this.findViewById(R.id.pengyouquan_btn)
        ivBack = this.findViewById(R.id.iv_back)

        AvatarUtils.loadAvatarByUrl(avatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(2.dp().toFloat())
                        .setCircle(true)
                        .build())
        nameTv.text = MyUserInfoManager.getInstance().nickName
        songTv.text = "《${mFeedsMakeModel?.songModel?.workName}》"

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

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
        var url = String.format("http://www.skrer.mobi/feed/song?songID=%s&userID=%s", mFeedsMakeModel?.songModel?.songID, MyUserInfoManager.getInstance().uid)
        if (!TextUtils.isEmpty(url)) {
            val music = UMusic(mFeedsMakeModel?.songModel?.playURL)
            music.title = "" + mFeedsMakeModel?.songModel?.workName
            music.description = MyUserInfoManager.getInstance().nickName

            if (sharePlatform == SharePlatform.WEIXIN_CIRCLE || sharePlatform == SharePlatform.WEIXIN) {
                music.setThumb(UMImage(this, R.drawable.share_app_weixin_circle_icon))
            } else {
                music.setThumb(UMImage(this, "http://res-static.inframe.mobi/app/app_icon.webp"))
            }
            music.setmTargetUrl(url)

            when (sharePlatform) {
                SharePlatform.QQ -> ShareAction(this).withMedia(music)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .share()
                SharePlatform.QZONE -> ShareAction(this).withMedia(music)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .share()
                SharePlatform.WEIXIN -> ShareAction(this).withMedia(music)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .share()
                SharePlatform.WEIXIN_CIRCLE -> ShareAction(this).withMedia(music)
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
