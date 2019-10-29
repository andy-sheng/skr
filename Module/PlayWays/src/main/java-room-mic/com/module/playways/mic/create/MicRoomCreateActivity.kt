package com.module.playways.mic.create

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.MicRoomServerApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_CREATE_MIC_ROOM)
class MicRoomCreateActiviy : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var nameEdittext: NoLeakEditText
    lateinit var divider: View
    lateinit var enterRoomTip: ExTextView
    lateinit var allManTv: ExTextView
    lateinit var whiteGoldTv: ExTextView
    lateinit var yellowGoldTv: ExTextView
    lateinit var boGoldTv: ExTextView

    val raceRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)
    var selected: Level = Level.RLL_All

    val selectDrawable1 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    val selectDrawable2 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    val selectDrawable3 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    val selectDrawable4 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.create_mic_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = this.findViewById(R.id.titlebar)
        nameEdittext = this.findViewById(R.id.name_edittext)
        divider = this.findViewById(R.id.divider)
        enterRoomTip = this.findViewById(R.id.enter_room_tip)
        allManTv = this.findViewById(R.id.all_man_tv)
        whiteGoldTv = this.findViewById(R.id.white_gold_tv)
        yellowGoldTv = this.findViewById(R.id.yellow_gold_tv)
        boGoldTv = this.findViewById(R.id.bo_gold_tv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        titlebar.rightTextView.setDebounceViewClickListener {
            createRoom()
        }

        allManTv.setDebounceViewClickListener {
            resetSelect()
            allManTv.isSelected = true
            selected = Level.RLL_All
        }

        whiteGoldTv.setDebounceViewClickListener {
            resetSelect()
            whiteGoldTv.isSelected = true
            selected = Level.RLL_Bai_Yin
        }

        yellowGoldTv.setDebounceViewClickListener {
            resetSelect()
            yellowGoldTv.isSelected = true
            selected = Level.RLL_Huang_Jin
        }

        boGoldTv.setDebounceViewClickListener {
            resetSelect()
            boGoldTv.isSelected = true
            selected = Level.RLL_Bo_Jin
        }

        selectDrawable1.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        selectDrawable2.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        selectDrawable3.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        selectDrawable4.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        allManTv.setCompoundDrawables(selectDrawable1, null, null, null)
        whiteGoldTv.setCompoundDrawables(selectDrawable2, null, null, null)
        yellowGoldTv.setCompoundDrawables(selectDrawable3, null, null, null)
        boGoldTv.setCompoundDrawables(selectDrawable4, null, null, null)

        resetSelect()
        allManTv.isSelected = true
        getPermmissionList(0)
    }

    private fun getPermmissionList(loop: Int) {
        launch {
            val result = subscribe(RequestControl("MicRoomCreateFragment getPermmissionList", ControlType.CancelThis)) {
                raceRoomServerApi.getRoomPermmissionList()
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("list"), Level::class.java)
                list?.let {
                    it.forEach {
                        when (it) {
                            Level.RLL_All, Level.RLL_Qing_Tong -> allManTv.visibility = View.VISIBLE
                            Level.RLL_Bai_Yin -> whiteGoldTv.visibility = View.VISIBLE
                            Level.RLL_Huang_Jin -> yellowGoldTv.visibility = View.VISIBLE
                            Level.RLL_Bo_Jin -> boGoldTv.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                if (loop < 5) {
                    delay(300)
                    var l = loop
                    getPermmissionList(++l)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    private fun createRoom() {
        launch {
            val map = mutableMapOf(
                    "levelLimit" to selected.value,
                    "roomName" to nameEdittext.text.toString()
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("MicRoomCreateFragment createRoom", ControlType.CancelThis)) {
                raceRoomServerApi.createRoom(body)
            }

            if (result.errno == 0) {
                var rsp = JSON.parseObject(result.data.toString(), JoinMicRoomRspModel::class.java)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_ROOM)
                        .withSerializable("JoinMicRoomRspModel", rsp)
                        .navigation()
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    private fun resetSelect() {
        allManTv.isSelected = false
        whiteGoldTv.isSelected = false
        yellowGoldTv.isSelected = false
        boGoldTv.isSelected = false
    }

    enum class Level(val level: Int) {
        RLL_All(0), RLL_Qing_Tong(1), RLL_Bai_Yin(2), RLL_Huang_Jin(3), RLL_Bo_Jin(4);

        val value: Int
            get() = level
    }
}
