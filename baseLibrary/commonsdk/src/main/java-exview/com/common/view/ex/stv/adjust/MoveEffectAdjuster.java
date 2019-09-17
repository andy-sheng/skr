/*
 * Copyright (C) 2017 CoorChice <icechen_@outlook.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * <p>
 * Last modified 17-4-17 下午3:23
 */

package com.common.view.ex.stv.adjust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.common.base.R;
import com.common.log.MyLog;
import com.common.view.ex.stv.SuperTextView;


/**
 * Project Name:SuperTextView
 * Author:CoorChice
 * Date:2017/4/17
 * Notes:
 */

public class MoveEffectAdjuster extends SuperTextView.Adjuster {

  private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
  private float totalWidth = 50f;
  private float startLocation = -99999f;
  private Paint paint = new Paint();
  private Path firstLinePath = new Path();
  private Path secondLinePath = new Path();
  private RectF rectF = new RectF();
  private float density;

  private Bitmap src;
  private Canvas srcCanvas;
  private Bitmap dst;
  private Canvas dstCanvas;
  private Bitmap render;
  private Canvas renderCanvas;


  @Override
  public void adjust(SuperTextView v, Canvas canvas) {
    MyLog.d("MoveEffectAdjuster","adjust" + " v=" + v + " canvas=" + canvas);

    int width = v.getWidth();
    int height = v.getHeight();
    if (density == 0) {
      density = v.getResources().getDisplayMetrics().density;
    }
    if (startLocation == -99999f) {
      startLocation = (float) (Math.random() * width);
    }
    if (startLocation < -(totalWidth * density + height * Math.tan(60))) {
      startLocation = width;
    }
    if (renderCanvas == null){
      render = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      renderCanvas = new Canvas(render);
    }

    if (dstCanvas == null){
      dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      dstCanvas = new Canvas(dst);
    }
    int firstLineWidth = (int) (totalWidth * density / 5 * 3);
    double velocity = 1.5 * density;
    startLocation = ((float) (startLocation - velocity));
    firstLinePath.reset();
    firstLinePath.moveTo(startLocation, height);
    firstLinePath.lineTo(startLocation + firstLineWidth, height);
    firstLinePath.lineTo((float) (startLocation + firstLineWidth + height * Math.tan(60)), 0);
    firstLinePath.lineTo((float) (startLocation + height * Math.tan(60)), 0);
    firstLinePath.close();
    paint.setAntiAlias(true);
    paint.setDither(true);
    paint.setColor(v.getResources().getColor(R.color.black));
    paint.setStyle(Paint.Style.FILL);
    int secondLineWidth = (int) (totalWidth * density / 5);
    secondLinePath.reset();
    secondLinePath.moveTo(startLocation + secondLineWidth * 4, height);
    secondLinePath.lineTo(startLocation + secondLineWidth * 4 + secondLineWidth, height);
    secondLinePath.lineTo(
        (float) (startLocation + secondLineWidth * 4 + secondLineWidth + height * Math.tan(60)),
        0);
    secondLinePath.lineTo((float) (startLocation + secondLineWidth * 4 + height * Math.tan(60)),
        0);
    secondLinePath.close();

    firstLinePath.addPath(secondLinePath);
    rectF.setEmpty();
    rectF.set(0, 0, width, height);

    if (srcCanvas == null){
      src = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      srcCanvas = new Canvas(src);

      paint.setColor(v.getResources().getColor(R.color.white));
      srcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
      srcCanvas.drawRoundRect(rectF, height / 2, height / 2, paint);
    }

    paint.setColor(Color.parseColor("#bf000000"));
    dstCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    dstCanvas.drawPath(firstLinePath, paint);

    renderCanvas.drawBitmap(src, 0, 0, paint);
    paint.setXfermode(xfermode);
    renderCanvas.drawBitmap(dst, 0, 0, paint);
    paint.setXfermode(null);
    canvas.drawBitmap(render, 0, 0, paint);
  }
}
