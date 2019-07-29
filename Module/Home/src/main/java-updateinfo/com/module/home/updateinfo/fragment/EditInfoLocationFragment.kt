package com.module.home.updateinfo.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.base.BaseFragment
import com.common.core.myinfo.Location
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.permission.PermissionUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.common.view.titlebar.CommonTitleBar
import com.module.home.R

class EditInfoLocationFragment : BaseFragment() {
    val mTag = "EditInfoLocationFragment"
    var mTitlebar: CommonTitleBar? = null
    var mBackground: ExImageView? = null
    var mGpsLocationTv: ExTextView? = null
    var mWholeTv: ExTextView? = null
    var mRecyclerView: RecyclerView? = null
    var mAdapter: DiffAdapter<String, LocationHolder>? = null
    lateinit var mProvinceArray: ArrayList<String>
    var mGpsLocation: Location? = null

    //之前定位的位置
    var mPreProvince: String? = null

    override fun initView() = R.layout.edit_info_location_fragment_layout

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = rootView.findViewById(com.module.home.R.id.titlebar)
        mBackground = rootView.findViewById(com.module.home.R.id.background)
        mGpsLocationTv = rootView.findViewById(com.module.home.R.id.gps_location)
        mWholeTv = rootView.findViewById(com.module.home.R.id.whole_tv)
        mRecyclerView = rootView.findViewById(com.module.home.R.id.recycler_view)
        mRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mAdapter = object : DiffAdapter<String, LocationHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.location_item_layout, parent, false)
                return LocationHolder(view)
            }

            override fun getItemCount() = mProvinceArray?.size

            override fun onBindViewHolder(holder: LocationHolder, position: Int) {
                holder.bindData(mProvinceArray.get(position))
            }
        }
        mRecyclerView?.adapter = mAdapter
        mProvinceArray = resources.getStringArray(R.array.province_list).toList() as ArrayList<String>
        mAdapter?.dataList = mProvinceArray

        mPreProvince = MyUserInfoManager.getInstance().locationProvince

        mTitlebar?.leftTextView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getFragmentUtils().popFragment(this@EditInfoLocationFragment)
            }
        })

        mGpsLocationTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mGpsLocation == null) {
                    U.getPermissionUtils().requestLocation(object : PermissionUtils.RequestPermission {
                        override fun onRequestPermissionSuccess() {
                            gps()
                        }

                        override fun onRequestPermissionFailure(permissions: MutableList<String>?) {

                        }

                        override fun onRequestPermissionFailureWithAskNeverAgain(permissions: MutableList<String>?) {

                        }
                    }, activity)
                } else {
                    updateLocation(mGpsLocation)
                    U.getFragmentUtils().popFragment(this@EditInfoLocationFragment)
                }
            }
        })

        if (U.getPermissionUtils().checkLocation(activity)) {
            gps()
        }
    }

    private fun gps() {
        U.getLbsUtils().getLocation(false) { location ->
            MyLog.d(mTag, "onReceive location=$location")
            if (location != null && location.isValid) {
                mGpsLocation = Location()
                mGpsLocation?.province = location.province
                mGpsLocation?.city = location.city
                mGpsLocation?.district = location.district
                mGpsLocationTv?.text = location.province
            } else {
                U.getToastUtil().showShort("获取位置失败，请重试")
                mGpsLocation = null
                mGpsLocationTv?.text = "未知位置"
            }
        }
    }

    override fun useEventBus() = false

    private fun updateLocation(l: Location?) {
        if (l == null) {
            MyLog.e(mTag, "updateLocation location is null")
            return
        }

        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager
                .newMyInfoUpdateParamsBuilder()
                .setLocation(l)
                .build(), true)

        fragmentDataListener?.onFragmentResult(0, 1, null, null)
    }

    inner class LocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mProvinceTv: ExTextView
        var mSelectIv: ImageView
        var mProvince: String? = null

        init {
            mProvinceTv = itemView.findViewById(com.module.home.R.id.province_tv) as ExTextView
            mSelectIv = itemView.findViewById(com.module.home.R.id.select_iv) as ImageView
            itemView.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    val l = Location()
                    l.province = mProvince
                    updateLocation(l)
                    U.getFragmentUtils().popFragment(this@EditInfoLocationFragment)
                }
            })
        }

        fun bindData(province: String) {
            this.mProvince = province
            mProvinceTv.text = province
            mSelectIv.visibility = if (province.equals(mPreProvince)) View.VISIBLE else View.GONE
        }
    }
}