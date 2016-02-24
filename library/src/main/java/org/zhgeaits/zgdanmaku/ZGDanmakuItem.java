package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by zhgeaits on 16/2/24.
 */
public class ZGDanmakuItem {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPainter;
    private Context mContext;

    public ZGDanmakuItem(Canvas canvas, Paint paint, String text) {
        this.mCanvas = canvas;
        this.mPainter = paint;
        setText(text);
    }

    public void setText(String text) {
        mBitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawText(text, 0, 90, mPainter);
    }

    public Bitmap getDanmakuBitmap() {
        return mBitmap;
    }

    public int getDanmakuHeight() {
        if(mBitmap == null) {
            return 0;
        }
        return mBitmap.getHeight();
    }
}
