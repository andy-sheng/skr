package com.common.view;

import android.graphics.Paint;

public class ExPaint extends Paint {
    public ExPaint() {
        this(0);
    }

    /**
     * Create a new paint with the specified flags. Use setFlags() to change
     * these after the paint is created.
     *
     * @param flags initial flag bits, as if they were passed via setFlags().
     */
    public ExPaint(int flags) {
        super(flags);
    }

    /**
     * Create a new paint, initialized with the attributes in the specified
     * paint parameter.
     *
     * @param paint Existing paint used to initialized the attributes of the
     *              new paint.
     */
    public ExPaint(Paint paint) {
        super(paint);
    }


    @Override
    public void setAntiAlias(boolean aa) {
        /**
         * 调查一下是不是抗锯齿导致的内存溢出
         */
//        if(true){
//            return;
//        }
        super.setAntiAlias(aa);
    }
}
