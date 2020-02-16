package com.component.person

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.R
import com.component.busilib.view.NickNameView
import com.component.level.utils.LevelConfigUtils
import com.component.person.view.CommonAudioView
import com.component.person.view.PersonTagView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants

// 名片页面 不知道这帮人怎么想的
@Route(path = RouterConstants.ACTIVITY_PERSON_BUSINESS)
class PersonBusinessActivity : BaseActivity() {
    lateinit var title: CommonTitleBar
    lateinit var backIv: ImageView
    lateinit var avatarIv: SimpleDraweeView
    lateinit var nameView: NickNameView
    lateinit var levelArea: ExConstraintLayout
    lateinit var levelIv: ImageView
    lateinit var levelDesc: TextView
    lateinit var audioView: CommonAudioView
    lateinit var personTagView: PersonTagView
    lateinit var divider: View
    lateinit var signTv: ExTextView

    var userInfoModel: UserInfoModel? = null
    var meiLiCntTotal = 0
    var fansNum = 0

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_business_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        userInfoModel = intent.getSerializableExtra("userInfoModel") as UserInfoModel?
        if (userInfoModel == null) {
            MyLog.w(TAG, "PersonPostActivity userInfoModel = null")
            finish()
            return
        }
        meiLiCntTotal = intent.getIntExtra("meiLiCntTotal", 0)
        fansNum = intent.getIntExtra("fansNum", 0)

        U.getStatusBarUtil().setTransparentBar(this, false)

        title = findViewById(R.id.title)
        backIv = findViewById(R.id.back_iv)
        avatarIv = findViewById(R.id.avatar_iv)
        nameView = findViewById(R.id.name_view)
        levelArea = findViewById(R.id.level_area)
        levelIv = findViewById(R.id.level_iv)
        levelDesc = findViewById(R.id.level_desc)
        audioView = findViewById(R.id.audio_view)
        personTagView = findViewById(R.id.person_tag_view)
        divider = findViewById(R.id.divider)
        signTv = findViewById(R.id.sign_tv)


        backIv.setDebounceViewClickListener {
            finish()
        }

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(userInfoModel?.avatar)
                .setCircle(true)
                .build())
        nameView.setHonorText(userInfoModel?.nicknameRemark, userInfoModel?.honorInfo)
        val ranking = userInfoModel?.ranking
        if (ranking != null && LevelConfigUtils.getSmallImageResoucesLevel(ranking.mainRanking) != 0) {
            levelArea.visibility = View.VISIBLE
            levelIv.background = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(ranking.mainRanking))
            levelDesc.text = ranking.rankingDesc
        } else {
            levelArea.visibility = View.GONE
        }
        signTv.text = userInfoModel?.signature

        personTagView.setFansNum(fansNum)
        personTagView.setCharmTotal(meiLiCntTotal)
        userInfoModel?.let {
            personTagView.setSex(it.sex)
            personTagView.setLocation(it.location)
            personTagView.setUserID(it.userId)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}