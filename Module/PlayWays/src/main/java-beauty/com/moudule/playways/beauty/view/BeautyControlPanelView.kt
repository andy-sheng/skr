package com.moudule.playways.beauty.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.common.utils.U
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.viewpager.SlidingTabLayout

import com.module.playways.R
import kotlinx.android.synthetic.main.beauty_control_panel_view_layout.view.*

/**
 * 美颜、滤镜和贴纸的控制面板
 */
class BeautyControlPanelView : RelativeLayout {

    companion object {
        const val TYPE_BEAUTY = 1     // 美颜
        const val TYPE_FITER = 2      // 滤镜
        const val TYPE_PATER = 3      // 贴纸
    }

    var mPagerAdapter: PagerAdapter? = null

    lateinit var mListener: Listener

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.beauty_control_panel_view_layout, this)
    }

    fun initData(){
        beauty_title_stl.setCustomTabView(R.layout.beauty_tab_view, R.id.tab_tv)
        beauty_title_stl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        beauty_title_stl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        beauty_title_stl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        beauty_title_stl.setIndicatorWidth(U.getDisplayUtils().dip2px(56f))
        beauty_title_stl.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
        beauty_title_stl.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        beauty_title_stl.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        mPagerAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position == 0) {
                    // 美颜
                    var mBeautyFiterPaterView = BeautyFiterPaterView(context, TYPE_BEAUTY, getViewModel(TYPE_BEAUTY), mListener)
                    if (container.indexOfChild(mBeautyFiterPaterView) == -1) {
                        container.addView(mBeautyFiterPaterView)
                    }
                    return mBeautyFiterPaterView
                } else if (position == 1) {
                    var mBeautyFiterPaterView = BeautyFiterPaterView(context, TYPE_FITER, getViewModel(TYPE_FITER), mListener)
                    if (container.indexOfChild(mBeautyFiterPaterView) == -1) {
                        container.addView(mBeautyFiterPaterView)
                    }
                    return mBeautyFiterPaterView
                    // 滤镜
                } else if (position == 2) {
                    var mBeautyFiterPaterView = BeautyFiterPaterView(context, TYPE_PATER, getViewModel(TYPE_PATER), mListener)
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

        beauty_vp.setAdapter(mPagerAdapter)
        beauty_title_stl.setViewPager(beauty_vp)
        mPagerAdapter?.notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
        initData()
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
