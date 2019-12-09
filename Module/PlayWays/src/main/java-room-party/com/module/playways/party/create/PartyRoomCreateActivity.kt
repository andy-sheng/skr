package com.module.playways.party.create

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
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
    lateinit var onlyInviteTv: ExTextView

    var from: String = "create"

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    var enterType = 2 // 1 只有邀请 2 无限制

    val selectDrawable1 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()

    val selectDrawable2 = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
            .build()


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.create_party_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = this.findViewById(R.id.titlebar)
        val from = intent.getStringExtra("from")
        if (!TextUtils.isEmpty(from)) {
            this.from = from
            if ("change" == from) {
                titlebar.rightTextView.text = "更改"
            }
        }

        nameEdittext = this.findViewById(R.id.name_edittext)
        divider = this.findViewById(R.id.divider)
        enterRoomTip = this.findViewById(R.id.enter_room_tip)
        allManTv = this.findViewById(R.id.all_man_tv)
        onlyInviteTv = this.findViewById(R.id.only_invite_tv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        titlebar.rightTextView.setDebounceViewClickListener {
            if(this.from == "create"){
                createRoom()
            }else{
                changeRoomSetting()
            }
        }

        allManTv.setDebounceViewClickListener {
            trySelect(2)
        }
        onlyInviteTv.setDebounceViewClickListener {
            trySelect(1)
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
        allManTv.setCompoundDrawables(selectDrawable1, null, null, null)
        onlyInviteTv.setCompoundDrawables(selectDrawable2, null, null, null)
        trySelect(2)
    }

    private fun trySelect(enterType: Int) {
        this.enterType = enterType
        if (enterType == 2) {
            allManTv.isSelected = true
            onlyInviteTv.isSelected = false
        } else if (enterType == 1) {
            allManTv.isSelected = false
            onlyInviteTv.isSelected = true
        }
    }

    private fun createRoom() {
        launch {
            var topicName = nameEdittext.text.toString()
//            if(TextUtils.isEmpty(topicName)){
//                topicName = "${MyUserInfoManager.nickName}的派对"
//            }
            val map = mutableMapOf(
                    "enterPermission" to enterType,
                    "topicName" to topicName
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

    private fun changeRoomSetting() {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
