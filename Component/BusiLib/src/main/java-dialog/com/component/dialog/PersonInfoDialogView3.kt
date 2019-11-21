package com.component.dialog

import android.content.Context
import android.graphics.Color
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.callback.Callback

import com.common.core.account.UserAccountManager
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.notification.event.FollowNotifyEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.R
import com.component.busilib.view.NickNameView
import com.component.busilib.view.MarqueeTextView
import com.component.level.utils.LevelConfigUtils
import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.component.person.view.PersonMoreOpView

import org.greenrobot.eventbus.EventBus
import com.component.person.view.GuardView
import com.component.person.view.PersonTagView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PersonInfoDialogView3 internal constructor(val mContext: Context, userID: Int, showKick: Boolean, showInvite: Boolean) : RelativeLayout(mContext) {

    val TAG = "PersonInfoDialogView3"

    private val moreIv: ImageView
    private val avatarIv: SimpleDraweeView
    private val levelBg: ImageView
    private val nameView: NickNameView
    private val signTv: MarqueeTextView
    private val verifyTv: TextView
    private val qinmiTv: TextView

    private val personTagView: PersonTagView
    private val guardView: GuardView
    private val photoViewBg: ExImageView
    private val photoView: RecyclerView
    private val photoNumTv: ExTextView
    private val divider: View
    private val inviteIv: ExTextView
    private val followIv: ExTextView

    private var mUserId: Int = 0
    private var mUserInfoModel = UserInfoModel()
    private var isShowKick: Boolean = false
    private var isShowInvite: Boolean = false

    private var clickListener: PersonInfoDialog.PersonCardClickListener? = null

    var mPersonMoreOpView: PersonMoreOpView? = null

    private val mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private var photoAdapter: PhotoAdapter? = null
    private var mOffset = 0
    private var mHasMore = false
    private var DEFAULT_CNT = 10

    // 未关注
    private val mUnFollowDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFC15B"))
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .build()

    // 已关注 或 互关
    private val mFollowDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#DB8800"))
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .build()

    init {
        View.inflate(context, R.layout.person_info3_card_view_layout, this)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        moreIv = this.findViewById(R.id.more_iv)
        avatarIv = this.findViewById(R.id.avatar_iv)
        levelBg = this.findViewById(R.id.level_bg)
        nameView = this.findViewById(R.id.name_view)
        signTv = this.findViewById(R.id.sign_tv)
        verifyTv = this.findViewById(R.id.verify_tv)

        qinmiTv = this.findViewById(R.id.qinmi_tv)

        personTagView = this.findViewById(R.id.person_tag_view)
        guardView = this.findViewById(R.id.guard_view)
        photoViewBg = this.findViewById(R.id.photo_view_bg)
        photoView = this.findViewById(R.id.photo_view)
        photoNumTv = this.findViewById(R.id.photo_num_tv)
        divider = this.findViewById(R.id.divider)
        inviteIv = this.findViewById(R.id.invite_iv)
        followIv = this.findViewById(R.id.follow_iv)

        initData(userID, showKick, showInvite)

        avatarIv.setDebounceViewClickListener { clickListener?.onClickAvatar(mUserInfoModel.avatar) }
        levelBg.setDebounceViewClickListener { clickListener?.onClickAvatar(mUserInfoModel.avatar) }
        moreIv.setDebounceViewClickListener { showMoreOpView() }
        inviteIv.setDebounceViewClickListener {
            // 点击邀请唱聊
            StatisticsAdapter.recordCountEvent("cp", "invite2", null)
            clickListener?.onClickDoubleInvite(mUserInfoModel)
        }
        followIv.setDebounceViewClickListener { clickListener?.onClickFollow(mUserId, mUserInfoModel.isFriend, mUserInfoModel.isFollow) }

        guardView.clickListener = {
            //todo 待补全点击守护事件
        }

        photoView.isFocusableInTouchMode = false
        photoView.layoutManager = GridLayoutManager(context, 3)
        photoAdapter = PhotoAdapter(PhotoAdapter.TYPE_PERSON_CARD)
        photoView.adapter = photoAdapter
        photoAdapter?.mOnClickPhotoListener = { _, position, _ ->
            BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<PhotoModel>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: PhotoModel) {
                    if (TextUtils.isEmpty(item.picPath)) {
                        imageBrowseView.load(item.localPath)
                    } else {
                        imageBrowseView.load(item.picPath)
                    }
                }

                override fun getInitCurrentItemPostion(): Int {
                    return position
                }

                override fun getInitList(): List<PhotoModel>? {
                    return photoAdapter?.mDataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        getPhotos(photoAdapter?.successNum ?: 0, Callback { r, list ->
                            if (callback != null && list != null) {
                                callback.onCallback(0, list)
                            }
                        })
                    }
                }

                override fun hasMore(backward: Boolean, position: Int, data: PhotoModel): Boolean {
                    return if (backward) {
                        mHasMore
                    } else false
                }
            })
        }
    }

    private fun initData(userID: Int, showKick: Boolean, showInvite: Boolean) {
        mUserId = userID
        isShowKick = showKick
        isShowInvite = showInvite

        if (mUserInfoModel == null) {
            mUserInfoModel = UserInfoModel()
        }
        mUserInfoModel.userId = userID

        // 多音和ai裁判
        if (mUserId == UserAccountManager.SYSTEM_GRAB_ID || mUserId == UserAccountManager.SYSTEM_RANK_AI) {
            isShowKick = false
            isShowInvite = false
            moreIv.visibility = View.GONE
        }

        if (isShowInvite) {
            inviteIv.visibility = View.VISIBLE
        } else {
            inviteIv.visibility = View.GONE
        }

        getHomePage(mUserId)
        // 自己卡片的处理
        if (mUserId.toLong() == MyUserInfoManager.uid) {
            isShowKick = false
            moreIv.visibility = View.GONE
            inviteIv.visibility = View.GONE
            followIv.visibility = View.GONE
            photoView.visibility = View.GONE
            photoViewBg.visibility = View.GONE
            divider.visibility = View.INVISIBLE
        } else {
            getPhotos(0)
        }
    }

    private fun showMoreOpView() {
        mPersonMoreOpView?.dismiss()
        mPersonMoreOpView = PersonMoreOpView(context, mUserInfoModel.userId, mUserInfoModel.isFollow, mUserInfoModel.isSPFollow, isShowKick)
        mPersonMoreOpView?.setListener(object : PersonMoreOpView.Listener {
            override fun onClickSpFollow() {
                mPersonMoreOpView?.dismiss()
                clickListener?.showSpFollowDialog(mUserId, mUserInfoModel.isSPFollow)
//                        if (mUserInfoModel.isSPFollow) {
//                            // 取消特别关注
//                            mClickListener?.showUnSpFollowDialog(mUserId)
//                        } else {
//                            // 新增特别关注
//
//                            addSpecialFollow(mUserId)
//                        }
            }

            override fun onClickRemark() {
                mPersonMoreOpView?.dismiss()
                clickListener?.onClickRemark(mUserInfoModel)
            }

            override fun onClickReport() {
                mPersonMoreOpView?.dismiss()
                clickListener?.onClickReport(mUserId)
            }

            override fun onClickKick() {
                mPersonMoreOpView?.dismiss()
                clickListener?.onClickKick(mUserInfoModel)
            }

            override fun onClickBlack(isInBlack: Boolean) {
                mPersonMoreOpView?.dismiss()
                if (isInBlack) {
                    UserInfoManager.getInstance().removeBlackList(mUserId, object : ResponseCallBack<Any?>() {
                        override fun onServerSucess(o: Any?) {
                            U.getToastUtil().showShort("移除黑名单成功")
                        }

                        override fun onServerFailed() {

                        }
                    })
                } else {
                    UserInfoManager.getInstance().addToBlacklist(mUserId, object : ResponseCallBack<Any?>() {
                        override fun onServerSucess(o: Any?) {
                            U.getToastUtil().showShort("加入黑名单成功")
                        }

                        override fun onServerFailed() {

                        }
                    })
                }
            }
        })
        mPersonMoreOpView!!.showAt(moreIv)
    }

    private fun getHomePage(userId: Int) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userId.toLong()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val userInfoModel = JSON.parseObject(result.data!!.getString("userBaseInfo"), UserInfoModel::class.java)
                    val relationNumModes = JSON.parseArray(result.data!!.getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel::class.java)
                    val scoreDetailModel = JSON.parseObject(result.data.getString("scoreDetail"), ScoreDetailModel::class.java)
                    val isFriend = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFriend")
                            ?: false
                    val isFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFollow")
                            ?: false
                    val isSpFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isSPFollow")
                            ?: false

                    val meiLiCntTotal = result.data?.getIntValue("meiLiCntTotal") ?: 0
                    val qinMiCntTotal = result.data?.getIntValue("qinMiCntTotal") ?: 0

                    userInfoModel.isFollow = isFollow
                    userInfoModel.isFriend = isFriend
                    userInfoModel.isSPFollow = isSpFollow
                    if (isFollow) {
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel)
                    }
                    showUserInfo(userInfoModel)
                    showUserLevel(scoreDetailModel)
                    showUserRelationNum(relationNumModes)
                    showCharmsAndQinMiTag(meiLiCntTotal, qinMiCntTotal)
                    refreshFollow()
                }
            }
        }, mContext as BaseActivity)
    }

    @JvmOverloads
    internal fun getPhotos(off: Int, callback: Callback<List<PhotoModel>>? = null) {
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(mUserId.toLong(), off, DEFAULT_CNT), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result != null && result.errno == 0) {
                    mOffset = result.data!!.getIntValue("offset")
                    val list = JSON.parseArray(result.data?.getString("pic"), PhotoModel::class.java)
                    val totalCount = result.data!!.getIntValue("totalCount")
                    if (off == 0) {
                        addPhotos(list, totalCount, true)
                    } else {
                        addPhotos(list, totalCount, false)
                    }
                    callback?.onCallback(0, list)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        })
    }

    fun addPhotos(list: List<PhotoModel>?, totalCount: Int, clear: Boolean) {
        if (clear) {
            photoAdapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                photoAdapter?.mDataList?.addAll(list)
            }
            photoAdapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                if (photoAdapter?.mDataList?.size ?: 0 >= 3) {
                    photoAdapter?.mDataList?.addAll(list)
                } else {
                    photoAdapter?.mDataList?.addAll(list)
                    photoAdapter?.notifyDataSetChanged()
                }
            }
        }

        if (photoAdapter?.mDataList?.isNullOrEmpty() == true) {
            photoView.visibility = View.GONE
            photoViewBg.visibility = View.GONE
            divider.visibility = View.VISIBLE
        } else {
            photoView.visibility = View.VISIBLE
            photoViewBg.visibility = View.VISIBLE
            divider.visibility = View.GONE
        }

        if (totalCount > 3) {
            photoNumTv.visibility = View.VISIBLE
            photoNumTv.text = "${totalCount}张"
        } else {
            photoNumTv.visibility = View.GONE
        }
    }

    private fun showUserInfo(userInfoModel: UserInfoModel?) {
        if (userInfoModel != null) {
            mUserInfoModel = userInfoModel
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(userInfoModel.avatar)
                    .setCircle(true)
                    .build())
            nameView.setAllStateText(userInfoModel.nicknameRemark, null, userInfoModel.honorInfo)
            signTv.text = userInfoModel.signature

            if (userInfoModel.vipInfo != null && userInfoModel.vipInfo.vipType > 0) {
                signTv.visibility = View.GONE
                verifyTv.visibility = View.VISIBLE
                verifyTv.text = userInfoModel.vipInfo.vipDesc
            } else {
                signTv.visibility = View.VISIBLE
                verifyTv.visibility = View.GONE
            }

            personTagView.setSex(userInfoModel.sex)
            personTagView.setLocation(userInfoModel.location)
        }
    }

    private fun showUserLevel(scoreDetailModel: ScoreDetailModel?) {
        scoreDetailModel?.scoreStateModel?.let {
            if (LevelConfigUtils.getRaceCenterAvatarBg(it.mainRanking) != 0) {
                levelBg.visibility = View.VISIBLE
                levelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(it.mainRanking))
            } else {
                levelBg.visibility = View.GONE
            }
        }
    }

    private fun showUserRelationNum(relationNumModes: List<RelationNumModel>?) {
        var fansNum = 0
        if (relationNumModes != null && relationNumModes.size > 0) {
            for (mode in relationNumModes) {
                if (mode.relation == UserInfoManager.RELATION.FANS.value) {
                    fansNum = mode.cnt
                }
            }
        }
        personTagView.setFansNum(fansNum)
    }

    private fun showCharmsAndQinMiTag(meiLiCntTotal: Int, qinMiCntTotal: Int) {
        personTagView.setCharmTotal(meiLiCntTotal)
        if (qinMiCntTotal > 0) {
            qinmiTv.visibility = View.VISIBLE
            qinmiTv.text = qinMiCntTotal.toString()
        } else {
            qinmiTv.visibility = View.GONE
        }
    }

    private fun refreshFollow() {
        when {
            mUserInfoModel.isFriend -> {
                followIv.text = "互关"
                followIv.background = mFollowDrawable
            }
            mUserInfoModel.isFollow -> {
                followIv.text = "已关注"
                followIv.background = mFollowDrawable
            }
            else -> {
                followIv.text = "关注Ta"
                followIv.background = mUnFollowDrawable
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == mUserId) {
            mUserInfoModel.isFollow = event.isFollow
            mUserInfoModel.isFriend = event.isFriend
            mUserInfoModel.isSPFollow = event.isSpFollow

            refreshFollow()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FollowNotifyEvent) {
        if (event.mUserInfoModel != null && event.mUserInfoModel.userId == mUserId) {
            mUserInfoModel.isFollow = event.mUserInfoModel.isFollow
            mUserInfoModel.isFriend = event.mUserInfoModel.isFriend

            refreshFollow()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    fun setListener(listener: PersonInfoDialog.PersonCardClickListener) {
        this.clickListener = listener
    }
}