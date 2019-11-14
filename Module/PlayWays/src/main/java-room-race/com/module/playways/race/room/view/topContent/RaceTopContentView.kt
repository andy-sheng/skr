package com.module.playways.race.room.view.topContent

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RacePlaySeatUpdateEvent
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.event.RaceWaitSeatUpdateEvent
import com.module.playways.race.room.event.UpdateAudienceCountEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceTopContentView : ConstraintLayout {
    val TAG = "RaceTopContentView"

    var arrowIv: ImageView
    var avatarIv: BaseImageView
    var userPlayNickNameTv: ExTextView
    var realNickNameTv: ExTextView
    var audienceIv: ImageView
    var playerCountTv: ExTextView
    var audienceCountTv: ExTextView
    var playerBg: ExImageView
    var userBgStrokeIv: ExImageView
    var audienceBgStrokeIv: ExImageView

    internal var mIsOpen = true
    private var mRoomData: RaceRoomData? = null

    internal var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.race_top_content_view_layout, this)

        arrowIv = rootView.findViewById(R.id.arrow_iv)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        userPlayNickNameTv = rootView.findViewById(R.id.user_play_nickName_tv)
        realNickNameTv = rootView.findViewById(R.id.real_nickName_tv)
        audienceIv = rootView.findViewById(R.id.audience_iv)
        playerCountTv = rootView.findViewById(R.id.player_count_tv)
        audienceCountTv = rootView.findViewById(R.id.audience_count_tv)
        playerBg = rootView.findViewById(R.id.player_bg)
        userBgStrokeIv = rootView.findViewById(R.id.user_bg_stroke_iv)
        audienceBgStrokeIv = rootView.findViewById(R.id.audience_bg_stroke_iv)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        userBgStrokeIv.setDebounceViewClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(MyUserInfoManager.uid.toInt()))
        }

        audienceBgStrokeIv.setDebounceViewClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(MyUserInfoManager.uid.toInt()))
        }

        arrowIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.clickArrow(!mIsOpen)
            }
        })

        playerBg.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mListener?.clickMore()
            }
        })

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setBorderColor(U.getColor(R.color.white))
                .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .setCircle(true)
                .build())
    }

    fun setName() {
        mRoomData?.let {
            if (it.audience) {
                realNickNameTv.visibility = View.GONE
                userPlayNickNameTv.visibility = View.GONE
                userBgStrokeIv.visibility = View.GONE
                audienceBgStrokeIv.visibility = View.VISIBLE
            } else {
                realNickNameTv.visibility = View.VISIBLE
                userPlayNickNameTv.visibility = View.VISIBLE
                userBgStrokeIv.visibility = View.VISIBLE
                audienceBgStrokeIv.visibility = View.GONE
                userPlayNickNameTv.text = it.getPlayerOrWaiterInfoModel(MyUserInfoManager.uid!!.toInt())?.fakeUserInfo?.nickName
                realNickNameTv.text = MyUserInfoManager.nickName
            }
        }
    }

    private fun updateCount() {
        mRoomData?.let {
            playerCountTv.text = "选手${mRoomData?.getPlayerCount()}人"

            var info = mRoomData?.realRoundInfo
            if (info == null) {
                info = mRoomData?.expectRoundInfo
            }
            info?.let {
                audienceCountTv.text = "观众${it.audienceUserCnt}人"
            }
        }
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            arrowIv.setImageResource(R.drawable.race_expand_icon)
        } else {
            // 折叠状态
            mIsOpen = false
            arrowIv.setImageResource(R.drawable.race_shrink_icon)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateAudienceCountEvent) {
        audienceCountTv.text = "观众${(event.count)}人"
    }

    //只有轮次切换的时候调用
    private fun initData(from: String) {
        val list = mRoomData?.getPlayerAndWaiterInfoList()
        MyLog.d(TAG, "initData list.size=${list?.size} from=$from")
        setName()
        updateCount()
    }

    fun setRoomData(roomData: RaceRoomData) {
        mRoomData = roomData
        initData("setRoomData")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundChangeEvent) {
        initData("RaceRoundChangeEvent")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RacePlaySeatUpdateEvent) {
        initData("RacePlaySeatUpdateEvent")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWaitSeatUpdateEvent) {
        initData("RaceWaitSeatUpdateEvent")
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
        fun clickMore()
    }
}