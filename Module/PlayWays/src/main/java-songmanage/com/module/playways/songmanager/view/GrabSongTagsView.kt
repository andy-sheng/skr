package com.module.playways.songmanager.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.utils.U
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.friends.SpecialModel
import com.module.playways.R
import com.module.playways.songmanager.adapter.GrabTagsAdapter

import java.util.ArrayList

class GrabSongTagsView : RelativeLayout {

    val TAG = "GrabSongTagsView"
    val mRecyclerView: RecyclerView
    var mGrabTagsAdapter: GrabTagsAdapter

    var mCurSpecialModelId: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_song_tags_view_layout, this)
        mRecyclerView = findViewById(R.id.recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mGrabTagsAdapter = GrabTagsAdapter()
        mRecyclerView.adapter = mGrabTagsAdapter
        val drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45f).toFloat())
                .setSolidColor(Color.parseColor("#404A9A"))
                .setCornersRadius(U.getDisplayUtils().dip2px(13f).toFloat())
                .build()
        background = drawable
    }

    fun setSpecialModelList(specialModelList: List<SpecialModel>?) {
        if (specialModelList != null && specialModelList.size > 0) {
            val models = ArrayList(specialModelList)
            val iterator = models.iterator()
            while (iterator.hasNext()) {
                val grabRoomSongModel = iterator.next()
                if (grabRoomSongModel.tagID == mCurSpecialModelId) {
                    iterator.remove()
                }
            }

            mGrabTagsAdapter.dataList = models
        }
    }
}
