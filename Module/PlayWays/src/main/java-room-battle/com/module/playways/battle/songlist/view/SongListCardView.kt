package com.module.playways.battle.songlist.view

import android.content.Context
import android.support.constraint.Group
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.battle.BattleServerApi
import com.module.playways.battle.songlist.adapter.SongListCardAdapter
import com.module.playways.battle.songlist.model.BattleSongModel
import com.module.playways.battle.songlist.model.BattleTagModel
import com.module.playways.room.prepare.model.PrepareData
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

//歌单详情页面
class SongListCardView(val model: BattleTagModel, context: Context) : ExConstraintLayout(context), CoroutineScope by MainScope() {
    val mTag = "SongListCardView"

    private val recordFilm: ImageView
    private val recordCover: SimpleDraweeView
    private val songNameTv: ExTextView
    private val hasSingTv: ExTextView
    private val rankIv: ImageView
    private val lightCountTv: ExTextView
    private val unlockGroup: Group
    private val starView: BattleStarView
    private val starBg: ExImageView
    private val startSongList: ExTextView
    private val recyclerView: RecyclerView
    private val cancelIv: ImageView

    val adapter = SongListCardAdapter()
    private val battleServerApi: BattleServerApi = ApiManager.getInstance().createService(BattleServerApi::class.java)

    private var mDialogPlus: DialogPlus? = null

    var blightCnt = 0  // 爆灯数
    var getSongCnt = 0  // 唱过

    init {
        View.inflate(context, R.layout.song_list_card_view, this)
        background = (U.getDrawable(R.drawable.gedan_yulan_bj))
        recordFilm = this.findViewById(R.id.record_film)
        recordCover = this.findViewById(R.id.record_cover)
        songNameTv = this.findViewById(R.id.song_name_tv)
        hasSingTv = this.findViewById(R.id.has_sing_tv)
        rankIv = this.findViewById(R.id.rank_iv)
        lightCountTv = this.findViewById(R.id.light_count_tv)
        unlockGroup = this.findViewById(R.id.unlock_group)
        starBg = this.findViewById(R.id.star_bg)
        starView = this.findViewById(R.id.star_view)
        startSongList = this.findViewById(R.id.start_song_list)
        recyclerView = this.findViewById(R.id.recycler_view)
        cancelIv = this.findViewById(R.id.cancel_iv)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        startSongList.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                enableStandTag()
            }
        })

        starBg.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                //todo 再次演唱
                val prepareData = PrepareData()
                prepareData.setGameType(GameModeType.GAME_MODE_PLAYBOOK)
//                prepareData.setTagId(model.tagID)
                prepareData.setTagId(8)

                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                        .withSerializable("prepare_data", prepareData)
                        .navigation()
            }
        })

        cancelIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                dismiss()
            }
        })

        rankIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_BATTLE_RANK)
                        .withInt("tagID", model.tagID)
                        .navigation()
            }
        })

        getStandSongList()
    }

    private fun enableStandTag() {
        launch {
            val map = HashMap<String, Any>()
            map["tagID"] = model.tagID
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("enableStandTag", ControlType.CancelThis)) {
                battleServerApi.enableStandTag(body)
            }
            if (result.errno == 0) {
                //todo 进入演唱
                val prepareData = PrepareData()
                prepareData.setGameType(GameModeType.GAME_MODE_PLAYBOOK)
//                prepareData.setTagId(model.tagID)
                prepareData.setTagId(8)

                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                        .withSerializable("prepare_data", prepareData)
                        .navigation()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (layoutParams as (FrameLayout.LayoutParams)).leftMargin = U.getDisplayUtils().dip2px(10f)
        (layoutParams as (FrameLayout.LayoutParams)).rightMargin = U.getDisplayUtils().dip2px(10f)
    }

    private fun getStandSongList() {
        launch {
            val result = subscribe(RequestControl("getStandSongList", ControlType.CancelThis)) {
                battleServerApi.getStandSongList(MyUserInfoManager.getInstance().uid, model.tagID)
            }
            if (result.errno == 0) {
                blightCnt = result.data.getIntValue("blightCnt")
                getSongCnt = result.data.getIntValue("getSongCnt")
                val list = JSON.parseArray(result.data.getString("details"), BattleSongModel::class.java)
                showSongCard(list)
            } else {

            }
        }
    }

    private fun showSongCard(list: List<BattleSongModel>?) {
        adapter.mDataList.clear()
        if (!list.isNullOrEmpty()) {
            adapter.mDataList.addAll(list)
        }
        adapter.notifyDataSetChanged()

        songNameTv.text = model.tagName
        hasSingTv.text = getSongCnt.toString()
        lightCountTv.text = blightCnt.toString()
        AvatarUtils.loadAvatarByUrl(recordCover, AvatarUtils.newParamsBuilder(model.coverURL)
                .setCircle(true)
                .build())
        if (model.status == BattleTagModel.SST_LOCK) {
            unlockGroup.visibility = View.GONE
            startSongList.visibility = View.VISIBLE
        } else {
            unlockGroup.visibility = View.VISIBLE
            startSongList.visibility = View.GONE
            starView.bindData(model.starCnt, 5)
        }
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentHeight(U.getDisplayUtils().phoneHeight - 68.dp())
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }
}
