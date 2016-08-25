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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import org.zhgeaits.zgdanmaku.utils.BitmapPool;
import org.zhgeaits.zgdanmaku.utils.DimensUtils;
import org.zhgeaits.zgdanmaku.utils.NativeBitmapFactory;
import org.zhgeaits.zgdanmaku.utils.ZGLog;
import org.zhgeaits.zgdanmaku.utils.ZGTimer;


/**
 * Created by zhgeaits on 16/2/24.
 * 弹幕包装类
 * 每个弹幕就是一个bitmap
 * 可以从这里进行扩展
 */
public class ZGDanmakuItem implements Comparable<ZGDanmakuItem> {

    public final static float BILI_PLAYER_WIDTH = 682;
    public final static long COMMON_DANMAKU_DURATION = 5800; // B站原始分辨率下弹幕存活时间
    public final static long MIN_DANMAKU_DURATION = 6000;
    public final static long MAX_DANMAKU_DURATION_HIGH_DENSITY = 11000;
    public long REAL_DANMAKU_DURATION = COMMON_DANMAKU_DURATION;
    public long MAX_DANMAKU_DURATION = MIN_DANMAKU_DURATION;

    private Bitmap mBitmap;
    private String mText;
    private Canvas mCanvas;
    private Paint mPainter;
    private Paint mStrokePainter;
    private Context mContext;
    private boolean isStroke;//是否描边
    private long id;
    private int mHeadIcon;
    private int mBackground;
    private float mDetalX;
    private float pxSpeed;
    private float dpSpeed;
    private float mTextSize;
    private long mOffsetTime;//出现的时间
    private long mLateTime;//最迟出现的时间

    public ZGDanmakuItem(long id, String text) {
        this.id = id;
        this.mText = text;
        this.mOffsetTime = ZGTimer.getInstance().getTime();
        this.mLateTime = Long.MAX_VALUE;
        this.mTextSize = 20;
        this.isStroke = true;
        this.pxSpeed = -1;
        this.dpSpeed = -1;
        initDefaultPainters();
    }

    /**
     * 初始化画笔
     */
    private void initDefaultPainters() {
        mCanvas = new Canvas();

        mPainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setColor(0xFFFFFFFF);
        mPainter.setTextAlign(Paint.Align.LEFT);

        mStrokePainter = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mStrokePainter.setColor(0xFF000000);
        mStrokePainter.setTextAlign(Paint.Align.LEFT);
        mStrokePainter.setStyle(Paint.Style.STROKE);
        mStrokePainter.setStrokeWidth(4.0f);
        mStrokePainter.setShadowLayer(4, 0, 0, 0xFF000000);
    }

    /**
     * 设置context
     *
     * @param context
     */
    public void setContext(Context context) {
        this.mContext = context;
        if (mPainter == null) {
            initDefaultPainters();
        }
        mPainter.setTextSize(DimensUtils.sp2pixel(mContext, mTextSize));
        mStrokePainter.setTextSize(DimensUtils.sp2pixel(mContext, mTextSize));
        this.pxSpeed = DimensUtils.dip2pixel(mContext, dpSpeed);
    }

    /**
     * 设置弹幕的时间
     *
     * @param time 单位ms
     */
    public void setOffsetTime(long time) {
        this.mOffsetTime = time;
        this.mLateTime = mOffsetTime + 5000;
    }

    /**
     * 设置文字颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        if (mPainter == null) {
            initDefaultPainters();
        }
        mPainter.setColor(color);
    }

    /**
     * 设置文字大小
     *
     * @param size
     */
    public void setTextSize(float size) {
        this.mTextSize = size;
    }

    /**
     * 设置头像
     *
     * @param resId
     */
    public void setHeadIcon(int resId) {
        this.mHeadIcon = resId;
    }

    /**
     * 设置背景
     * @param resId
     */
    public void setBackground(int resId) {
        this.mBackground = resId;
    }

    /**
     * 设置画笔
     *
     * @param canvas
     * @param paint
     */
    public void setPainters(Canvas canvas, Paint paint) {
        this.mCanvas = canvas;
        this.mPainter = paint;
    }

    /**
     * 设置是否描边
     * @param isStroke
     */
    public void setStroke(boolean isStroke) {
        this.isStroke = isStroke;
    }

    public String getText() {
        return mText;
    }

    /**
     * 获取即时出现时间
     *
     * @return
     */
    public long getOffsetTime() {
        return mOffsetTime;
    }

    /**
     * 获取最迟出现时间
     *
     * @return
     */
    public long getLateTime() {
        return mLateTime;
    }

    /**
     * 获取弹幕高
     *
     * @return
     */
    public int getDanmakuHeight() {
        getDanmakuBitmap();
        if (mBitmap == null) {
            return 0;
        }
        return mBitmap.getHeight();
    }

    /**
     * 获取弹幕宽
     *
     * @return
     */
    public int getDanmakuWidth() {
        getDanmakuBitmap();
        if (mBitmap == null) {
            return 0;
        }
        return mBitmap.getWidth();
    }

    /**
     * 测量弹幕高度
     * @return
     */
    public int measureTextHeight() {
        int baseline = (int) (-mPainter.ascent() + 0.5f);
        int height = (int) (mPainter.descent() + baseline + 0.5f);
        return height;
    }

    /**
     * 获取Bitmap
     *
     * @return
     */
    public Bitmap getDanmakuBitmap() {
        if (mBitmap == null) {
            int baseline = (int) (-mPainter.ascent() + 0.5f);
            int height = (int) (mPainter.descent() + baseline + 0.5f);
            int width = (int) (mPainter.measureText(mText) + 0.5f);
            float textX = 0, textY = baseline;
            float iconX = 0, iconY = 0;
            int bgLeft = 0, bgTop = 0, bgRight = width, bgBottom = height;
            int gap = 4;//icon跟文字之间4个像素间距
            int padding = 20;//背景的padding

            try {
                if (height > 0 && width > 0) {

                    Bitmap icon = null;
                    if (mHeadIcon > 0) {
                        try {
                            icon = BitmapFactory.decodeResource(mContext.getResources(), mHeadIcon);
                        } catch (OutOfMemoryError e) {
                            ZGLog.e("getDanmakuBitmap decode icon oom:", e);
                        }
                    }

                    //如果有背景,则重新计算宽高和x,y坐标
                    if (mBackground > 0) {
                        width += padding * 2;
                        height += padding * 2;
                        textX += padding;
                        textY += padding;
                        bgRight = width;
                        bgBottom = height;
                    }

                    //如果有头衔icon则重新计算宽高,并计算坐标
                    if (icon != null) {
                        width += icon.getWidth() + gap;

                        textX += icon.getWidth() + gap;
                        if (icon.getHeight() > height) {
                            textY = (textY + (icon.getHeight() - height) / 2.0f);
                        } else {
                            iconY = ((height - icon.getHeight()) / 2.0f);
                        }

                        height = Math.max(icon.getHeight(), height);

                        bgLeft += icon.getWidth() + gap;
                        bgRight = width;
                        bgBottom = height;
                    }

                    //这里用到了ARGB_8888, 用RGB565会没有透明的.注意要和GLES20.glTexImage2D对应,不然会崩的
                    mBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mCanvas.setBitmap(mBitmap);

                    //画icon
                    if (icon != null) {
                        mCanvas.drawBitmap(icon, iconX, iconY, mPainter);
                    }

                    //画背景
                    if (mBackground > 0) {
                        try {
                            Drawable drawable = mContext.getResources().getDrawable(mBackground);
                            if (drawable != null) {
                                drawable.setBounds(bgLeft, bgTop, bgRight, bgBottom);
                                drawable.draw(mCanvas);
                            }
                        } catch (OutOfMemoryError e) {
                            ZGLog.e("getDanmakuBitmap decode bg oom:", e);
                        }
                    }

                    if (isStroke) {
                        //描边
                        mCanvas.drawText(mText, textX, textY, mStrokePainter);
                    }
                    //写文字
                    mCanvas.drawText(mText, textX, textY, mPainter);
                }
            } catch (OutOfMemoryError e) {
                ZGLog.e("getDanmakuBitmap oom:", e);
            }
        }
        return mBitmap;
    }

    private Bitmap createBitmap(int width, int height, Bitmap.Config config) {
//        Bitmap bitmap = null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        options.outHeight = height;
//        options.outWidth = width;
//        Bitmap inBitmap = BitmapPool.getInstance().getBitmapFromReusableSet(options);
//        if (inBitmap != null) {
//            bitmap = Bitmap.createBitmap(inBitmap, 0, 0, width, height);
//            bitmap.eraseColor(0x00000000);
//        } else {
//            bitmap = NativeBitmapFactory.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        }
        Bitmap bitmap = NativeBitmapFactory.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    /**
     * 计算每1ms的步长
     *
     * @param width
     * @param height
     * @param viewportSizeFactor
     */
    public void updateDetalX(int width, int height, float viewportSizeFactor) {
        REAL_DANMAKU_DURATION = (long) (COMMON_DANMAKU_DURATION * (viewportSizeFactor
                * width / BILI_PLAYER_WIDTH));
        REAL_DANMAKU_DURATION = Math.min(MAX_DANMAKU_DURATION_HIGH_DENSITY,
                REAL_DANMAKU_DURATION);
        REAL_DANMAKU_DURATION = Math.max(MIN_DANMAKU_DURATION, REAL_DANMAKU_DURATION);

        MAX_DANMAKU_DURATION = Math.max(COMMON_DANMAKU_DURATION, MAX_DANMAKU_DURATION);
        MAX_DANMAKU_DURATION = Math.max(REAL_DANMAKU_DURATION, MAX_DANMAKU_DURATION);

        calculateDetal(width, height);
    }

    /**
     * 计算每帧的步长
     */
    private void calculateDetal(int width, int height) {
        mDetalX = (float) (getDanmakuWidth() + width) / (float) MAX_DANMAKU_DURATION;
    }

    /**
     * 获取每毫秒的步长
     *
     * @return
     */
    public float getDetalX() {
        if (pxSpeed > 0) {
            return pxSpeed / 1000.0f;
        }
        return mDetalX;
    }

    /**
     * 设置速度,单位dp/s
     * @param speed
     */
    public void setSpeed(float speed) {
        if (mContext != null) {
            this.pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        }
        this.dpSpeed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        ZGDanmakuItem item = (ZGDanmakuItem) o;
        return item.id == id;
    }

    @Override
    public int compareTo(ZGDanmakuItem another) {
        if (another == null) {
            return 0;
        }
        return (int) (mOffsetTime - another.mOffsetTime);
    }
}
