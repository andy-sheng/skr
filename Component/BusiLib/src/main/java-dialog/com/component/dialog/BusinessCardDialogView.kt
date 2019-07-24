package com.component.dialog

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.live.proto.Common.ESex
import com.component.person.StringFromatUtils
import com.component.person.model.TagModel
import kotlinx.android.synthetic.main.business_card_dialog_view.view.*
import java.util.ArrayList
import java.util.HashMap

// 个人名片页面
class BusinessCardDialogView : ConstraintLayout {

    private val CHARM_TAG = 1              //魅力值标签
    private val FANS_NUM_TAG = 2           //粉丝数
    private val LOCATION_TAG = 3           //城市标签

    private val mTags = ArrayList<TagModel>()  //标签
    private val mHashMap = HashMap<Int, String>()
    private var mTagAdapter: TagAdapter<TagModel>? = null

    private var mUserInfo: UserInfoModel
    private var mFansNums: Int
    private var mCharmNums: Int

    constructor(context: Context, userInfo: UserInfoModel, fansNum: Int, charmNum: Int) : super(context) {
        this.mUserInfo = userInfo
        this.mFansNums = fansNum
        this.mCharmNums = charmNum
        initData()
    }

    init {
        View.inflate(context, R.layout.business_card_dialog_view, this)
    }

    private fun initData() {
        AvatarUtils.loadAvatarByUrl(avatar_iv, AvatarUtils.newParamsBuilder(mUserInfo.avatar)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setCircle(true)
                .build())
        name_tv.text = UserInfoManager.getInstance().getRemarkName(mUserInfo.userId, mUserInfo.nickname)
        userid_tv.text = "ID号：" + mUserInfo.userId
        if (mUserInfo.sex == ESex.SX_MALE.value) {
            sex_iv.visibility = View.VISIBLE
            sex_iv.setBackgroundResource(R.drawable.sex_man_icon)
        } else if (mUserInfo.sex == ESex.SX_FEMALE.value) {
            sex_iv.visibility = View.VISIBLE
            sex_iv.setBackgroundResource(R.drawable.sex_woman_icon)
        } else {
            sex_iv.visibility = View.GONE
        }

        mTagAdapter = object : TagAdapter<TagModel>(mTags) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: TagModel): View {
                if (tagModel.type != CHARM_TAG) {
                    val tv = LayoutInflater.from(context).inflate(R.layout.person_center_business_tag,
                            flowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                } else {
                    val tv = LayoutInflater.from(context).inflate(R.layout.person_card_charm_tag,
                            flowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                }
            }
        }
        flowlayout.adapter = mTagAdapter

        mHashMap[CHARM_TAG] = "魅力 " + StringFromatUtils.formatCharmNum(mCharmNums)
        mHashMap[FANS_NUM_TAG] = "粉丝 " + StringFromatUtils.formatFansNum(mFansNums)

        if (mUserInfo.location != null && !TextUtils.isEmpty(mUserInfo.location.province)) {
            mHashMap[LOCATION_TAG] = mUserInfo.location.province
        } else {
            mHashMap[LOCATION_TAG] = "火星"
        }

        refreshTag()
    }

    private fun refreshTag() {
        mTags.clear()
        if (mHashMap != null) {

            if (!TextUtils.isEmpty(mHashMap[CHARM_TAG])) {
                mTags.add(TagModel(CHARM_TAG, mHashMap[CHARM_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[FANS_NUM_TAG])) {
                mTags.add(TagModel(FANS_NUM_TAG, mHashMap[FANS_NUM_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[LOCATION_TAG])) {
                mTags.add(TagModel(LOCATION_TAG, mHashMap[LOCATION_TAG]!!))
            }
        }
        mTagAdapter!!.setTagDatas(mTags)
        mTagAdapter!!.notifyDataChanged()
    }
}
