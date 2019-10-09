package com.common.view.viewpager.arraypageradapter;

import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.R;

import java.util.List;

/**
 * ViewPager adapter that handles View and items.
 * If you want to use View to draw pages, this adapter is useful.
 * Subclasses of this class just need to implement getView() and return a view associated with position and items.
 *
 * @param <T> item type
 */
public abstract class ArrayViewPagerAdapter<T> extends ArrayPagerAdapter<T> {

    public ArrayViewPagerAdapter() {
        super();
    }

    public ArrayViewPagerAdapter(T... items) {
        super(items);
    }

    public ArrayViewPagerAdapter(List<T> items) {
        super(items);
    }

    /**
     * Return the View associated with a specified position and item.
     *
     * @param inflater  inflater
     * @param container ViewGroup that will be container of the view.
     * @param item      item of this page.
     * @param position  position of this page.
     * @return view of this page.
     */
    public abstract View getView(LayoutInflater inflater, ViewGroup container, T item, int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        T item = getItem(position);
        View view = getView(LayoutInflater.from(container.getContext()), container, item, position);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object item) {
        return item.equals(view);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object item) {
        container.removeView((View) item);
    }

    View currentPrimaryView = null;

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        currentPrimaryView = (View) object;
    }

    public View getPrimaryItem() {
        return currentPrimaryView;
    }
}
