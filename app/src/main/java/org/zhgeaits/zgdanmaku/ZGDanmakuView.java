package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 */
public class ZGDanmakuView extends GLSurfaceView {

    private ZGDanmakuRenderer mRenderer;//场景渲染器
    private Canvas mCanvas;
    private Paint mPainter;

    private boolean isInited = false;
    private List<Bitmap> cached = new ArrayList<>();

    public ZGDanmakuView(Context context) {
        super(context);
        init(context);
    }

    public ZGDanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new ZGDanmakuRenderer(context);	//创建场景渲染器
        setRenderer(mRenderer);				//设置渲染器
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染

        mCanvas = new Canvas();
        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        mPainter.setTextSize((int) (18 * fontScale + 0.5f));
        mPainter.setColor(0xffffffff);
        mPainter.setTextAlign(Paint.Align.LEFT);
        mPainter.setShadowLayer(2, 3, 3, 0x5a000000);//0xff000000

        //for test
        mRenderer.setListener(new ZGDanmakuRenderer.RenderListener() {
            @Override
            public void onInited() {
                isInited = true;
                if (cached.size() > 0) {
                    float x = 0;
                    for (Bitmap bitmap : cached) {
                        showBitmap(bitmap, x);
                        x++;
                    }
                    cached.clear();
                }
            }
        });

    }

    public void showBitmap(Bitmap bitmap, float x) {
        ZGDanmaku danmaku = new ZGDanmaku(bitmap);
        danmaku.setOffsetY((int) (x * bitmap.getHeight()));
        mRenderer.addDanmaku(danmaku);
    }

    public void shotDanmamku(String text) {
        //通过输入流加载图片===============begin===================
        InputStream is = this.getResources().openRawResource(R.drawable.test);
        Bitmap bitmapTmp;
        try {
            bitmapTmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通过输入流加载图片===============end=====================

        if(isInited) {
            showBitmap(bitmapTmp, 0);
        } else {
            cached.add(bitmapTmp);
        }

        Bitmap textBitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(textBitmap);
        mCanvas.drawText(text, 0, 90, mPainter);

        if(isInited) {
            showBitmap(textBitmap, 0);
        } else {
            cached.add(textBitmap);
        }
    }


}
