package com.module.home.game.viewholder

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import com.common.base.BaseFragment
import com.common.view.AnimateClickListener
import com.common.view.ex.ExImageView
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.adapter.GrabSelectAdapter
import com.module.home.game.model.QuickJoinRoomModel

class QuickRoomViewHolder(itemView: View, baseFragment: BaseFragment,
                          onCreateRoomListener: (() -> Unit)?,
                          onSelectSpecialListener: ((specialModel: SpecialModel) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    val TAG = "QuickRoomViewHolder"

    private var mGrabSelectAdapter: GrabSelectAdapter

    init {
        var mCreateRoom: ExImageView = itemView.findViewById(R.id.create_room)
        var mFriendsRecycle: RecyclerView = itemView.findViewById(R.id.friends_recycle)
        mFriendsRecycle.isFocusableInTouchMode = false
        mFriendsRecycle.layoutManager = GridLayoutManager(baseFragment.context, 2)
        mGrabSelectAdapter = GrabSelectAdapter(RecyclerOnItemClickListener<SpecialModel> { _, _, model ->
            onSelectSpecialListener?.invoke(model)
        })

        mCreateRoom.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                onCreateRoomListener?.invoke()
            }
        })

        mFriendsRecycle.adapter = mGrabSelectAdapter
    }

    fun bindData(quickJoinRoomModel: QuickJoinRoomModel) {
        mGrabSelectAdapter.dataList = quickJoinRoomModel.modelList
        mGrabSelectAdapter.notifyDataSetChanged()
    }
}
