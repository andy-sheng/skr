package com.zq.dialog

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
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

    private val mTags = ArrayList<String>()  //标签
    private val mHashMap = HashMap<Int, String>()
    private var mTagAdapter: TagAdapter<String>

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.business_card_dialog_view, this)


        AvatarUtils.loadAvatarByUrl(avatar_iv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setCircle(true)
                .build())
        name_tv.text = MyUserInfoManager.getInstance().nickName
        userid_tv.text = "ID号：" + MyUserInfoManager.getInstance().uid
        if (MyUserInfoManager.getInstance().sex == ESex.SX_MALE.value) {
            sex_iv.setVisibility(View.VISIBLE)
            sex_iv.setBackgroundResource(R.drawable.sex_man_icon)
        } else if (MyUserInfoManager.getInstance().sex == ESex.SX_FEMALE.value) {
            sex_iv.setVisibility(View.VISIBLE)
            sex_iv.setBackgroundResource(R.drawable.sex_woman_icon)
        } else {
            sex_iv.setVisibility(View.GONE)
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

        if (MyUserInfoManager.getInstance().location != null && !TextUtils.isEmpty(MyUserInfoManager.getInstance().location.city)) {
            mHashMap[LOCATION_TAG] = MyUserInfoManager.getInstance().location.city
        }

        if (MyUserInfoManager.getInstance().age > 0) {
            mHashMap[AGE_TAG] = MyUserInfoManager.getInstance().age.toString() + "岁"
        }

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().constellation)) {
            mHashMap[CONSTELLATION_TAG] = MyUserInfoManager.getInstance().constellation
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

        }
        mTagAdapter.setTagDatas(mTags)
        mTagAdapter.notifyDataChanged()
    }
}
