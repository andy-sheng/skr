package com.module.playways.songmanager

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity

import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.PinyinUtils
import com.common.utils.U
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.mic.room.MicRoomData
import com.module.playways.songmanager.fragment.DoubleSongManageFragment
import com.module.playways.songmanager.fragment.GrabSongManageFragment
import com.module.playways.songmanager.fragment.MicSongManageFragment

class SongManagerActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return 0
    }

    override fun initData(savedInstanceState: Bundle?) {
        val from = intent.getIntExtra("from", 0)
        if (from == TYPE_FROM_GRAB) {
            val mRoomData = intent.getSerializableExtra("room_data") as GrabRoomData
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabSongManageFragment::class.java)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build())
        } else if (from == TYPE_FROM_DOUBLE) {
            val mRoomData = intent.getSerializableExtra("room_data") as DoubleRoomData
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, DoubleSongManageFragment::class.java)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build())
        } else if (from == TYPE_FROM_MIC) {
            val mRoomData = intent.getSerializableExtra("room_data") as MicRoomData
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, MicSongManageFragment::class.java)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build())
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    companion object {

        const val TYPE_FROM_GRAB = 1
        const val TYPE_FROM_DOUBLE = 2
        const val TYPE_FROM_MIC = 3

        fun open(activity: FragmentActivity?, roomData: GrabRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_GRAB)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?, roomData: DoubleRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_DOUBLE)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?,roomData: MicRoomData){
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_MIC)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }
    }
}
