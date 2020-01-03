package com.module.playways.relay.room

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.report.fragment.QuickFeedbackFragment
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.room.model.RelayResultModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 接唱结果页
@Route(path = RouterConstants.ACTIVITY_RELAY_RESULT)
class RelayResultActivity : BaseActivity() {

    lateinit var followTv: ExImageView
    lateinit var backTv: TextView

    var roomData: RelayRoomData? = null

    var isFriend: Boolean? = null
    var isFollow: Boolean? = null

    lateinit var mainActContainer: ConstraintLayout
    lateinit var title: CommonTitleBar
    lateinit var reportTv: TextView
    lateinit var contentArea: ConstraintLayout
    lateinit var avatarLeftIv: BaseImageView
    lateinit var leftNameTv: ExTextView
    lateinit var avatarRightIv: BaseImageView
    lateinit var rightNameTv: ExTextView
    lateinit var fenTv: ExTextView
    lateinit var tipsTv: ExTextView
    lateinit var xinIv: BaseImageView
    lateinit var xinCountTv: ExTextView
    lateinit var coinIv: BaseImageView
    lateinit var coinCountTv: ExTextView

    private val relayRoomServerApi = ApiManager.getInstance().createService(RelayRoomServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        roomData = intent.getSerializableExtra("roomData") as RelayRoomData?
        if (roomData == null) {
            finish()
        }

        U.getStatusBarUtil().setTransparentBar(this, false)
        title = findViewById(R.id.title)
        reportTv = findViewById(R.id.report_tv)
        contentArea = findViewById(R.id.content_area)
//        avatarLevel = findViewById(R.id.avatar_level)
//        gameStatusTv = findViewById(R.id.game_status_tv)
//        gameTimeTv = findViewById(R.id.game_time_tv)
        followTv = findViewById(R.id.follow_tv)
        backTv = findViewById(R.id.back_tv)
        avatarLeftIv = findViewById(R.id.avatar_left_iv)
        leftNameTv = findViewById(R.id.left_name_tv)
        avatarRightIv = findViewById(R.id.avatar_right_iv)
        rightNameTv = findViewById(R.id.right_name_tv)
        fenTv = findViewById(R.id.fen_tv)
        tipsTv = findViewById(R.id.tips_tv)
        xinIv = findViewById(R.id.xin_iv)
        xinCountTv = findViewById(R.id.xin_count_tv)
        coinIv = findViewById(R.id.coin_iv)
        coinCountTv = findViewById(R.id.coin_count_tv)
        followTv = findViewById(R.id.follow_tv)
        backTv = findViewById(R.id.back_tv)
        (backTv.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = ((U.getDisplayUtils().phoneHeight - U.getStatusBarUtil().getStatusBarHeight(this) - (U.getDisplayUtils().phoneWidth * 527 / 371)) - U.getDisplayUtils().dip2px(32f)) / 2

        followTv.setDebounceViewClickListener {
            if (isFriend == true || isFollow == true) {
                UserInfoManager.getInstance().mateRelation(roomData?.peerUser?.userID
                        ?: 0, UserInfoManager.RA_UNBUILD, isFriend == true)
            } else {
                UserInfoManager.getInstance().mateRelation(roomData?.peerUser?.userID
                        ?: 0, UserInfoManager.RA_BUILD, false)
            }
        }

        backTv.setDebounceViewClickListener {
            finish()
        }

        reportTv.setDebounceViewClickListener {
            // 举报
            U.getFragmentUtils().addFragment(
                    FragmentUtils.newAddParamsBuilder(this, QuickFeedbackFragment::class.java)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_RELAY_ROOM)
                            .addDataBeforeAdd(1, QuickFeedbackFragment.REPORT)
                            .addDataBeforeAdd(2, roomData?.peerUser?.userID ?: 0)
                            .setEnterAnim(com.component.busilib.R.anim.slide_in_bottom)
                            .setExitAnim(com.component.busilib.R.anim.slide_out_bottom)
                            .build())
        }

        getGameResult()

        AvatarUtils.loadAvatarByUrl(avatarLeftIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        AvatarUtils.loadAvatarByUrl(avatarRightIv, AvatarUtils.newParamsBuilder(roomData?.peerUser?.userInfo?.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        leftNameTv.text = MyUserInfoManager.nickName
        rightNameTv.text = UserInfoManager.getInstance().getRemarkName(roomData?.peerUser?.userInfo?.userId
                ?: 0, roomData?.peerUser?.userInfo?.nickname)
    }

    private fun getGameResult() {
        launch {
            val result = subscribe(RequestControl("getGameResult", ControlType.CancelLast)) {
                relayRoomServerApi.getRelayResult(roomData?.gameId ?: 0)
            }
            if (result.errno == 0) {
                val resultModel = JSON.parseObject(result.data.toJSONString(), RelayResultModel::class.java)
                showGameResult(resultModel)
            } else {

            }
        }
    }

    private fun showGameResult(model: RelayResultModel?) {
        model?.let {
            //            avatarLevel.bindData(roomData?.peerUser?.userInfo)
//            gameStatusTv.text = it.gameEndReasonDesc
//            gameTimeTv.text = "你与${roomData?.peerUser?.userInfo?.nicknameRemark}合唱了${it.chatDurTime}分钟"
            isFollow = it.isFollow
            isFriend = it.isFriend
            refreshFollow()
//            if (it.reason == RelayResultModel.GER_USER_NO_RESPONSE) {
//                // 对方掉线了
//            } else if (it.reason == RelayResultModel.GER_USER_EXIT && it.exitUserID != MyUserInfoManager.uid.toInt()) {
//                // 对方结束了
//            } else {
//                // 合唱结束了（包括我点击结束和时间到了）
//            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == roomData?.peerUser?.userID) {
            isFriend = event.isFriend
            isFollow = event.isFollow

            refreshFollow()
        }
    }

    private fun refreshFollow() {
        when {
            isFriend == true -> {
//                followTv.text = "已互关"
//                followTv.setTextColor(Color.parseColor("#EBAC44"))
                followTv.background = U.getDrawable(R.drawable.reply_has_follow)
            }
            isFollow == true -> {
//                followTv.text = "已关注"
//                followTv.setTextColor(Color.parseColor("#EBAC44"))
                followTv.background = U.getDrawable(R.drawable.reply_has_follow)
            }
            else -> {
//                followTv.text = "关注Ta"
//                followTv.setTextColor(Color.parseColor("#8B572A"))
                followTv.background = U.getDrawable(R.drawable.relay_follow)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun canSlide(): Boolean {
        return false
    }
}