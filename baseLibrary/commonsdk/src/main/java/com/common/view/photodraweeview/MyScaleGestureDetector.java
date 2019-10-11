package com.common.view.photodraweeview;

import android.content.Context;
import android.os.Handler;
import android.view.ScaleGestureDetector;

import java.lang.reflect.Field;

public class MyScaleGestureDetector extends ScaleGestureDetector {
    public MyScaleGestureDetector(Context context, OnScaleGestureListener listener) {
        super(context, listener);
        changeMinSpan();
    }

    public MyScaleGestureDetector(Context context, OnScaleGestureListener listener, Handler handler) {
        super(context, listener, handler);
        changeMinSpan();
    }

    private void changeMinSpan() {
        try {
            Class<?> obj = Class.forName("android.view.ScaleGestureDetector");
            Field[] f = obj.getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mMinSpan")) {
                    field.setAccessible(true);
                    field.set(this, 0);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
