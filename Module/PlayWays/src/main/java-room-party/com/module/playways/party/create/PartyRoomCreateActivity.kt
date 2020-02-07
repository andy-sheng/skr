package com.module.playways.party.create

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
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
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.component.person.view.CommonTagView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.party.create.model.PartyCreateRecommendGameModel
import com.module.playways.party.create.view.GameTagView
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_CREATE_PARTY_ROOM)
class PartyRoomCreateActivity : BaseActivity(), View.OnTouchListener {
    lateinit var titlebar: CommonTitleBar
    lateinit var nameEdittext: NoLeakEditText
    lateinit var divider: View
    lateinit var enterRoomTip: ExTextView
    lateinit var allManTv: ExTextView
    lateinit var onlyInviteTv: ExTextView
    lateinit var directMicTv: ExTextView
    lateinit var applicationMicTv: ExTextView
    lateinit var divider3: View
    lateinit var gameTip: ExTextView
    lateinit var gameTagView: GameTagView

    val gameList = ArrayList<PartyCreateRecommendGameModel>()

    var from: String = "create"

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    var enterType = 2 // 1 只有邀请 2 无限制
    var micType = 1 // EGSM_NEED_APPLY = 0 : 需要申请上麦 - EGSM_NO_APPLY = 1 : 不需要申请上麦

    val skrAudioPermission = SkrAudioPermission()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.create_party_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
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
        directMicTv = this.findViewById(R.id.direct_mic_tv)
        applicationMicTv = this.findViewById(R.id.application_mic_tv)
        divider3 = this.findViewById(R.id.divider_3)
        gameTip = this.findViewById(R.id.game_tip)
        gameTagView = this.findViewById(R.id.game_tag_view)

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

        directMicTv.setDebounceViewClickListener {
            trySelectMicType(1)
        }
        applicationMicTv.setDebounceViewClickListener {
            trySelectMicType(0)
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
        nameEdittext.setOnTouchListener(this);

        getSelectDrawable().let {
            it.bounds = Rect(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            allManTv.setCompoundDrawables(it, null, null, null)
        }

        getSelectDrawable().let {
            it.bounds = Rect(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            onlyInviteTv.setCompoundDrawables(it, null, null, null)
        }

        getSelectDrawable().let {
            it.bounds = Rect(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            directMicTv.setCompoundDrawables(it, null, null, null)
        }

        getSelectDrawable().let {
            it.bounds = Rect(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            applicationMicTv.setCompoundDrawables(it, null, null, null)
        }

        trySelect(2)
        trySelectMicType(1)

        if ("change".equals(from)) {
            gameTagView.visibility = View.GONE
            gameTip.visibility = View.GONE
            divider3.visibility = View.GONE
            nameEdittext.setText(H.partyRoomData?.topicName)

            trySelect(H.partyRoomData?.enterPermission ?: 2)
        } else {
            getRecommendGameList()
        }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
        if ((view?.getId() == R.id.name_edittext && canVerticalScroll(nameEdittext))) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (motionEvent?.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false
    }

    private fun canVerticalScroll(editText: EditText): Boolean {
        //滚动的距离
        val scrollY = editText.getScrollY();
        //控件内容的总高度
        val scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        val scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        val scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }


    private fun getSelectDrawable(): Drawable {
        return DrawableCreator.Builder()
                .setSelectedDrawable(U.getDrawable(R.drawable.chuangjian_xuanzhong))
                .setUnSelectedDrawable(U.getDrawable(R.drawable.chuangjian_weixuanzhong))
                .build()
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

    private fun trySelectMicType(micType: Int) {
        this.micType = micType
        if (micType == 1) {
            directMicTv.isSelected = true
            applicationMicTv.isSelected = false
        } else if (micType == 0) {
            directMicTv.isSelected = false
            applicationMicTv.isSelected = true
        }
    }

    var mTipsDialogView: TipsDialogView? = null

    private fun createRoom() {
        StatisticsAdapter.recordCountEvent("party", "create", null)
        var ageStage = gameTagView.getSelectTag()
        if (ageStage == 0) {
            U.getToastUtil().showShort("请选择一个游戏")
            titlebar.rightTextView.isClickable = true
            return
        }

        var topicName = nameEdittext.text.toString().trim()
        var model: PartyCreateRecommendGameModel? = null

        gameList?.forEach {
            if (ageStage == it.ruleID) {
                model = it
                return@forEach
            }
        }

        launch {
            val map = mutableMapOf(
                    "enterPermission" to enterType,
                    "gameMode" to model?.gameMode,
                    "getSeatMode" to micType,
                    "playID" to model?.playID,
                    "ruleID" to model?.ruleID,
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
            } else if (8436006 == result.errno) {
                //因为创建成功就把这个界面finish了，所以只有在失败的时候isClickable = true就可以了
                titlebar.rightTextView.isClickable = true
                if (MyUserInfoManager.myUserInfo?.vipInfo == null) {
                    mTipsDialogView = TipsDialogView.Builder(this@PartyRoomCreateActivity)
                            .setMessageTip("开通VIP特权，立即获得创建权限")
                            .setConfirmTip("立即开通")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                override fun click(view: View) {
                                    mTipsDialogView?.dismiss(false)
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                            .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
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

    private fun getRecommendGameList() {
        launch {
            repeatUntil(5) {
                val map = mutableMapOf(
                        "cnt" to 20
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("createRoom", ControlType.CancelThis)) {
                    roomServerApi.getRecommendGameList(body)
                }

                if (result.errno == 0) {
                    val list = JSON.parseArray(result.data.getString("items"), PartyCreateRecommendGameModel::class.java)
                    gameList.addAll(list)
                    val tagList = ArrayList<CommonTagView.TagModel>();
                    list?.forEach {
                        val model = CommonTagView.TagModel(it.ruleID, it.gameDesc)
                        tagList.add(model)
                    }

                    gameTagView.bindData(tagList)

                    false
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                    true
                }
            }
        }
    }

    inline fun repeatUntil(times: Int, needRepeat: (Int) -> Boolean) {
        for (index in 0..times - 1) {
            if (!needRepeat(index)) break
        }
    }

    private fun changeRoomSetting() {
        var topicName = nameEdittext.text.toString().trim()

        if (TextUtils.isEmpty(topicName)) {
            U.getToastUtil().showShort("房间主题不可以为空")
            return
        }

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
