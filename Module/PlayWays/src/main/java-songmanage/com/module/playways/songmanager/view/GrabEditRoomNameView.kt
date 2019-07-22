package com.module.playways.songmanager.view

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout

import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.NoLeakEditText
import com.dialog.view.StrokeTextView
import com.module.playways.R

/**
 * 修改房间名字
 */
class GrabEditRoomNameView(context: Context, internal var mRoomNameText: String) : RelativeLayout(context) {

    private var mRoomName: NoLeakEditText
    private var mClearEditIv: ImageView
    private var mCancelTv: StrokeTextView
    private var mSaveTv: StrokeTextView
    private var mPlaceBottomView: View
    private var mPlaceTopView: View

    var onClickCancel: (() -> Unit)? = null
    var onClickSave: ((roomName: String) -> Unit)? = null

    init {
        View.inflate(context, R.layout.grab_edit_view_layout, this)
        mRoomName = this.findViewById<View>(R.id.room_name) as NoLeakEditText
        mClearEditIv = this.findViewById<View>(R.id.clear_edit_iv) as ImageView
        mCancelTv = this.findViewById<View>(R.id.cancel_tv) as StrokeTextView
        mSaveTv = this.findViewById<View>(R.id.save_tv) as StrokeTextView

        mPlaceBottomView = this.findViewById(R.id.place_bottom_view) as View
        mPlaceTopView = this.findViewById(R.id.place_top_view) as View

        val layoutParams = mPlaceBottomView.layoutParams
        layoutParams.height = U.getKeyBoardUtils().keyBoardHeight
        mPlaceBottomView.layoutParams = layoutParams

        if (!TextUtils.isEmpty(mRoomNameText)) {
            mRoomName.setText(mRoomNameText)
            mRoomName.hint = mRoomNameText
        }
        initListener()
    }

    private fun initListener() {
        mCancelTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                onClickCancel?.invoke()
            }
        })

        mPlaceBottomView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                onClickCancel?.invoke()
            }
        })

        mPlaceTopView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                onClickCancel?.invoke()
            }
        })

        mSaveTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                val name = mRoomName.text.toString().trim { it <= ' ' }
                onClickSave?.invoke(name)
            }
        })

        mClearEditIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mRoomName.setText("")
            }
        })

        mRoomName.postDelayed({
            val editName = mRoomName.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(editName)) {
                mRoomName.setSelection(editName.length)
            }
            mRoomName.requestFocus()
        }, 300)

    }

}
