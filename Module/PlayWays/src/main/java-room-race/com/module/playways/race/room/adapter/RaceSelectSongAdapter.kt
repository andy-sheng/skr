package com.module.playways.race.room.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.view.RaceSongInfoView
import java.util.*

class RaceSelectSongAdapter(internal var mContext: Context, internal val mSigupUpMethed: (Int, RaceGamePlayInfo?) -> Unit) : PagerAdapter() {
    val TAG = "GiftViewPagerAdapter"
    internal var mGiftOnePageViewHashMap = HashMap<Int, RaceSongInfoView>()
    internal var mRaceGamePlayInfos: List<RaceGamePlayInfo> = ArrayList()

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
        container.removeView(`object` as View)
    }

    override fun getItemPosition(any: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        MyLog.d(TAG, "instantiateItem container=$container position=$position")
        val view = mGiftOnePageViewHashMap[position]
        if (container.indexOfChild(view) == -1) {
            container.addView(view)
        }
        return view!!
    }

    fun setData(raceGamePlayInfoList: List<RaceGamePlayInfo>?) {
        if (raceGamePlayInfoList == null) {
            return
        }

        mRaceGamePlayInfos = raceGamePlayInfoList
        val size = mRaceGamePlayInfos.size
        for (i in 0 until size) {
            val model = mRaceGamePlayInfos.get(i)
            var view: RaceSongInfoView? = mGiftOnePageViewHashMap.get(i)
            if (view == null) {
                view = RaceSongInfoView(mContext)
                mGiftOnePageViewHashMap.put(i, view)
            }

            view.signUpCall = { choceId, model ->
                mSigupUpMethed.invoke(choceId, model)
            }

            view.setData(i, model)
        }

        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (mRaceGamePlayInfos == null) 0 else mRaceGamePlayInfos!!.size
    }

    fun destroy() {
        val integerGiftOnePageViewIterator = mGiftOnePageViewHashMap.entries.iterator()
        //        while (integerGiftOnePageViewIterator.hasNext()) {
        //            integerGiftOnePageViewIterator.next().getValue().destroy();
        //        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}
