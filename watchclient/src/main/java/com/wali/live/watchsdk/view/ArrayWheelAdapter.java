
package com.wali.live.watchsdk.view;

/**
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> implements WheelView.WheelAdapter {
    public static final int DEFAULT_LENGTH = -1;

    private T items[];
    private int length;

    public ArrayWheelAdapter(T items[], int length) {
        this.items = items;
        this.length = length;
    }

    public ArrayWheelAdapter(T items[]) {
        this(items, DEFAULT_LENGTH);
    }
    public void setData(T items[]){
        this.items = items;
    }

    @Override
    public String getItem(int index) {
        if (index >= 0 && index < items.length) {
            return items[index].toString();
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return items.length;
    }

    @Override
    public int getMaximumLength() {
        return length;
    }
}
