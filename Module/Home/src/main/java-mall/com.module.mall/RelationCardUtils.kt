package com.module.mall

import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.*
import com.common.utils.U
import com.dialog.view.TipsDialogView
import com.module.ModuleServiceManager
import com.trello.rxlifecycle2.android.ActivityEvent
import io.reactivex.functions.Consumer
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.lang.ref.WeakReference
import java.util.*

class RelationCardUtils {
    private val rankedServerApi = ApiManager.getInstance().createService(MallServerApi::class.java)
    private var tipsDialogView: TipsDialogView? = null

    fun checkRelation(userInfoModel: UserInfoModel, weakReference: WeakReference<BaseActivity>?, goodsID: Int, packageModelID: String) {
        val map = mutableMapOf("goodsID" to goodsID, "otherUserID" to userInfoModel.userId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(rankedServerApi.checkCardRelation(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val msg = result.data!!.getString("noticeMsg")
                    showInviteCardDialog(userInfoModel, msg, weakReference, packageModelID)
                } else {
                    //ErrAlreadyHasRelation           = 8428114; //对方已是你的闺蜜，不能再发送邀请哦～
                    //ErrApplyAfter24Hour             = 8428115; //24小时之后才能再次发送关系申请哦～
                    //ErrAlreadyHasOtherRelation      = 8428116; //对方已是你的闺蜜，对方接受邀请将自动解除你们原来的关系哦～
                    if (8428114 == result.errno) {
                        U.getToastUtil().showShort(result.errmsg)
                    } else if (8428115 == result.errno) {
                        U.getToastUtil().showShort(result.errmsg)
                    } else if (8428116 == result.errno) {
                        showInviteCardDialog(userInfoModel, result.errmsg, weakReference, packageModelID)
                    }
                }
            }
        }, RequestControl("checkCardRelation", ControlType.CancelLast))

        weakReference?.get()?.provideLifecycleSubject()?.subscribe(Consumer<ActivityEvent> { activityEvent ->
            if (activityEvent == ActivityEvent.DESTROY) {
                if (tipsDialogView != null) {
                    tipsDialogView?.dismiss(false)
                    tipsDialogView = null
                }
            }
        })
    }

    /**
     * 邀请好友发生关系
     *
     * @param userInfoModel
     */
    private fun showInviteCardDialog(userInfoModel: UserInfoModel, msg: String, weakReference: WeakReference<BaseActivity>?, packageModelID: String) {
        if (tipsDialogView != null) {
            tipsDialogView?.dismiss(false)
            tipsDialogView = null
        }

        weakReference?.get()?.let {
            tipsDialogView = TipsDialogView.Builder(it)
                    .setMessageTip(msg)
                    .setCancelBtnClickListener {
                        if (tipsDialogView != null) {
                            tipsDialogView?.dismiss(false)
                        }
                        tipsDialogView = null
                    }
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        weakReference?.get()?.finish()
                        tipsDialogView = null
                        inviteToRelation(userInfoModel.userId, packageModelID, weakReference)
                    }
                    .setConfirmTip("邀请")
                    .build()
            tipsDialogView?.showByDialog()
        }

    }

    //亲故发生某种关系
    private fun inviteToRelation(userID: Int, packageModelID: String, weakReference: WeakReference<BaseActivity>?) {
        weakReference?.get()?.let {
            it.launch {
                val map = HashMap<String, Any>()
                map["packetItemID"] = packageModelID
                map["toUserID"] = userID

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val obj = subscribe {
                    rankedServerApi.useGoods(body)
                }

                if (obj.errno == 0) {
                    U.getToastUtil().showShort("邀请成功")
                    // 生成一条邀请IM消息
                    var msgService = ModuleServiceManager.getInstance().msgService
                    msgService?.sendRelationInviteMsg(userID.toString(), obj.data.getString("uniqID"), obj.data.getString("msgContent"))
                } else {
                    U.getToastUtil().showShort(obj.errmsg)
                }
            }
        }
    }

    interface UsePacketListener {
        fun useFinish(success: Boolean)
    }
}
