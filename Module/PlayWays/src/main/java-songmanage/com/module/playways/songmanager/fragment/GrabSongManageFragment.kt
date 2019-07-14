package com.module.playways.songmanager.fragment

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup

import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.base.FragmentDataListener
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.event.SongNumChangeEvent
import com.module.playways.songmanager.model.RecommendTagModel
import com.module.playways.songmanager.presenter.GrabSongManagePresenter
import com.module.playways.songmanager.view.GrabEditRoomNameView
import com.module.playways.songmanager.view.GrabExistSongManageView
import com.module.playways.songmanager.view.GrabSongWishView
import com.module.playways.songmanager.view.ISongManageView
import com.module.playways.songmanager.view.RecommendSongView
import com.module.playways.room.song.fragment.GrabSearchSongFragment
import com.module.playways.room.song.model.SongModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.OnDismissListener
import com.orhanobut.dialogplus.ViewHolder

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 一唱到底，房主点歌或房成员点歌
 */
class GrabSongManageFragment : BaseFragment(), ISongManageView {
    lateinit var mCommonTitleBar: CommonTitleBar
    lateinit var mSearchSongIv: ExTextView
    lateinit var mTagTab: SlidingTabLayout
    lateinit var mViewpager: ViewPager

    lateinit var mPagerAdapter: PagerAdapter

    private var mGrabSongManageView: GrabExistSongManageView? = null
    private var mGrabSongWishView: GrabSongWishView? = null
    private var mEditRoomDialog: DialogPlus? = null

    lateinit var mOwnerManagePresenter: GrabSongManagePresenter
    private var mRoomData: GrabRoomData? = null

    var mTagModelList: List<RecommendTagModel>? = null

    override fun initView(): Int {
        return R.layout.grab_song_manage_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mRoomData == null) {
            if (activity != null) {
                activity!!.finish()
            }
            return
        }

        mCommonTitleBar = mRootView.findViewById<View>(R.id.titlebar) as CommonTitleBar
        mSearchSongIv = mRootView.findViewById<View>(R.id.search_song_iv) as ExTextView
        mTagTab = mRootView.findViewById<View>(R.id.tag_tab) as SlidingTabLayout
        mViewpager = mRootView.findViewById<View>(R.id.viewpager) as ViewPager

        mCommonTitleBar.centerTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                showEditRoomDialog()
            }
        })

        mCommonTitleBar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity is SongManagerActivity) {
                    if (activity != null) {
                        activity!!.finish()
                    }
                } else {
                    finish()
                }
            }
        })

        mOwnerManagePresenter = GrabSongManagePresenter(this, mRoomData)
        addPresent(mOwnerManagePresenter)
        mOwnerManagePresenter.getRecommendTag()
        showRoomName(mRoomData!!.roomName)

        mSearchSongIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(context as BaseActivity?, GrabSearchSongFragment::class.java)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_GRAB)
                        .addDataBeforeAdd(1, mRoomData!!.isOwner)
                        .setFragmentDataListener { requestCode, resultCode, bundle, obj ->
                            if (requestCode == 0 && resultCode == 0 && obj != null) {
                                val model = obj as SongModel
                                MyLog.d(TAG, "onFragmentResult model=$model")
                                EventBus.getDefault().post(AddSongEvent(model))
                            }
                        }
                        .build())
            }
        })
    }

    override fun showRoomName(roomName: String) {
        mCommonTitleBar.centerTextView.text = roomName
        if (mRoomData!!.isOwner) {
            val drawable = U.getDrawable(R.drawable.ycdd_edit_roomname_icon)
            drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            mCommonTitleBar.centerTextView.setCompoundDrawables(null, null, drawable, null)
            mCommonTitleBar.centerTextView.compoundDrawablePadding = U.getDisplayUtils().dip2px(7f)
            mCommonTitleBar.centerTextView.isClickable = true
        } else {
            mCommonTitleBar.centerTextView.isClickable = false
        }
    }

    /**
     * 得到所有类别
     *
     * @param recommendTagModelList
     */

    override fun showRecommendSong(recommendTagModelList: MutableList<RecommendTagModel>) {
        if (recommendTagModelList == null || recommendTagModelList.size == 0) {
            return
        }


        if (mRoomData!!.isOwner) {
            val recommendModel = RecommendTagModel()
            recommendModel.type = -1
            recommendModel.name = "愿望歌单"
            recommendTagModelList.add(0, recommendModel)

            val recommendTagModel = RecommendTagModel()
            recommendTagModel.type = -1
            recommendTagModel.name = "已点0"
            recommendTagModelList.add(0, recommendTagModel)
        }

        mTagModelList = recommendTagModelList
        mTagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv)
        mTagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        mTagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        mTagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        mTagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        mTagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        mPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return instantiateItemGrab(container, position, mTagModelList!!)
            }

            override fun getCount(): Int {
                return mTagModelList!!.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mTagModelList!![position].name
            }
        }

        mTagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = mViewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is RecommendSongView) {
                        view.tryLoad()
                    } else if (view is GrabSongWishView) {
                        view.tryLoad()
                    } else if (view is GrabExistSongManageView) {
                        view.tryLoad()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mViewpager.adapter = mPagerAdapter
        mTagTab.setViewPager(mViewpager)
        mPagerAdapter.notifyDataSetChanged()
    }

    fun instantiateItemGrab(container: ViewGroup, position: Int, recommendTagModelList: List<RecommendTagModel>): Any {
        MyLog.d(TAG, "instantiateItem container=$container position=$position")
        var view: View

        if (mRoomData!!.isOwner) {
            if (position == 0) {
                if (mGrabSongManageView == null) {
                    mGrabSongManageView = GrabExistSongManageView(context!!, mRoomData!!)
                }
                view = mGrabSongManageView!!
            } else if (position == 1) {
                if (mGrabSongWishView == null) {
                    mGrabSongWishView = GrabSongWishView(context!!, mRoomData!!)
                }
                mGrabSongWishView!!.tag = position
                view = mGrabSongWishView!!
            } else {
                val recommendTagModel = recommendTagModelList[position]
                val recommendSongView = RecommendSongView(activity!!, SongManagerActivity.TYPE_FROM_GRAB,
                        mRoomData!!.isOwner, mRoomData!!.gameId, recommendTagModel)
                recommendSongView.tag = position
                view = recommendSongView
            }
        } else {
            val recommendTagModel = recommendTagModelList[position]
            val recommendSongView = RecommendSongView(activity!!, SongManagerActivity.TYPE_FROM_GRAB,
                    mRoomData!!.isOwner, mRoomData!!.gameId, recommendTagModel)
            recommendSongView.tag = position
            view = recommendSongView
        }

        if (container.indexOfChild(view) == -1) {
            container.addView(view)
        }

        return view
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mRoomData = data as GrabRoomData?
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SongNumChangeEvent) {
        mTagModelList?.get(0)?.name = "已点" + event.songNum
        mTagTab.notifyDataChange()
    }

    private fun showEditRoomDialog() {
        val grabEditView = GrabEditRoomNameView(context!!, mRoomData!!.roomName)
        grabEditView.onClickCancel = {
            mEditRoomDialog?.dismiss()
        }
        grabEditView.onClickSave = {
            if (!TextUtils.isEmpty(it)) {
                // TODO: 2019/4/18 修改房间名
                mEditRoomDialog?.dismiss(false)
                mOwnerManagePresenter.updateRoomName(mRoomData!!.gameId, it)
            } else {
                // TODO: 2019/4/18 房间名为空
                U.getToastUtil().showShort("输入的房间名为空")
            }
        }

        mEditRoomDialog?.dismiss(false)
        mEditRoomDialog = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(grabEditView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .setOnDismissListener { U.getKeyBoardUtils().hideSoftInputKeyBoard(activity) }
                .create()
        U.getKeyBoardUtils().showSoftInputKeyBoard(activity)
        mEditRoomDialog!!.show()
    }

    override fun destroy() {
        super.destroy()
        //        for (RecommendSongView recommendSongView : mRecommendSongViews) {
        //            recommendSongView.destroy();
        //        }

        if (mGrabSongManageView != null) {
            mGrabSongManageView!!.destroy()
        }

        if (mGrabSongWishView != null) {
            mGrabSongWishView!!.destroy()
        }
    }

    override fun onBackPressed(): Boolean {
        if (mEditRoomDialog != null && mEditRoomDialog!!.isShowing) {
            mEditRoomDialog!!.dismiss(false)
            mEditRoomDialog = null
            return true
        }
        if (activity != null) {
            activity!!.finish()
        }
        return true
    }

    override fun useEventBus(): Boolean {
        return true
    }
}
