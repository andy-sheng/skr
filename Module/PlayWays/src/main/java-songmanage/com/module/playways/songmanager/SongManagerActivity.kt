package com.module.playways.songmanager

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity

import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.PinyinUtils
import com.common.utils.U
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.mic.room.MicRoomData
import com.module.playways.party.room.PartyRoomData
import com.module.playways.race.room.RaceRoomData
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.room.song.fragment.GrabSearchSongFragment
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.fragment.*
import org.greenrobot.eventbus.EventBus

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
        } else if (from == TYPE_FROM_RACE) {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabSearchSongFragment::class.java)
                    .setAddToBackStack(true)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, TYPE_FROM_RACE)
                    .addDataBeforeAdd(1, false)
                    .setFragmentDataListener(object : FragmentDataListener {
                        override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                            if (requestCode == 0 && resultCode == 0 && obj != null) {
                                val model = obj as SongModel
                                MyLog.d(TAG, "onFragmentResult model=$model")
                                EventBus.getDefault().post(AddSongEvent(model, TYPE_FROM_RACE))
                            }
                            finish()
                        }
                    })
                    .build())
        } else if (from == TYPE_FROM_RELAY_HOME) {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabSearchSongFragment::class.java)
                    .setAddToBackStack(true)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, TYPE_FROM_RELAY_HOME)
                    .addDataBeforeAdd(1, false)
                    .setFragmentDataListener(object : FragmentDataListener {
                        override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                            if (requestCode == 0 && resultCode == 0 && obj != null) {
                                val model = obj as SongModel
                                MyLog.d(TAG, "onFragmentResult model=$model")
                                EventBus.getDefault().post(AddSongEvent(model, TYPE_FROM_RELAY_HOME))
                            }
                            finish()
                        }
                    })
                    .build())
        } else if (from == TYPE_FROM_RELAY_ROOM) {
            val mRoomData = intent.getSerializableExtra("room_data") as RelayRoomData
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, RelaySongManageFragment::class.java)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build())
        } else if (from == TYPE_FROM_PARTY) {
            val mRoomData = intent.getSerializableExtra("room_data") as PartyRoomData
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, PartySongManageFragment::class.java)
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

        const val TYPE_FROM_GRAB = 1      //抢唱
        const val TYPE_FROM_DOUBLE = 2    //双人聊
        const val TYPE_FROM_MIC = 3       //排麦房
        const val TYPE_FROM_RACE = 4      //排位赛
        const val TYPE_FROM_RELAY_HOME = 5   //接唱首页
        const val TYPE_FROM_RELAY_ROOM = 6   //接唱房间
        const val TYPE_FROM_PARTY = 7        //剧场房间

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

        fun open(activity: FragmentActivity?, roomData: MicRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_MIC)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?, roomData: RaceRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_RACE)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_RELAY_HOME)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?, roomData: RelayRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_RELAY_ROOM)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }

        fun open(activity: FragmentActivity?, roomData: PartyRoomData) {
            val intent = Intent(activity, SongManagerActivity::class.java)
            intent.putExtra("from", TYPE_FROM_PARTY)
            intent.putExtra("room_data", roomData)
            activity?.startActivity(intent)
        }
    }
}
