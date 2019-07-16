package com.module.playways.doubleplay.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel
import com.zq.live.proto.CombineRoom.EGameStage
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.Observable
import kotlinx.android.synthetic.main.double_game_sence_layout.view.*
import okhttp3.MediaType
import okhttp3.RequestBody


class DoubleGameSenceView : ExConstraintLayout {
    val mTag = "DoubleGameSenceView"
    val mShowCard: DoubleSingCardView
    val mMicIv: ExImageView
    val mPickIv: ImageView
    val mSelectIv: ImageView
    val mMicTv: TextView
    val mDoubleGameCardGroupView: DoubleGameCardGroupView
    var mPickFun: (() -> Unit)? = null
    var mRoomData: DoubleRoomData? = null
    var mGameStage: Int? = null
    var mPanelSeq: Int? = null
    var mItemId: Int? = null
    internal var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.double_game_sence_layout, this)
        background = U.getDrawable(R.drawable.srf_youxi_bj)
        mShowCard = findViewById(R.id.show_card)
        mMicIv = findViewById(R.id.mic_iv)
        mPickIv = findViewById(R.id.pick_iv)
        mSelectIv = findViewById(R.id.select_iv)
        mMicTv = findViewById(R.id.mic_tv)
        mDoubleGameCardGroupView = findViewById(R.id.card_group_view)
        mShowCard.mCutSongTv.text = "结束"
        mShowCard.mOnClickNextSongListener = {
            val mutableSet1 = mutableMapOf("roomID" to mRoomData!!.gameId, "panelSeq" to mPanelSeq)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
            ApiMethods.subscribe(mDoubleRoomServerApi.endGameCard(body), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    if (obj?.errno == 0) {
                        val panel = LocalGamePanelInfo.json2LocalModel(obj?.data.getJSONObject("nextPanel"))
                        showGamePanel(panel)
                    } else {
                        MyLog.w(mTag, "changeGamePanel faild，errno is ${obj?.errno}")
                    }
                }
            }, this@DoubleGameSenceView)
        }
        mShowCard.mNextSongTipTv.text = ""

        mPickIv.setOnClickListener {
            mPickFun?.invoke()
            pick_diffuse_view.start(2000)
        }

        mSelectIv.setDebounceViewClickListener {
            if (mGameStage == EGameStage.GS_ChoicGameItem.value) {
                U.getToastUtil().showShort("请结束游戏再换")
                return@setDebounceViewClickListener
            }

            val mutableSet1 = mutableMapOf("roomID" to mRoomData!!.gameId, "curPanelSeq" to mPanelSeq)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
            ApiMethods.subscribe(mDoubleRoomServerApi.changeGamePanel(body), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    if (obj?.errno == 0) {
                        val panel = LocalGamePanelInfo.json2LocalModel(obj?.data.getJSONObject("nextPanel"))
                        showGamePanel(panel)
                    } else {
                        MyLog.w(mTag, "changeGamePanel faild，errno is ${obj?.errno}")
                    }
                }
            }, this@DoubleGameSenceView)
        }

        mDoubleGameCardGroupView.mCard1.setDebounceViewClickListener {
            mDoubleGameCardGroupView.mCard1.itemId?.let {
                select(it)
            }
        }

        mDoubleGameCardGroupView.mCard2.setDebounceViewClickListener {
            mDoubleGameCardGroupView.mCard2.itemId?.let {
                select(it)
            }
        }

        mDoubleGameCardGroupView.mCard3.setDebounceViewClickListener {
            mDoubleGameCardGroupView.mCard3.itemId?.let {
                select(it)
            }
        }

        mDoubleGameCardGroupView.mCard4.setDebounceViewClickListener {
            mDoubleGameCardGroupView.mCard4.itemId?.let {
                select(it)
            }
        }
    }

    fun select(itemID: Int) {
        val mutableSet1 = mutableMapOf("itemID" to itemID, "panelSeq" to mPanelSeq, "roomID" to mRoomData?.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.choiceGameItem(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    MyLog.w(mTag, "选择游戏卡片成功，itemID is $itemID")
                } else {
                    U.getToastUtil().showShort(obj?.errmsg)
                }
            }
        }, this@DoubleGameSenceView)
    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    fun joinAgora() {
        MyLog.d("DoubleGameSenceView", "joinAgora")
        val drawable = DrawableCreator.Builder()
                .setSelectedDrawable(U.getDrawable(R.drawable.skr_jingyin_able))
                .setUnSelectedDrawable(U.getDrawable(R.drawable.srf_bimai))
                .build()
        mMicIv?.background = drawable

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected
                ZqEngineKit.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })
    }

    fun selected() {
        mMicIv?.setSelected(ZqEngineKit.getInstance().params.isLocalAudioStreamMute)
    }

    fun unselected() {
        pick_diffuse_view.visibility = View.GONE
    }

    fun setFirstGamePanelInfo(gamePanelInfo: LocalGamePanelInfo?) {
        MyLog.w(mTag, "setFirstGamePanelInfo is $gamePanelInfo")
        gamePanelInfo?.let {
            mGameStage = EGameStage.GS_ChoicGameItem.value
            mPanelSeq = gamePanelInfo.panelSeq
            mDoubleGameCardGroupView.setPanelInfo(it)
            mShowCard.visibility = View.GONE
            mDoubleGameCardGroupView.visibility = View.VISIBLE
        }
    }

    fun playGame(localGameItemInfo: LocalGameItemInfo?) {
        MyLog.w(mTag, "playGame is $localGameItemInfo")
        localGameItemInfo?.let {
            mGameStage = EGameStage.GS_InGamePlay.value
            mItemId = localGameItemInfo.itemID
            mShowCard.playLyric(localGameItemInfo)
            mShowCard.visibility = View.VISIBLE
            mDoubleGameCardGroupView.visibility = View.GONE
        }
    }

    fun showGamePanel(localGamePanelInfo: LocalGamePanelInfo?) {
        MyLog.w(mTag, "showGamePanel is $localGamePanelInfo")
        localGamePanelInfo?.let {
            mGameStage = EGameStage.GS_ChoicGameItem.value
            mPanelSeq = localGamePanelInfo.panelSeq
            mDoubleGameCardGroupView.setPanelInfo(it)
            mShowCard.visibility = View.GONE
            mDoubleGameCardGroupView.visibility = View.VISIBLE
        }
    }

    fun updateLockState() {
        mShowCard?.updateLockState()
    }

    //这个是有人选择的某一个游戏卡片
    fun changeChoiceGameState(userInfoModel: UserInfoModel, panelSeq: Int, itemID: Int) {
        if (mGameStage == EGameStage.GS_ChoicGameItem.value && panelSeq == mPanelSeq) {
            mDoubleGameCardGroupView.updateSelectState(userInfoModel, panelSeq, itemID)
        } else {
            MyLog.w(mTag, "changeChoiceGameState failed panelSeq is $panelSeq, itemID is $itemID, local stage is $mGameStage, panelSeq is $mPanelSeq")
        }
    }

    fun setData(localGameSenceDataModel: LocalGameSenceDataModel) {
        if (localGameSenceDataModel.gameStage == EGameStage.GS_InGamePlay.value) {
            mGameStage = EGameStage.GS_InGamePlay.value
            mShowCard.visibility = View.VISIBLE
            mDoubleGameCardGroupView.visibility = View.GONE
            if (mItemId != localGameSenceDataModel.itemID) {
                getGameItemInfo(localGameSenceDataModel.itemID)
            }
        } else if (localGameSenceDataModel.gameStage == EGameStage.GS_ChoicGameItem.value) {
            mGameStage = EGameStage.GS_ChoicGameItem.value
            mShowCard.visibility = View.GONE
            mDoubleGameCardGroupView.visibility = View.VISIBLE
            if (mPanelSeq != localGameSenceDataModel.panelSeq) {
                getGamePanelInfo(localGameSenceDataModel.panelSeq)
            }
        }
    }

    private fun getGameItemInfo(itemID: Int) {
        Observable.create<Any> {
            ApiMethods.subscribe(mDoubleRoomServerApi.getGameItemInfo(mRoomData!!.gameId, mPanelSeq
                    ?: 0, itemID), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    it.onComplete()
                    if (obj?.errno == 0) {
                        mItemId = itemID
                        val localGameItemInfo = JSON.parseObject(obj.data.getString("item"), LocalGameItemInfo::class.java)
                        playGame(localGameItemInfo)
                    } else {
                        MyLog.w(mTag, "getGameItemInfo faild, errno is ${obj?.errno}")
                    }
                }

                override fun onError(e: Throwable) {
                    it.onError(e)
                }

                override fun onNetworkError(errorType: ErrorType?) {
                    it.onError(Throwable("网络错误"))
                }
            }, this@DoubleGameSenceView, ApiMethods.RequestControl("getGameItemInfo", ApiMethods.ControlType.CancelThis))
        }.compose(bindDetachEvent()).retryWhen(RxRetryAssist(10, "")).subscribe()
    }

    private fun getGamePanelInfo(panelSeq: Int) {
        Observable.create<Any> {
            ApiMethods.subscribe(mDoubleRoomServerApi.getGamePanelInfo(mRoomData!!.gameId, panelSeq), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    it.onComplete()
                    if (obj?.errno == 0) {
                        val panel = LocalGamePanelInfo.json2LocalModel(obj?.data.getJSONObject("panel"))
                        showGamePanel(panel)
                        mPanelSeq = panelSeq
                    } else {
                        MyLog.w(mTag, "getGamePanelInfo faild, errno is ${obj?.errno}")
                    }
                }

                override fun onError(e: Throwable) {
                    it.onError(Throwable("网络错误"))
                }

                override fun onNetworkError(errorType: ErrorType?) {
                    it.onError(Throwable("网络延迟"))
                }
            }, this@DoubleGameSenceView, ApiMethods.RequestControl("getGamePanelInfo", ApiMethods.ControlType.CancelThis))
        }.compose(bindDetachEvent()).retryWhen(RxRetryAssist(10, "")).subscribe()
    }

    fun destroy() {

    }
}