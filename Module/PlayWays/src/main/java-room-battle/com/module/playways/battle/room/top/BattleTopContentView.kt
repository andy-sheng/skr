package com.module.playways.battle.room.top

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.component.person.event.ShowPersonCardEvent
import com.engine.EngineEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BattleTopContentView : ExConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var listener: Listener? = null
    var mIsOpen = true

    var roomData: BattleRoomData? = null

    private val arrowIv: ImageView
    private val userInfoBg: ExImageView
    private val leftAvatarSdv: SimpleDraweeView
    private val rightAvatarSdv: SimpleDraweeView
    private val leftSpeaking: SpeakingTipsAnimationView
    private val rightSpeaking: SpeakingTipsAnimationView
    private val songNumTv: ExTextView

    init {
        View.inflate(context, R.layout.battle_top_content_view_layout, this)

        arrowIv = this.findViewById(R.id.arrow_iv)
        userInfoBg = this.findViewById(R.id.user_info_bg)
        leftAvatarSdv = this.findViewById(R.id.left_avatar_sdv)
        rightAvatarSdv = this.findViewById(R.id.right_avatar_sdv)
        leftSpeaking = this.findViewById(R.id.left_speaking)
        rightSpeaking = this.findViewById(R.id.right_speaking)
        songNumTv = this.findViewById(R.id.song_num_tv)

        arrowIv.setDebounceViewClickListener { listener?.clickArrow(!mIsOpen) }
        leftAvatarSdv.setDebounceViewClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(MyUserInfoManager.uid.toInt()))
        }
        rightAvatarSdv.setDebounceViewClickListener {
            roomData?.getFirstTeammate()?.userID?.let {
                EventBus.getDefault().post(ShowPersonCardEvent(it))
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

    fun switchRoom() {
        bindData()
    }

    fun bindData() {
        // todo 等重写 左边固定是我自己
        AvatarUtils.loadAvatarByUrl(leftAvatarSdv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderWidth(1.dp().toFloat())
                .setBorderColor(Color.WHITE)
                .build())
        AvatarUtils.loadAvatarByUrl(rightAvatarSdv, AvatarUtils.newParamsBuilder(roomData?.getFirstTeammate()?.userInfo?.avatar)
                .setCircle(true)
                .setBorderWidth(1.dp().toFloat())
                .setBorderColor(Color.WHITE)
                .build())
    }

    //todo 需要接一个轮次改变或者进度改变更新歌曲进度

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            var list = event.getObj<List<EngineEvent.UserVolumeInfo>>()
            for (uv in list) {
                //    MyLog.d(TAG, "UserVolumeInfo uv=" + uv);
                if (uv != null) {
                    var uid = uv.uid
                    if (uid == 0) {
                        uid = MyUserInfoManager.uid.toInt()
                    }
                    var volume = uv.volume
                    if (volume > 20) {
                        //todo 判断一下是左边还是右边头像说话
                    }
                }
            }
        }
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

    interface Listener {
        fun clickArrow(open: Boolean)
    }
}