package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PBeginDiamondboxModel
import com.module.playways.party.room.model.PartyDiamondboxModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.PBeginDiamondbox
import com.zq.live.proto.broadcast.PartyDiamondbox
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

/**
 * 宝箱发送页面
 */
class PartySendDiamondBoxDialogView : ConstraintLayout{

    private val TAG = PartySendDiamondBoxDialogView::class.java.simpleName

    private var mDiamondBoxPriceTv:ExTextView? = null
    private var mDiamondBoxSenderBtn:ExImageView? = null
    private var mNickNameTv: ExTextView? = null
    private var mCancelTv:ExTextView? = null

    private var mDialogPlus: DialogPlus? = null

    private val mPartyRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    private var mRoomID:Int?= null


    constructor(context: Context?) : super(context){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }


    private fun init() {
        View.inflate(context, R.layout.party_send_diamond_box_view_layout, this)

        mDiamondBoxPriceTv = findViewById(R.id.diamond_box_sender_price)
        mDiamondBoxSenderBtn = findViewById(R.id.diamond_box_sender_img)
        mNickNameTv = findViewById(R.id.diamond_box_sender_nick_name)
        mCancelTv = findViewById(R.id.title_cancel_tv)

        mNickNameTv?.text = "${MyUserInfoManager.nickName}的宝箱"
        mCancelTv?.setDebounceViewClickListener {
            hide()
        }

        // medium 字体效果
        mDiamondBoxPriceTv?.paint?.isFakeBoldText = true
        mDiamondBoxSenderBtn?.setDebounceViewClickListener {
            U.getToastUtil().showShort("正在加载宝箱，请稍后...")
        }

        MainScope().launch {
            val apiResult = subscribe(RequestControl("getDiamondBoxList", ControlType.CancelThis)){
                mPartyRoomServerApi.getDiamondBoxList(mRoomID!!, MyUserInfoManager.uid, MyUserInfoManager.nickName)
            }

            if(apiResult.errno == 0){
                val confs = apiResult.data.getJSONArray("confs")
                val conf = confs.getJSONObject(0)

                MyLog.e(TAG, "成功获取宝箱配置 $confs")
                mDiamondBoxPriceTv?.text = conf.getString("zsCnt")
                mDiamondBoxSenderBtn?.setDebounceViewClickListener {
                    sendDiamondBox(mRoomID!!, conf.getString("diamondboxID"))
                }
            }else{
                U.getToastUtil().showShort("宝箱配置获取失败！")
                MyLog.e(TAG, "宝箱配置获取失败 ${apiResult.errno} ${apiResult.errmsg}")
            }
        }
    }

    private fun sendDiamondBox(roomID:Int, diamondboxID:String){
        MyLog.d(TAG, "发送宝箱 $roomID $diamondboxID")

        val map = mutableMapOf("roomID" to mRoomID, "diamondboxID" to diamondboxID)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        MainScope().launch {
            val apiResult = subscribe (RequestControl("beginDiamondBox", ControlType.CancelThis)){
                mPartyRoomServerApi.beginDiamondBox(body)
            }

            if(apiResult.errno == 0){
                val box = apiResult.data.getString("pBeginDiamondbox")
                val pBeginDiamondbox = JSON.parseObject(box, PBeginDiamondboxModel::class.java)
                val partyDiamondbox = PartyDiamondboxModel().let {
                    it.roomID = mRoomID
                    it.pBeginDiamondbox = pBeginDiamondbox
                    it
                }

                EventBus.getDefault().post(partyDiamondbox)
                MyLog.e(TAG, "成功发送宝箱 $pBeginDiamondbox")

                hide()
            }else{
                MyLog.e(TAG, "发送宝箱失败 ${apiResult.errno} ${apiResult.errmsg}")
                U.getToastUtil().showShort(apiResult.errmsg)
            }
        }
    }

    fun show(context: Context, roomID:Int){
        MyLog.d(TAG, "显示了发送宝箱弹窗")
        mRoomID = roomID

        mDialogPlus?.dismiss()
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentHeight(U.getDisplayUtils().screenHeight)
                .setPadding(0, 0, 0, 0)
                .setMargin(0, 0, 0, 0)
                .setExpanded(false)
                .setCancelable(true)
                .create()

        mDialogPlus?.show()
    }

    fun hide(){
        mDialogPlus?.dismiss()
    }

}