//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.emoticon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import io.rong.imkit.R;
import io.rong.imkit.utilities.RongUtils;

public class EmoticonTabAdapter {
    private View mContainer;
    private IEmoticonTab mCurrentTab;
    private ViewPager mViewPager;
    private io.rong.imkit.emoticon.EmoticonTabAdapter.TabPagerAdapter mAdapter;
    private ViewGroup mScrollTab;
    private int selected = 0;
    private View mTabAdd;
    private View mTabSetting;
    private boolean mTabBarEnabled = true;
    private boolean mInitialized;
    private boolean mAddEnabled = false;
    private boolean mSettingEnabled = false;
    private IEmoticonClickListener mEmoticonClickListener;
    private IEmoticonSettingClickListener mEmoticonSettingClickListener;
    private LinkedHashMap<String, List<IEmoticonTab>> mEmotionTabs = new LinkedHashMap();
    private View extraTabBarItem;
    private OnClickListener tabClickListener = new OnClickListener() {
        public void onClick(View v) {
            int count = EmoticonTabAdapter.this.mScrollTab.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; ++i) {
                    if (v.equals(EmoticonTabAdapter.this.mScrollTab.getChildAt(i))) {
                        EmoticonTabAdapter.this.mViewPager.setCurrentItem(i);
                        break;
                    }
                }
            }

        }
    };

    public EmoticonTabAdapter() {
    }

    public boolean isInitialized() {
        return this.mInitialized;
    }

    public void setOnEmoticonClickListener(IEmoticonClickListener listener) {
        this.mEmoticonClickListener = listener;
    }

    public void setOnEmoticonSettingClickListener(IEmoticonSettingClickListener listener) {
        this.mEmoticonSettingClickListener = listener;
    }

    public void setCurrentTab(IEmoticonTab tab, String tag) {
        if (this.mEmotionTabs.containsKey(tag)) {
            this.mCurrentTab = tab;
            if (this.mAdapter != null && this.mViewPager != null) {
                int index = this.getIndex(tab);
                if (index >= 0) {
                    this.mViewPager.setCurrentItem(index);
                    this.mCurrentTab = null;
                }
            }
        }

    }

    public void bindView(ViewGroup viewGroup) {
        this.mInitialized = true;
        this.mContainer = this.initView(viewGroup.getContext(), viewGroup);
    }

    public void initTabs(List<IEmoticonTab> tabs, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            this.mEmotionTabs.put(tag, tabs);
        }
    }

    public void refreshTabIcon(IEmoticonTab tab, Drawable drawable) {
        int index = this.getIndex(tab);
        if (index >= 0) {
            View child = this.mScrollTab.getChildAt(index);
            ImageView iv = (ImageView) child.findViewById(R.id.rc_emoticon_tab_iv);
            iv.setImageDrawable(drawable);
        }

    }

    public boolean addTab(int index, IEmoticonTab tab, String tag) {
        List<IEmoticonTab> tabs = this.mEmotionTabs.get(tag);
        int idx;
        if (tabs == null) {
            tabs = new ArrayList();
            tabs.add(tab);
            this.mEmotionTabs.put(tag, tabs);
        } else {
            idx = tabs.size();
            if (index > idx) {
                return false;
            }

            tabs.add(index, tab);
        }

        idx = this.getIndex(tab);
        if (this.mAdapter != null && this.mViewPager != null) {
            View view = this.getTabIcon(this.mViewPager.getContext(), tab);
            this.mScrollTab.addView(view, idx);
            this.mAdapter.notifyDataSetChanged();
            this.mViewPager.setCurrentItem(idx <= this.selected ? this.selected + 1 : this.selected);
        }

        return true;
    }

    public void addTab(IEmoticonTab tab, String tag) {
        List<IEmoticonTab> tabs = this.mEmotionTabs.get(tag);
        if (tabs == null) {
            tabs = new ArrayList();
            tabs.add(tab);
            this.mEmotionTabs.put(tag, tabs);
        } else {
            tabs.add(tab);
        }

        int idx = this.getIndex(tab);
        if (this.mAdapter != null && this.mViewPager != null) {
            View view = this.getTabIcon(this.mViewPager.getContext(), tab);
            this.mScrollTab.addView(view, idx);
            this.mAdapter.notifyDataSetChanged();
            this.mViewPager.setCurrentItem(idx <= this.selected ? this.selected + 1 : this.selected);
        }

    }

    public List<IEmoticonTab> getTagTabs(String tag) {
        return this.mEmotionTabs.get(tag);
    }

    public int getTagTabIndex(String tag) {
        Set<String> keys = this.mEmotionTabs.keySet();
        List<String> list = new ArrayList();
        list.addAll(keys);
        return list.indexOf(tag);
    }

    private int getIndex(IEmoticonTab tab) {
        return this.getAllTabs().indexOf(tab);
    }

    private List<IEmoticonTab> getAllTabs() {
        Collection<List<IEmoticonTab>> c = this.mEmotionTabs.values();
        List<IEmoticonTab> list = new ArrayList();
        Iterator var3 = c.iterator();

        while (var3.hasNext()) {
            List<IEmoticonTab> tabs = (List) var3.next();

            for (int i = 0; tabs != null && i < tabs.size(); ++i) {
                list.add(tabs.get(i));
            }
        }

        return list;
    }

    public LinkedHashMap<String, List<IEmoticonTab>> getTabList() {
        return this.mEmotionTabs;
    }

    private IEmoticonTab getTab(int index) {
        return this.getAllTabs().get(index);
    }

    public boolean removeTab(IEmoticonTab tab, String tag) {
        if (!this.mEmotionTabs.containsKey(tag)) {
            return false;
        } else {
            boolean result = false;
            List<IEmoticonTab> list = this.mEmotionTabs.get(tag);
            int index = this.getIndex(tab);
            if (list.remove(tab)) {
                this.mScrollTab.removeViewAt(index);
                this.mAdapter.notifyDataSetChanged();
                result = true;
                if (this.selected == index) {
                    this.mViewPager.setCurrentItem(this.selected);
                    this.onPageChanged(-1, this.selected);
                }
            }

            return result;
        }
    }

    public void setVisibility(int visibility) {
        if (this.mContainer != null) {
            if (visibility == 0) {
                this.mContainer.setVisibility(View.VISIBLE);
            } else {
                this.mContainer.setVisibility(View.GONE);
            }
        }

    }

    public int getVisibility() {
        return this.mContainer != null ? this.mContainer.getVisibility() : 8;
    }

    public void setTabViewEnable(boolean enable) {
        this.mTabBarEnabled = enable;
    }

    public void setAddEnable(boolean enable) {
        this.mAddEnabled = enable;
        if (this.mTabAdd != null) {
            this.mTabAdd.setVisibility(enable ? View.VISIBLE : View.GONE);
        }

    }

    public void setSettingEnable(boolean enable) {
        this.mSettingEnabled = enable;
        if (this.mTabSetting != null) {
            this.mTabSetting.setVisibility(enable ? View.VISIBLE : View.GONE);
        }

    }

    public void addExtraTab(Context context, Drawable drawable, OnClickListener clickListener) {
        this.extraTabBarItem = this.getTabIcon(context, drawable);
        this.extraTabBarItem.setOnClickListener(clickListener);
    }

    private View initView(Context context, ViewGroup parent) {
        View container = LayoutInflater.from(context).inflate(R.layout.rc_ext_emoticon_tab_container, null);
        Integer height = (int) context.getResources().getDimension(R.dimen.rc_extension_board_height);
        container.setLayoutParams(new LayoutParams(-1, height));
        this.mViewPager = (ViewPager) container.findViewById(R.id.rc_view_pager);
        this.mScrollTab = (ViewGroup) container.findViewById(R.id.rc_emotion_scroll_tab);
        this.mTabAdd = container.findViewById(R.id.rc_emoticon_tab_add);
        this.mTabAdd.setVisibility(this.mAddEnabled ? View.VISIBLE : View.GONE);
        this.mTabAdd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (EmoticonTabAdapter.this.mEmoticonClickListener != null) {
                    EmoticonTabAdapter.this.mEmoticonClickListener.onAddClick(v);
                }

            }
        });
        this.mTabSetting = container.findViewById(R.id.rc_emoticon_tab_setting);
        this.mTabSetting.setVisibility(this.mSettingEnabled ? View.VISIBLE : View.GONE);
        this.mTabSetting.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (EmoticonTabAdapter.this.mEmoticonSettingClickListener != null) {
                    EmoticonTabAdapter.this.mEmoticonSettingClickListener.onSettingClick(v);
                }

            }
        });
        LinearLayout tabBar = (LinearLayout) container.findViewById(R.id.rc_emotion_tab_bar);
        if (this.mTabBarEnabled) {
            tabBar.setVisibility(View.VISIBLE);
            if (this.extraTabBarItem != null && this.mAddEnabled) {
                tabBar.addView(this.extraTabBarItem, 1);
            }
        } else {
            tabBar.setVisibility(View.GONE);
        }

        Iterator var6 = this.getAllTabs().iterator();

        while (var6.hasNext()) {
            IEmoticonTab tab = (IEmoticonTab) var6.next();
            View view = this.getTabIcon(context, tab);
            this.mScrollTab.addView(view);
        }

        this.mAdapter = new EmoticonTabAdapter.TabPagerAdapter();
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(6);
        this.mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                EmoticonTabAdapter.this.onPageChanged(EmoticonTabAdapter.this.selected, position);
                EmoticonTabAdapter.this.selected = position;
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        int index;
        if (this.mCurrentTab != null && (index = this.getIndex(this.mCurrentTab)) >= 0) {
            this.mCurrentTab = null;
            this.onPageChanged(-1, index);
            this.mViewPager.setCurrentItem(index);
        } else {
            this.onPageChanged(-1, 0);
        }

        parent.addView(container);
        return container;
    }

    private View getTabIcon(Context context, IEmoticonTab tab) {
        Drawable drawable = tab.obtainTabDrawable(context);
        return this.getTabIcon(context, drawable);
    }

    private View getTabIcon(Context context, Drawable drawable) {
        View item = LayoutInflater.from(context).inflate(R.layout.rc_ext_emoticon_tab_item, null);
        item.setLayoutParams(new LayoutParams(RongUtils.dip2px(60.0F), RongUtils.dip2px(36.0F)));
        ImageView iv = (ImageView) item.findViewById(R.id.rc_emoticon_tab_iv);
        iv.setImageDrawable(drawable);
        item.setOnClickListener(this.tabClickListener);
        return item;
    }

    private void onPageChanged(int pre, int cur) {
        int count = this.mScrollTab.getChildCount();
        if (count > 0 && cur < count) {
            ViewGroup curView;
            if (pre >= 0 && pre < count) {
                curView = (ViewGroup) this.mScrollTab.getChildAt(pre);
                curView.setBackgroundColor(0);
            }

            if (cur >= 0) {
                curView = (ViewGroup) this.mScrollTab.getChildAt(cur);
                curView.setBackgroundColor(Color.rgb(215, 215, 215));
                int w = curView.getMeasuredWidth();
                if (w != 0) {
                    int screenW = RongUtils.getScreenWidth();
                    if (this.mAddEnabled) {
                        int addW = this.mTabAdd.getMeasuredWidth();
                        screenW -= addW;
                    }

                    HorizontalScrollView scrollView = (HorizontalScrollView) this.mScrollTab.getParent();
                    int scrollX = scrollView.getScrollX();
                    int offset = scrollX - scrollX / w * w;
                    if (cur * w < scrollX) {
                        scrollView.smoothScrollBy(offset == 0 ? -w : -offset, 0);
                    } else if (cur * w - scrollX > screenW - w) {
                        scrollView.smoothScrollBy(w - offset, 0);
                    }
                }
            }
        }

        if (cur >= 0 && cur < count) {
            IEmoticonTab curTab = this.getTab(cur);
            if (curTab != null) {
                curTab.onTableSelected(cur);
            }
        }

    }

    private class TabPagerAdapter extends PagerAdapter {
        private TabPagerAdapter() {
        }

        public int getCount() {
            return EmoticonTabAdapter.this.getAllTabs().size();
        }

        public View instantiateItem(ViewGroup container, int position) {
            IEmoticonTab tab = EmoticonTabAdapter.this.getTab(position);
            View view = tab.obtainTabPager(container.getContext());
            if (view.getParent() == null) {
                container.addView(view);
            }

            return view;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;
            container.removeView(layout);
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int getItemPosition(Object object) {
            return -2;
        }
    }
}
