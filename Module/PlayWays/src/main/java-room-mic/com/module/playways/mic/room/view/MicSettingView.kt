package com.module.playways.mic.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.MicRoomServerApi
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

// 右边操作区域，投票
class MicSettingView : ExConstraintLayout {

    val mTag = "MicSettingView"

    private var mDialogPlus: DialogPlus? = null

    // 清唱与伴奏
    private var mSbAcc: SwitchButton? = null

    var mRoomData: MicRoomData? = null

    internal var mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    init {
        View.inflate(context, R.layout.mic_setting_view_layout, this)
        mSbAcc = findViewById(R.id.sb_acc)

        mSbAcc?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            //EMMS_UNKNOWN = 0 : 未知 - EMMS_OPEN = 1 : match 打开 - EMMS_CLOSED = 2 : match 关闭
            launch {
                val map = mutableMapOf(
                        "roomID" to mRoomData?.gameId,
                        "matchStatus" to (if (isChecked) 2 else 1)
                )

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("$mTag matchStatus", ControlType.CancelLast)) {
                    mRoomServerApi.changeMatchStatus(body)
                }

                if (result.errno == 0) {

                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        })

        setOnClickListener {
            //拦截
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
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.transparent)
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
