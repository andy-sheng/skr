package com.module.playways.mic.create.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
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
import com.module.playways.mic.room.MicRoomServerApi
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class MicRoomCreateFragment : BaseFragment() {
    lateinit var titlebar: CommonTitleBar
    lateinit var nameEdittext: NoLeakEditText
    lateinit var divider: View
    lateinit var enterRoomTip: ExTextView
    lateinit var allManTv: ExTextView
    lateinit var whiteGoldTv: ExTextView
    lateinit var yellowGoldTv: ExTextView
    lateinit var boGoldTv: ExTextView

    val raceRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    val selectDrawable = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    override fun initView(): Int {
        return R.layout.create_mic_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        nameEdittext = rootView.findViewById(R.id.name_edittext)
        divider = rootView.findViewById(R.id.divider)
        enterRoomTip = rootView.findViewById(R.id.enter_room_tip)
        allManTv = rootView.findViewById(R.id.all_man_tv)
        whiteGoldTv = rootView.findViewById(R.id.white_gold_tv)
        yellowGoldTv = rootView.findViewById(R.id.yellow_gold_tv)
        boGoldTv = rootView.findViewById(R.id.bo_gold_tv)

        titlebar.leftImageButton.setDebounceViewClickListener {
            activity?.finish()
        }

        titlebar.rightTextView.setDebounceViewClickListener {
            createRoom()
        }

        allManTv.setDebounceViewClickListener {
            resetSelect()
            allManTv.isSelected = true
        }

        whiteGoldTv.setDebounceViewClickListener {
            resetSelect()
            whiteGoldTv.isSelected = true
        }

        yellowGoldTv.setDebounceViewClickListener {
            resetSelect()
            yellowGoldTv.isSelected = true
        }

        boGoldTv.setDebounceViewClickListener {
            resetSelect()
            boGoldTv.isSelected = true
        }

        selectDrawable.bounds = Rect(0, 0, selectDrawable.intrinsicWidth, selectDrawable.intrinsicHeight)
        allManTv.setCompoundDrawables(selectDrawable, null, null, null)
        whiteGoldTv.setCompoundDrawables(selectDrawable, null, null, null)
        yellowGoldTv.setCompoundDrawables(selectDrawable, null, null, null)
        boGoldTv.setCompoundDrawables(selectDrawable, null, null, null)

        resetSelect()
        allManTv.isSelected = true
    }

    private fun createRoom() {
        launch {
            val map = mutableMapOf(
                    "" to ""
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("MicRoomCreateFragment createRoom", ControlType.CancelThis)) {
                raceRoomServerApi.createRoom(body)
            }

            if (result.errno == 0) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_ROOM)
                        .navigation()
                activity?.finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun resetSelect() {
        allManTv.isSelected = false
        whiteGoldTv.isSelected = false
        yellowGoldTv.isSelected = false
        boGoldTv.isSelected = false
    }
}