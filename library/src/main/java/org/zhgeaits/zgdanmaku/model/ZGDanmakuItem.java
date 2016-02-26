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

import org.zhgeaits.zgdanmaku.utils.DimensUtils;

/**
 * Created by zhgeaits on 16/2/24.
 * 弹幕包装类
 * 每个弹幕就是一个bitmap
 * 可以从这里进行扩展
 */
public class ZGDanmakuItem {

    private Bitmap mBitmap;
    private String mText;
    private Canvas mCanvas;
    private Paint mPainter;
    private Context mContext;

    public ZGDanmakuItem(String text, Context context) {
        this.mText = text;
        this.mContext = context;
        initDefaultPainters();
    }

    private void initDefaultPainters() {
        mCanvas = new Canvas();
        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, 18));
        mPainter.setColor(0xffffffff);
        mPainter.setTextAlign(Paint.Align.LEFT);
        mPainter.setShadowLayer(2, 3, 3, 0x5a000000);
    }

    public void setPainters(Canvas canvas, Paint paint) {
        this.mCanvas = canvas;
        this.mPainter = paint;
    }

    public Bitmap getDanmakuBitmap() {
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawText(mText, 0, 90, mPainter);
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
}
