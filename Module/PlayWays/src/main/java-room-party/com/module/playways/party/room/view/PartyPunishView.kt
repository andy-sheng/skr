package com.module.playways.party.room.view

import android.os.Handler
import android.os.Message
import android.support.constraint.Group
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.model.PartyPunishInfoModel
import com.module.playways.party.room.presenter.PartyGamePresenter
import com.module.playways.party.room.ui.IPartyGameView
import com.module.playways.room.data.H
import com.zq.live.proto.PartyRoom.PBeginPunish
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PartyPunishView(viewStub: ViewStub, protected var mRoomData: PartyRoomData) : ExViewStub(viewStub), IPartyGameView {
    val mTag = "PartyPunishView"
    var presenter: PartyGamePresenter? = null
    var delayHideJob: Job? = null

    internal val MSG_FAST_MUSIC = 3
    internal val MSG_DECR_MUSIC = 4
    internal val MSG_RESULT_MUSIC = 5

    lateinit var container: ImageView
    lateinit var raceMatchItemView: PartyPunishItemListView
    lateinit var dengView: ImageView
    lateinit var zxhTv: ExTextView
    lateinit var dmxTv: ExTextView
    lateinit var nextPunishIv: ExImageView
    lateinit var closeIv: ImageView
    lateinit var punishTypeTv: ExTextView
    lateinit var punishContent: ExTextView
    lateinit var coverGroup: Group

    var timer: HandlerTaskTimer? = null

    var lastUpdateTs: Long = 0

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when {
                msg.what == MSG_FAST_MUSIC -> {
                    starFastBgMusic()
                }
                msg.what == MSG_DECR_MUSIC -> {
                    starDecrBgMusic()
                }
                msg.what == MSG_RESULT_MUSIC -> {
                    starResultMusic()
                }
            }
        }
    }

    override fun init(parentView: View) {
        U.getSoundUtils().preLoad(mTag, R.raw.rank_flipsonglist, R.raw.race_select, R.raw.race_unselect)
        presenter = PartyGamePresenter(mRoomData, this)
        container = parentView.findViewById(R.id.container)
        raceMatchItemView = parentView.findViewById(R.id.race_match_item_view)
        dengView = parentView.findViewById(R.id.deng_view)
        zxhTv = parentView.findViewById(R.id.zxh_tv)
        dmxTv = parentView.findViewById(R.id.dmx_tv)
        nextPunishIv = parentView.findViewById(R.id.next_punish_iv)
        closeIv = parentView.findViewById(R.id.close_iv)
        punishTypeTv = parentView.findViewById(R.id.punish_type_tv)
        punishContent = parentView.findViewById(R.id.punish_content)
        coverGroup = parentView.findViewById(R.id.cover_group)

        zxhTv.setDebounceViewClickListener {
            if (mRoomData.hostId == MyUserInfoManager.uid.toInt() && !zxhTv.isSelected) {
                zxhTv.isSelected = true
                dmxTv.isSelected = false
                presenter?.type = 1
                punishTypeTv.text = "真心话"
                coverGroup.visibility = View.VISIBLE
                raceMatchItemView.visibility = View.GONE
                presenter?.getGameList()
                delayHideJob?.cancel()
            }
        }

        dmxTv.setDebounceViewClickListener {
            if (mRoomData.hostId == MyUserInfoManager.uid.toInt() && !dmxTv.isSelected) {
                dmxTv.isSelected = true
                zxhTv.isSelected = false
                presenter?.type = 2
                punishTypeTv.text = "大冒险"
                coverGroup.visibility = View.VISIBLE
                raceMatchItemView.visibility = View.GONE
                presenter?.getGameList()
                delayHideJob?.cancel()
            }
        }

        zxhTv.isSelected = true

        nextPunishIv.setDebounceViewClickListener {
            presenter?.getNextPunish()
            nextPunishIv.isSelected = true
        }

        closeIv.setDebounceViewClickListener {
            setVisibility(View.GONE)
            raceMatchItemView.reset()
        }

        if (mRoomData.hostId == MyUserInfoManager.uid.toInt()) {
            closeIv.visibility = View.VISIBLE
            nextPunishIv.visibility = View.VISIBLE
            coverGroup.visibility = View.VISIBLE
            raceMatchItemView.visibility = View.GONE
            presenter?.type = 1
            presenter?.getGameList()
        } else {
            closeIv.visibility = View.GONE
            nextPunishIv.visibility = View.GONE
            coverGroup.visibility = View.GONE
            raceMatchItemView.visibility = View.VISIBLE
        }

        container.setDebounceViewClickListener {
            //拦截
        }
    }

    // 播放快速滚动音乐
    private fun starFastBgMusic() {
        timer?.dispose()
        timer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval(raceMatchItemView.itemTime.toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        U.getSoundUtils().play(mTag, R.raw.rank_flipsonglist)
                    }
                })
    }

    // 播放减速滚动音乐
    private fun starDecrBgMusic() {
        timer?.dispose()
        timer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval(2 * raceMatchItemView.itemTime.toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        U.getSoundUtils().play(mTag, R.raw.rank_flipsonglist)
                    }
                })
    }

    // 播放是否选中音乐
    private fun starResultMusic() {
        timer?.dispose()
//        U.getSoundUtils().play(mTag, R.raw.race_select)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (mRoomData.hostId == MyUserInfoManager.uid.toInt()) {
            closeIv.visibility = View.VISIBLE
            nextPunishIv.visibility = View.VISIBLE
        } else {
            closeIv.visibility = View.GONE
            nextPunishIv.visibility = View.GONE
        }

        if (visibility == View.GONE) {
            delayHideJob?.cancel()
        }
    }

    fun showWithGuest(event: PBeginPunish) {
        tryInflate()
        setVisibility(View.GONE)

        if (event.beginTimeMs > lastUpdateTs) {
            lastUpdateTs = event.beginTimeMs
            presenter?.type = event.punishInfo.punishType.value
            presenter?.getGameList {
                val model = PartyPunishInfoModel.toLocalModelFromPB(event.punishInfo)
                presenter?.scrollToModel(model, event.endTimeMs - event.beginTimeMs)
            }
        }
    }

    fun show() {
        setVisibility(View.VISIBLE)
    }

    override fun showPunishList(list: ArrayList<PartyPunishInfoModel>) {
        //这里不展示
    }

    override fun updateGame(index: Int, model: PartyPunishInfoModel, duration: Long) {
        delayHideJob?.cancel()
        setVisibility(View.VISIBLE)
        coverGroup.visibility = View.GONE
        raceMatchItemView.visibility = View.VISIBLE

        if (model.punishType == 1) {
            zxhTv.isSelected = true
            dmxTv.isSelected = false
        } else {
            zxhTv.isSelected = false
            dmxTv.isSelected = true
        }

        if (H.partyRoomData?.hostId != MyUserInfoManager.uid.toInt()) {
            delayHideJob = launch {
                delay(duration)
                setVisibility(View.GONE)
                raceMatchItemView.reset()
            }
        } else {
            nextPunishIv.isSelected = false
        }

        raceMatchItemView.setData(presenter?.getCurList(model.punishType), index) {

        }

        mUiHandler.removeMessages(MSG_FAST_MUSIC)
        mUiHandler.sendEmptyMessage(MSG_FAST_MUSIC)

        mUiHandler.removeMessages(MSG_DECR_MUSIC)
        mUiHandler.sendEmptyMessageDelayed(MSG_DECR_MUSIC, raceMatchItemView.getFastTime())


        mUiHandler.removeMessages(MSG_RESULT_MUSIC)
        mUiHandler.sendEmptyMessageDelayed(MSG_RESULT_MUSIC, raceMatchItemView.getAnimationTime())
    }

    override fun getNextPunishFailed() {
        nextPunishIv.isSelected = false
    }

    override fun layoutDesc(): Int {
        return R.layout.party_punish_view_layout
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyHostChangeEvent) {
        setVisibility(View.GONE)
        raceMatchItemView.reset()
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        presenter?.destroy()
        raceMatchItemView.reset()
    }
}