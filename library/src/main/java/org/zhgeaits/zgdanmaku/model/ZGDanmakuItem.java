/*
 * Copyright (C) 2016 Zhang Ge <zhgeaits@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zhgeaits.zgdanmaku.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import org.zhgeaits.zgdanmaku.utils.DimensUtils;
import org.zhgeaits.zgdanmaku.utils.NativeBitmapFactory;

/**
 * Created by zhgeaits on 16/2/24.
 * 弹幕包装类
 * 每个弹幕就是一个bitmap
 * 可以从这里进行扩展
 */
public class ZGDanmakuItem implements Comparable<ZGDanmakuItem> {

    private Bitmap mBitmap;
    public String mText;
    private Canvas mCanvas;
    private Paint mPainter;
    private Paint mStrokePainter;
    private Context mContext;
    private long mOffsetTime;//出现的时间
    private long mLateTime;//最迟出现的时间

    public ZGDanmakuItem(String text, Context context) {
        this.mText = text;
        this.mContext = context;
        this.mOffsetTime = -1;
        this.mLateTime = Long.MAX_VALUE;
        initDefaultPainters();
    }

    public ZGDanmakuItem(String text, Context context, long time) {
        this.mText = text;
        this.mContext = context;
        this.mOffsetTime = time;
        this.mLateTime = mOffsetTime + 5000;
        initDefaultPainters();
    }

    private void initDefaultPainters() {
        mCanvas = new Canvas();

        mPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, 20));
        mPainter.setColor(0xFFFFFFFF);
        mPainter.setTextAlign(Paint.Align.LEFT);
//        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
//        mPainter.setTypeface(font);

        mStrokePainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mStrokePainter.setTextSize(DimensUtils.dip2pixel(mContext, 20));
        mStrokePainter.setColor(0xFF000000);
        mStrokePainter.setTextAlign(Paint.Align.LEFT);
        mStrokePainter.setStyle(Paint.Style.STROKE);
        mStrokePainter.setStrokeWidth(4.0f);
        mStrokePainter.setShadowLayer(4, 0, 0, 0xFF000000);
//        mStrokePainter.setTypeface(font);
    }

    public void setTextColor(int color) {
        if (mPainter == null) {
            initDefaultPainters();
        }
        mPainter.setColor(color);
    }

    public void  setTextSize(float size) {
        if (mPainter == null) {
            initDefaultPainters();
        }
        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, size));
    }

    public void setPainters(Canvas canvas, Paint paint) {
        this.mCanvas = canvas;
        this.mPainter = paint;
    }

    /**
     * 获取展示的时间
     * @return
     */
    public long getOffsetTime() {
        return mOffsetTime;
    }

    public long getLateTime() {
        return mLateTime;
    }

    public Bitmap getDanmakuBitmap() {
        if(mBitmap == null) {
            int baseline = (int) (-mPainter.ascent() + 0.5f);
            int height = (int) (mPainter.descent() + baseline + 0.5f);
            int width = (int) (mPainter.measureText(mText) + 0.5f);

            if(height > 0 && width > 0) {
                //这里用到了ARGB_8888, 用RGB565会没有透明的.注意要和GLES20.glTexImage2D对应,不然会崩的
                mBitmap = NativeBitmapFactory.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas.setBitmap(mBitmap);
                mCanvas.drawText(mText, 0, baseline, mStrokePainter);
                mCanvas.drawText(mText, 0, baseline, mPainter);
            }
        }
        return mBitmap;
    }

    public int getDanmakuHeight() {
        getDanmakuBitmap();
        if(mBitmap == null) {
            return 0;
        }
        return mBitmap.getHeight();
    }

    @Override
    public int compareTo(ZGDanmakuItem another) {
        if (another == null) {
            return 0;
        }
        return (int) (mOffsetTime - another.mOffsetTime);
    }
}
