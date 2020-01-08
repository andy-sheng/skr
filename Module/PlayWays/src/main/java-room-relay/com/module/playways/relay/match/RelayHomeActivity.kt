package com.module.playways.relay.match

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongAdapter
import com.module.playways.room.song.fragment.SongSelectFragment
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.AddSongEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_RELAY_HOME)
class RelayHomeActivity : BaseActivity() {

    val adapter: RelayHomeSongAdapter = RelayHomeSongAdapter()
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)

    //在滑动到最后的时候自动加载更多
    var skrAudioPermission = SkrAudioPermission()
    var mTipsDialogView: TipsDialogView? = null

    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, SongSelectFragment::class.java)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_RELAY_HOME)
                .setHasAnimation(false)
                .build())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        // 过滤一下点歌的来源
        if (event.from == SongManagerActivity.TYPE_FROM_RELAY_HOME) {
            StatisticsAdapter.recordCountEvent("chorus", "start", null)
            goMatch(event.songModel)
        }
    }

    fun goMatch(model: SongModel?) {
        // 先跳到匹配页面发起匹配
        model?.let {
            launch {
                val result = subscribe(RequestControl("checkEnterPermission", ControlType.CancelThis)) {
                    relayMatchServerApi.checkEnterPermission()
                }
                if (result.errno == 0) {
                    skrAudioPermission.ensurePermission({
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_RELAY_MATCH)
                                .withSerializable("songModel", model)
                                .navigation()
                    }, true)
                } else {
                    if (result.errno == 8343059) {
                        // 多次恶意退出
                        mTipsDialogView?.dismiss(false)
                        mTipsDialogView = TipsDialogView.Builder(this@RelayHomeActivity)
                                .setMessageTip(result.errmsg)
                                .setOkBtnTip("我知道了")
                                .setOkBtnClickListener {
                                    mTipsDialogView?.dismiss(false)
                                }
                                .build()
                        mTipsDialogView?.showByDialog()
                    } else {
                        U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }

        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        skrAudioPermission.onBackFromPermisionManagerMaybe(this)
    }


    override fun destroy() {
        super.destroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}
