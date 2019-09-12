package com.module.playways.battle.songlist

import android.os.Bundle
import android.support.constraint.Group
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.battle.BattleServerApi
import com.module.playways.battle.songlist.adapter.SongListCardAdapter
import com.module.playways.battle.songlist.model.BattleSongModel
import com.module.playways.battle.songlist.model.BattleTagModel
import com.module.playways.battle.songlist.view.BattleStarView
import com.module.playways.room.prepare.model.PrepareData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

//歌单详情页面
class BattleSongListCardFragment : BaseFragment() {
    val mTag = "SongListCardView"

    lateinit var topView: View
    lateinit var recordFilm: ImageView
    lateinit var recordCover: SimpleDraweeView
    lateinit var songNameTv: ExTextView
    lateinit var hasSingTv: ExTextView
    lateinit var rankIv: ImageView
    lateinit var lightCountTv: ExTextView
    lateinit var unlockGroup: Group
    lateinit var lockGroup: Group
    lateinit var starView: BattleStarView
    lateinit var starBg: ExImageView
    lateinit var startSongList: ExTextView
    lateinit var recyclerView: RecyclerView
    lateinit var cancelIv: ImageView

    val adapter = SongListCardAdapter()
    private val battleServerApi: BattleServerApi = ApiManager.getInstance().createService(BattleServerApi::class.java)
    var mSkrAudioPermission: SkrAudioPermission = SkrAudioPermission()

    var model: BattleTagModel? = null
    var tipsDialogView: TipsDialogView? = null

    var blightCnt = 0  // 爆灯数
    var getSongCnt = 0  // 唱过

    override fun initView(): Int {
        return R.layout.song_list_card_fragment_layout
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            model = data as BattleTagModel?
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        topView = rootView.findViewById(R.id.top_view)
        recordFilm = rootView.findViewById(R.id.record_film)
        recordCover = rootView.findViewById(R.id.record_cover)
        songNameTv = rootView.findViewById(R.id.song_name_tv)
        hasSingTv = rootView.findViewById(R.id.has_sing_tv)
        rankIv = rootView.findViewById(R.id.rank_iv)
        lightCountTv = rootView.findViewById(R.id.light_count_tv)
        unlockGroup = rootView.findViewById(R.id.unlock_group)
        lockGroup = rootView.findViewById(R.id.lock_group)
        starBg = rootView.findViewById(R.id.star_bg)
        starView = rootView.findViewById(R.id.star_view)
        startSongList = rootView.findViewById(R.id.start_song_list)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        cancelIv = rootView.findViewById(R.id.cancel_iv)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        startSongList.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                enableStandTag(false)
            }
        })

        starBg.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                goPlaybookMatch()
            }
        })

        topView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getFragmentUtils().popFragment(this@BattleSongListCardFragment)
            }
        })

        cancelIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                U.getFragmentUtils().popFragment(this@BattleSongListCardFragment)

            }
        })

        rankIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_BATTLE_RANK)
                        .withInt("tagID", model?.tagID ?: 0)
                        .navigation()
            }
        })


        songNameTv.text = model?.tagName
        AvatarUtils.loadAvatarByUrl(recordCover, AvatarUtils.newParamsBuilder(model?.coverURL)
                .setCircle(true)
                .build())
        refreshView()
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        getStandSongList()
    }

    private fun refreshView() {
        if (model?.status == BattleTagModel.SST_LOCK) {
            unlockGroup.visibility = View.GONE
            lockGroup.visibility = View.VISIBLE
        } else {
            unlockGroup.visibility = View.VISIBLE
            lockGroup.visibility = View.GONE
            starView.bindData(model?.starCnt ?: 0, 5)
        }
        hasSingTv.text = getSongCnt.toString()
        lightCountTv.text = blightCnt.toString()
    }

    override fun destroy() {
        super.destroy()
        tipsDialogView?.dismiss(false)
        fragmentDataListener?.onFragmentResult(0, 0, null, null)
    }

    private fun goPlaybookMatch() {
        mSkrAudioPermission.ensurePermission({
            val prepareData = PrepareData()
            prepareData.gameType = GameModeType.GAME_MODE_PLAYBOOK
            prepareData.tagId = model?.tagID ?: 0
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                    .withSerializable("prepare_data", prepareData)
                    .navigation()
        }, true)
    }

    // 是否花费金币解锁
    private fun enableStandTag(consumCoin: Boolean) {
        launch {
            val map = HashMap<String, Any>()
            map["tagID"] = model?.tagID ?: 0
            map["consumCoin"] = consumCoin
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("enableStandTag", ControlType.CancelThis)) {
                battleServerApi.enableStandTag(body)
            }
            if (result.errno == 0) {
                goPlaybookMatch()
            } else {
                if (result.errno == ERROR_NETWORK_BROKEN) {
                    U.getToastUtil().showShort("网络异常，请检查网络后重试")
                    return@launch
                }
                if (result.errno == 8302552) {
                    tipsDialogView?.dismiss(false)
                    tipsDialogView = TipsDialogView.Builder(context)
                            .setMessageTip(result.errmsg)
                            .setConfirmTip("金币解锁")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    tipsDialogView?.dismiss()
                                    enableStandTag(true)
                                }
                            })
                            .setCancelBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    tipsDialogView?.dismiss()
                                }
                            })
                            .build()
                    tipsDialogView?.showByDialog()
                    return@launch
                }
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun getStandSongList() {
        launch {
            delay(200)
            val result = subscribe(RequestControl("getStandSongList", ControlType.CancelThis)) {
                battleServerApi.getStandSongList(MyUserInfoManager.getInstance().uid, model?.tagID
                        ?: 0)
            }
            if (result.errno == 0) {
                blightCnt = result.data.getIntValue("blightCnt")
                getSongCnt = result.data.getIntValue("getSongCnt")
                val starCnt = result.data.getIntValue("starCnt")
                val status = result.data.getIntValue("status")
                model?.starCnt = starCnt
                model?.status = status
                refreshView()
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
    }
}
