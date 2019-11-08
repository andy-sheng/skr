package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.Location
import com.common.core.userinfo.model.UserInfoModel
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.utils.StringFromatUtils
import com.component.person.model.TagModel
import com.zq.live.proto.Common.ESex
import kotlinx.android.synthetic.main.person_tag_view_layout.view.*
import java.util.ArrayList
import java.util.HashMap

// 个人页面和他人页面的标签
class PersonTagView : ConstraintLayout {

    private val SEX_TAG = 1                //性别标签
    private val LOCATION_TAG = 2           //城市标签
    private val CHARM_TAG = 3              //魅力值标签
    private val USERID_TAG = 4             //撕歌ID标签
    private val FANS_NUM_TAG = 5           //粉丝数（他人中心才有）
    private val QINMI_TAG = 6              //亲密度 (信息卡片才有)

    private var sex: Int = ESex.SX_UNKNOWN.value

    private val mTags = ArrayList<TagModel>()  //标签
    private val mHashMap = HashMap<Int, String>()
    private var mTagAdapter: TagAdapter<TagModel>? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.person_tag_view_layout, this)

        mTagAdapter = object : TagAdapter<TagModel>(mTags) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: TagModel): View {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.person_tag_item_layout, parent, false)
                val descIv: ImageView = view.findViewById(R.id.desc_iv)
                val descTv: TextView = view.findViewById(R.id.desc_tv)
                descTv.text = tagModel.content
                when {
                    tagModel.type == CHARM_TAG -> {
                        descIv.visibility = View.VISIBLE
                        descIv.background = U.getDrawable(R.drawable.grab_charm_icon)
                    }
                    tagModel.type == QINMI_TAG -> {
                        descIv.visibility = View.VISIBLE
                        descIv.background = U.getDrawable(R.drawable.game_qinmi_icon)
                    }
                    tagModel.type == SEX_TAG -> {
                        descIv.visibility = View.VISIBLE
                        when (sex) {
                            ESex.SX_MALE.value -> descIv.background = U.getDrawable(R.drawable.sex_male_icon)
                            ESex.SX_FEMALE.value -> descIv.background = U.getDrawable(R.drawable.sex_female_icon)
                            else -> descIv.visibility = View.GONE
                        }

                    }
                    else -> descIv.visibility = View.GONE
                }

                return view
            }
        }
        flowlayout.adapter = mTagAdapter
    }

    fun setSex(sex: Int) {
        this.sex = sex
        when (sex) {
            ESex.SX_MALE.value -> mHashMap[SEX_TAG] = "男"
            ESex.SX_FEMALE.value -> mHashMap[SEX_TAG] = "女"
            else -> {

            }
        }
        refreshTag()
    }

    fun setLocation(location: Location?) {
        if (location != null && !TextUtils.isEmpty(location.province)) {
            mHashMap[LOCATION_TAG] = location.province
        } else {
            mHashMap[LOCATION_TAG] = "火星"
        }
        refreshTag()
    }

    fun setCharmTotal(meiLiCntTotal: Int) {
        mHashMap[CHARM_TAG] = "魅力${StringFromatUtils.formatMillion(meiLiCntTotal)}"
        refreshTag()
    }

    fun setQinMiTotal(qinMiCntTotal: Int) {
        mHashMap[QINMI_TAG] = "亲密度${StringFromatUtils.formatMillion(qinMiCntTotal)}"
        refreshTag()
    }

    fun setUserID(userID: Int) {
        mHashMap[USERID_TAG] = "撕歌号$userID"
        refreshTag()
    }

    fun setFansNum(fansNum: Int) {
        mHashMap[FANS_NUM_TAG] = "粉丝${StringFromatUtils.formatTenThousand(fansNum)}"
        refreshTag()
    }

    private fun refreshTag() {
        mTags.clear()
        if (mHashMap != null) {
            if (!TextUtils.isEmpty(mHashMap[SEX_TAG])) {
                mTags.add(TagModel(SEX_TAG, mHashMap[SEX_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[LOCATION_TAG])) {
                mTags.add(TagModel(LOCATION_TAG, mHashMap[LOCATION_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[CHARM_TAG])) {
                mTags.add(TagModel(CHARM_TAG, mHashMap[CHARM_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[USERID_TAG])) {
                mTags.add(TagModel(USERID_TAG, mHashMap[USERID_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[FANS_NUM_TAG])) {
                mTags.add(TagModel(FANS_NUM_TAG, mHashMap[FANS_NUM_TAG]!!))
            }

            if (!TextUtils.isEmpty(mHashMap[QINMI_TAG])) {
                mTags.add(TagModel(QINMI_TAG, mHashMap[QINMI_TAG]))
            }
        }
        mTagAdapter!!.setTagDatas(mTags)
        mTagAdapter!!.notifyDataChanged()
    }
}
