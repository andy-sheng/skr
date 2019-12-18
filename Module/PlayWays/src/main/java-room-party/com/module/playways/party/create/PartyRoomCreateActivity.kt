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
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
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

    val skrAudioPermission = SkrAudioPermission()

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
            titlebar.rightTextView.isClickable = false
            if (this.from == "create") {
                skrAudioPermission.ensurePermission({ createRoom() }, true)
            } else if (this.from == "change") {
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
                val length = str.length
                if (length > 15) {
                    MyLog.d(TAG, "onTextChanged s = $str")
                    val selectIndex = preString.length
                    nameEdittext.setText(preString)
                    nameEdittext.setSelection(selectIndex)
                    U.getToastUtil().showShort("不能超过15个文字")
                }
            }
        })

        selectDrawable1.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        selectDrawable2.bounds = Rect(0, 0, selectDrawable1.intrinsicWidth, selectDrawable1.intrinsicHeight)
        allManTv.setCompoundDrawables(selectDrawable1, null, null, null)
        onlyInviteTv.setCompoundDrawables(selectDrawable2, null, null, null)
        trySelect(2)

        if ("change".equals(from)) {
            nameEdittext.setText(H.partyRoomData?.topicName)

            trySelect(H.partyRoomData?.enterPermission ?: 2)
        }
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

    var mTipsDialogView: TipsDialogView? = null

    private fun createRoom() {
        var topicName = nameEdittext.text.toString().trim()
        if (TextUtils.isEmpty(topicName)) {
            U.getToastUtil().showShort("房间主题不可以为空")
            return
        }

        launch {
            val map = mutableMapOf(
                    "enterPermission" to enterType,
                    "topicName" to topicName
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("createRoom", ControlType.CancelThis)) {
                roomServerApi.createRoom(body)
            }
            titlebar.rightTextView.isClickable = true
            if (result.errno == 0) {
                var rsp = JSON.parseObject(result.data.toString(), JoinPartyRoomRspModel::class.java)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_ROOM)
                        .withSerializable("JoinPartyRoomRspModel", rsp)
                        .navigation()
                finish()
            } else if (8436006 == result.errno) {
                if (MyUserInfoManager.myUserInfo?.vipInfo == null) {
                    mTipsDialogView = TipsDialogView.Builder(this@PartyRoomCreateActivity)
                            .setMessageTip("开通VIP特权，立即获得创建权限")
                            .setConfirmTip("立即开通")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    mTipsDialogView?.dismiss(false)
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                            .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/vip?title=1"))
                                            .greenChannel().navigation()
                                }
                            })
                            .setCancelBtnClickListener {
                                mTipsDialogView?.dismiss()
                            }
                            .build()
                    mTipsDialogView?.showByDialog(true)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun changeRoomSetting() {
        launch {
            var topicName = nameEdittext.text.toString().trim()
//            if(TextUtils.isEmpty(topicName)){
//                topicName = "${MyUserInfoManager.nickName}的派对"
//            }

            if (topicName.equals(H.partyRoomData?.topicName) && enterType == H.partyRoomData?.enterPermission) {
                finish()
            } else {
                val map = mutableMapOf(
                        "enterPermission" to enterType,
                        "roomID" to H.partyRoomData?.gameId,
                        "topicName" to topicName
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("changeRoom", ControlType.CancelThis)) {
                    roomServerApi.changeRoomInfo(body)
                }
                if (result.errno == 0) {
                    U.getToastUtil().showShort("房间信息修改成功")
                    finish()
                } else {
                    titlebar.rightTextView.isClickable = true
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        skrAudioPermission.onBackFromPermisionManagerMaybe(this)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
