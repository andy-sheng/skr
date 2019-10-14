package com.module.playways.race.room.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.view.RaceSongInfoView
import java.util.*

class RaceSelectSongAdapter(internal var mContext: Context, internal val listener: IRaceSelectListener) : PagerAdapter() {
    val TAG = "RaceSelectSongAdapter"
    private val cachedList = ArrayList<RaceSongInfoView>()
    internal var mRaceGamePlayInfos: List<RaceGamePlayInfo> = ArrayList()

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
        val view = `object` as RaceSongInfoView
        container.removeView(view)
        cachedList.add(view)
    }

    override fun getItemPosition(any: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        MyLog.d(TAG, "instantiateItem container=$container position=$position")
        val view = getCachedView()
        view.signUpCall = { choceId, model ->
            listener.onSignUp(choceId, model)
        }
        if (container.indexOfChild(view) == -1) {
            container.addView(view)
        }
        view.setData(position, mRaceGamePlayInfos[position], listener.getSignUpChoiceID() != -1, listener.getSignUpChoiceID())
        return view!!
    }

    fun setData(raceGamePlayInfoList: List<RaceGamePlayInfo>?) {
        if (raceGamePlayInfoList == null) {
            return
        }

        mRaceGamePlayInfos = raceGamePlayInfoList
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (mRaceGamePlayInfos == null) 0 else mRaceGamePlayInfos!!.size
    }

    fun destroy() {
    }

    private fun getCachedView(): RaceSongInfoView {
        if (cachedList.isNotEmpty()) {
            return cachedList.removeAt(0)
        }
        return RaceSongInfoView(mContext)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    interface IRaceSelectListener {
        fun onSignUp(choiceID: Int, model: RaceGamePlayInfo?)
        fun getSignUpChoiceID(): Int
    }
}
