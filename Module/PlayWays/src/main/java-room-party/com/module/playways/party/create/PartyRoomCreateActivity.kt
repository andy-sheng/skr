package com.module.playways.party.create

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.MicRoomServerApi
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.PartyRoomServerApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_CREATE_PARTY_ROOM)
class PartyRoomCreateActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var nameEdittext: NoLeakEditText
    lateinit var divider: View
    lateinit var enterRoomTip: ExTextView
    lateinit var allManTv: ExTextView
    lateinit var whiteGoldTv: ExTextView
    lateinit var yellowGoldTv: ExTextView
    lateinit var boGoldTv: ExTextView

    var levelList: ArrayList<Level> = ArrayList()

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

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
        levelList.add(Level.RLL_All)
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
            trySelect(Level.RLL_All, allManTv)
        }

        whiteGoldTv.setDebounceViewClickListener {
            trySelect(Level.RLL_Bai_Yin, whiteGoldTv)
        }

        yellowGoldTv.setDebounceViewClickListener {
            trySelect(Level.RLL_Huang_Jin, yellowGoldTv)
        }

        boGoldTv.setDebounceViewClickListener {
            trySelect(Level.RLL_Bo_Jin, boGoldTv)
        }

        nameEdittext.addTextChangedListener(object : TextWatcher {
            var preString = ""
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                preString = s.toString()
                MyLog.d(TAG, "beforeTextChanged s = $preString")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString()
                val length = U.getStringUtils().getStringLength(str)
                if (length > 16) {
                    MyLog.d(TAG, "onTextChanged s = $str")
                    val selectIndex = preString.length
                    nameEdittext.setText(preString)
                    nameEdittext.setSelection(selectIndex)
                    U.getToastUtil().showShort("昵称不能超过8个汉字或16个英文")
                }
            }
        })

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

    private fun trySelect(level: Level, textView: ExTextView) {
        if (levelList.contains(level)) {
            resetSelect()
            textView.isSelected = true
            selected = level
        } else {
            U.getToastUtil().showShort("段位还没有达到哦～")
        }
    }

    private fun getPermmissionList(loop: Int) {
        launch {
            val result = subscribe(RequestControl("getPermmissionList", ControlType.CancelThis)) {
                roomServerApi.getRoomPermmissionList()
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("list"), Level::class.java)
                levelList.clear()
                levelList.addAll(list)
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
                    "enterType" to "ET_UNKNOWN",
                    "roomName" to nameEdittext.text.toString(),
                    "topicName" to nameEdittext.text.toString()
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("createRoom", ControlType.CancelThis)) {
                roomServerApi.createRoom(body)
            }

            if (result.errno == 0) {
                var rsp = JSON.parseObject(result.data.toString(), JoinPartyRoomRspModel::class.java)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_ROOM)
                        .withSerializable("JoinPartyRoomRspModel", rsp)
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
