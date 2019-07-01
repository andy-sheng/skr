package com.zq.dialog

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.zq.live.proto.Common.ESex
import kotlinx.android.synthetic.main.business_card_dialog_view.view.*
import java.util.ArrayList
import java.util.HashMap

// 个人名片页面
class BusinessCardDialogView : ConstraintLayout {

    private val LOCATION_TAG = 1           //城市标签
    private val AGE_TAG = 2                //年龄标签
    private val CONSTELLATION_TAG = 3      //星座标签
    private val FANS_NUM_TAG = 4           //粉丝数

    private val mTags = ArrayList<String>()  //标签
    private val mHashMap = HashMap<Int, String>()
    private var mTagAdapter: TagAdapter<String>? = null

    private var mUserInfo: UserInfoModel
    private var mFansNums: Int

    constructor(context: Context, userInfo: UserInfoModel, fansNum: Int) : super(context) {
        this.mUserInfo = userInfo
        this.mFansNums = fansNum
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

        mTagAdapter = object : TagAdapter<String>(mTags) {
            override fun getView(parent: FlowLayout, position: Int, o: String): View {
                val tv = LayoutInflater.from(context).inflate(R.layout.person_center_business_tag,
                        flowlayout, false) as ExTextView
                tv.text = o
                return tv
            }
        }
        flowlayout.adapter = mTagAdapter

        if (mUserInfo.location != null && !TextUtils.isEmpty(mUserInfo.location.city)) {
            mHashMap[LOCATION_TAG] = mUserInfo.location.city
        }

        if (mUserInfo.age > 0) {
            mHashMap[AGE_TAG] = mUserInfo.age.toString() + "岁"
        }

        if (!TextUtils.isEmpty(mUserInfo.constellation)) {
            mHashMap[CONSTELLATION_TAG] = mUserInfo.constellation
        }

        if (mUserInfo.userId != MyUserInfoManager.getInstance().uid.toInt()) {
            mHashMap[FANS_NUM_TAG] = "粉丝" + mFansNums
        }

        refreshTag()
    }

    private fun refreshTag() {
        mTags.clear()
        if (mHashMap != null) {

            if (!TextUtils.isEmpty(mHashMap[LOCATION_TAG])) {
                mTags.add(mHashMap[LOCATION_TAG]!!)
            }

            if (!TextUtils.isEmpty(mHashMap[AGE_TAG])) {
                mTags.add(mHashMap[AGE_TAG]!!)
            }

            if (!TextUtils.isEmpty(mHashMap[CONSTELLATION_TAG])) {
                mTags.add(mHashMap[CONSTELLATION_TAG]!!)
            }

            if (!TextUtils.isEmpty(mHashMap[FANS_NUM_TAG])) {
                mTags.add(mHashMap[FANS_NUM_TAG]!!)
            }

        }
        mTagAdapter!!.setTagDatas(mTags)
        mTagAdapter!!.notifyDataChanged()
    }
}
