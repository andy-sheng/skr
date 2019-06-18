package com.moudule.playways.beauty.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.viewpager.SlidingTabLayout
import com.module.playways.R

/**
 * 美颜、滤镜和贴纸的控制面板
 */
class BeautyControlPanelView(viewStub: ViewStub?) : ExViewStub(viewStub) {

    companion object {
        const val TYPE_BEAUTY = 1     // 美颜
        const val TYPE_FITER = 2      // 滤镜
        const val TYPE_PATER = 3      // 贴纸
    }

    lateinit var mBeautyTitleStl: SlidingTabLayout
    lateinit var mPagerAdapter: PagerAdapter
    lateinit var mListener: Listener
    lateinit var mBeautyVp: ViewPager
    var mShowOrHideAnimator: Animator?=null
    var mPlaceHolderView:View? = null

    override fun init(parentView: View?) {
        mBeautyTitleStl = mParentView.findViewById(R.id.beauty_title_stl);
        mBeautyTitleStl.setCustomTabView(R.layout.beauty_tab_view, R.id.tab_tv)
        mBeautyTitleStl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        mBeautyTitleStl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        mBeautyTitleStl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mBeautyTitleStl.setIndicatorWidth(U.getDisplayUtils().dip2px(56f))
        mBeautyTitleStl.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
        mBeautyTitleStl.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        mBeautyTitleStl.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())

        mBeautyVp = mParentView.findViewById(R.id.beauty_vp);
        mPagerAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position == 0) {
                    // 美颜
                    var mBeautyFiterPaterView = BeautyFiterPaterView(mBeautyVp.context, TYPE_BEAUTY, getViewModel(TYPE_BEAUTY), mListener)
                    if (container.indexOfChild(mBeautyFiterPaterView) == -1) {
                        container.addView(mBeautyFiterPaterView)
                    }
                    return mBeautyFiterPaterView
                } else if (position == 1) {
                    var mBeautyFiterPaterView = BeautyFiterPaterView(mBeautyVp.context, TYPE_FITER, getViewModel(TYPE_FITER), mListener)
                    if (container.indexOfChild(mBeautyFiterPaterView) == -1) {
                        container.addView(mBeautyFiterPaterView)
                    }
                    return mBeautyFiterPaterView
                    // 滤镜
                } else if (position == 2) {
                    var mBeautyFiterPaterView = BeautyFiterPaterView(mBeautyVp.context, TYPE_PATER, getViewModel(TYPE_PATER), mListener)
                    if (container.indexOfChild(mBeautyFiterPaterView) == -1) {
                        container.addView(mBeautyFiterPaterView)
                    }
                    return mBeautyFiterPaterView
                    // 贴纸
                }
                return super.instantiateItem(container, position)
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "美颜"
                } else if (position == 1) {
                    return "滤镜"
                } else if (position == 2) {
                    return "贴纸"
                }
                return ""
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        mBeautyVp.setAdapter(mPagerAdapter)
        mBeautyTitleStl.setViewPager(mBeautyVp)
        mPagerAdapter?.notifyDataSetChanged()
        mPlaceHolderView = mParentView.findViewById(R.id.place_holder_view)
        mPlaceHolderView?.setOnClickListener(object:DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                hide()
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.beauty_control_panel_view_stub_layout
    }


    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getViewModel(type: Int): List<BeautyViewModel> {
        var mList = mutableListOf<BeautyViewModel>();
        if (type == TYPE_BEAUTY) {
            mList.add(BeautyViewModel(1, "本色", getDrawable("#90DAFF", "#72C1E9")))
            mList.add(BeautyViewModel(2, "少女", getDrawable("#FFB1CF", "#DF8BAB")))
            mList.add(BeautyViewModel(3, "复古", getDrawable("#F9CC82", "#D79F43")))
            mList.add(BeautyViewModel(4, "糖果", getDrawable("#C7E4AC", "#A1C580")))
            mList.add(BeautyViewModel(5, "贵气", getDrawable("#D7ABEE", "#BB81CF")))
        } else if (type == TYPE_FITER) {
            mList.add(BeautyViewModel(1, "黑白", getDrawable("#90DAFF", "#72C1E9")))
            mList.add(BeautyViewModel(2, "怀旧", getDrawable("#FFB1CF", "#DF8BAB")))
            mList.add(BeautyViewModel(3, "光影", getDrawable("#F9CC82", "#D79F43")))
            mList.add(BeautyViewModel(4, "经典", getDrawable("#C7E4AC", "#A1C580")))
        } else if (type == TYPE_PATER) {
            mList.add(BeautyViewModel(1, "小猪", getDrawable("#90DAFF", "#72C1E9")))
            mList.add(BeautyViewModel(2, "耳朵", getDrawable("#FFB1CF", "#DF8BAB")))
            mList.add(BeautyViewModel(3, "小狗", getDrawable("#F9CC82", "#D79F43")))
        }
        return mList
    }


    fun getDrawable(solidColor: String, strokeColor: String): Drawable {
        return DrawableCreator.Builder()
                .setSolidColor(Color.parseColor(solidColor))
                .setStrokeColor(Color.parseColor(strokeColor))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setCornersRadius(U.getDisplayUtils().dip2px(22f).toFloat())
                .build()
    }

    fun show() {
        tryInflate()
        mShowOrHideAnimator?.cancel()

        mShowOrHideAnimator = ObjectAnimator.ofFloat(mParentView,View.TRANSLATION_Y,mParentView.height.toFloat(),0f)
        mShowOrHideAnimator?.setDuration(300)
        mShowOrHideAnimator?.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                mParentView.visibility = View.VISIBLE
            }
        })
        mShowOrHideAnimator?.start()
    }


    fun hide() {
        //tryInflate()
        mShowOrHideAnimator?.cancel()

        mShowOrHideAnimator = ObjectAnimator.ofFloat(mParentView,View.TRANSLATION_Y,0f,mParentView.height.toFloat())
        mShowOrHideAnimator?.setDuration(300)
        mShowOrHideAnimator?.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                mParentView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                mParentView.visibility = View.GONE
            }
        })
        mShowOrHideAnimator?.start()
    }

    override fun onViewDetachedFromWindow(v: View?) {
        super.onViewDetachedFromWindow(v)
        mShowOrHideAnimator?.cancel()
    }

    fun onBackPressed(): Boolean {
        if(mParentView!=null && mParentView.visibility == View.VISIBLE){
            hide()
            return true
        }
        return false
    }

    interface Listener {
        // 美颜改变
        fun onChangeBeauty(id: Int, progress: Int)

        // 滤镜改变
        fun onChangeFiter(id: Int, progress: Int)

        // 贴纸改变
        fun onChangePater(id: Int)
    }

    open class BeautyViewModel(var id: Int // 唯一标识
                               ,
                               var name: String        //显示的文字
                               ,
                               var drawable: Drawable

    )
}
