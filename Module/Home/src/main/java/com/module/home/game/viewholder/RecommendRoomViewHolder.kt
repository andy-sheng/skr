package com.module.home.game.viewholder

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.friends.FriendRoomHorizontalAdapter
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.friends.RecommendModel
import com.module.home.R
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.listener.EndlessRecycleOnScollListener
import com.module.home.game.model.RecommendRoomModel

class RecommendRoomViewHolder(itemView: View, internal var mBaseFragment: BaseFragment,
                              onEnterRoomListener: ((model: RecommendModel) -> Unit)?,
                              onMoreRoomListener: (() -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    val TAG = "RecommendRoomViewHolder"

    private var mRecommendRoomModel: RecommendRoomModel? = null
    private var mFriendRoomAdapter: FriendRoomHorizontalAdapter

    init {
        var mFriendsRecycle: RecyclerView = itemView.findViewById(R.id.friends_recycle)
        var mMoreFriends: ExImageView = itemView.findViewById(R.id.more_friends)

        mFriendsRecycle.isFocusableInTouchMode = false
        mFriendsRecycle.layoutManager = LinearLayoutManager(mBaseFragment.context, LinearLayoutManager.HORIZONTAL, false)
        mFriendRoomAdapter = FriendRoomHorizontalAdapter(RecyclerOnItemClickListener<RecommendModel> { _, _, model ->
            if (model != null) {
                onEnterRoomListener?.invoke(model)
            } else {
                MyLog.w(TAG, "onItemClicked model = null")
            }
        })
        mFriendsRecycle.addOnScrollListener(object : EndlessRecycleOnScollListener() {
            override fun onLoadMore() {
                MyLog.d(TAG, "onLoadMore")
                refreshData()
            }
        })

        mMoreFriends.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                onMoreRoomListener?.invoke()
            }
        })

        mFriendsRecycle.adapter = mFriendRoomAdapter

    }

    fun bindData(recommendRoomModel: RecommendRoomModel) {
        this.mRecommendRoomModel = recommendRoomModel
        mFriendRoomAdapter.dataList = mRecommendRoomModel?.roomModels
    }

    private fun refreshData() {
        val mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
        ApiMethods.subscribe(mGrabSongApi.firstPageRecommendRoomList, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val list = JSON.parseArray(obj.data!!.getString("rooms"), RecommendModel::class.java)
                    refreshData(list)
                }
            }
        }, mBaseFragment)
    }

    private fun refreshData(list: List<RecommendModel>?) {
        if (list != null && list.isNotEmpty()) {
            this.mRecommendRoomModel?.roomModels?.clear()
            this.mRecommendRoomModel?.roomModels?.addAll(list)
            mFriendRoomAdapter.dataList = mRecommendRoomModel?.roomModels
        }
    }
}

