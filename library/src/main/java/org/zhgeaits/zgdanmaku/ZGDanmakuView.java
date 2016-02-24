package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 */
public class ZGDanmakuView extends GLSurfaceView {

    private Context mContext;
    private ZGDanmakuRenderer mRenderer;//渲染器
    private int mLines = 4;//默认4行
    private float mLineSpace;//行距
    private Canvas mCanvas;
    private Paint mPainter;
    private boolean isInited = false;
    private boolean isPaused = false;
    private Queue<ZGDanmakuItem> mCachedDanmaku;
    private Map<Integer, ZGDanmaku> mLinesAvaliable;

    public ZGDanmakuView(Context context) {
        super(context);
        init(context);
    }

    public ZGDanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mCachedDanmaku = new LinkedList<>();
        mLinesAvaliable = new HashMap<>();

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2); //设置使用OPENGL ES2.0

        mRenderer = new ZGDanmakuRenderer(context);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染

        setSpeed(50);//默认50dp/s速度
        setLineSpace(8);//默认8dp行距

        mCanvas = new Canvas();
        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, 18));
        mPainter.setColor(0xffffffff);
        mPainter.setTextAlign(Paint.Align.LEFT);
        mPainter.setShadowLayer(2, 3, 3, 0x5a000000);

        mRenderer.setListener(new ZGDanmakuRenderer.RenderListener() {
            @Override
            public void onInited() {
                isInited = true;
                showCachedDanmaku();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isPaused) {
                        break;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        break;
                    }
                    showCachedDanmaku();
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    private synchronized void showCachedDanmaku() {
        int size = mCachedDanmaku.size();
        for (int i = 0; i < size; i++) {
            ZGDanmakuItem item = mCachedDanmaku.poll();
            if (!shotDanmakuItem(item)) {
                mCachedDanmaku.offer(item);
            }
        }
    }

    private boolean shotDanmakuItem(ZGDanmakuItem danmakuItem) {

        int avaliableLine = getAvaliableLine();
        if (avaliableLine == -1) {
            return false;
        }

        ZGDanmaku danmaku = new ZGDanmaku(danmakuItem.getDanmakuBitmap());
        mLinesAvaliable.put(avaliableLine, danmaku);

        float offsetY = (danmakuItem.getDanmakuHeight() + mLineSpace) * avaliableLine;
        danmaku.setOffsetY(offsetY);

        mRenderer.addDanmaku(danmaku);

        return true;
    }

    /**
     * 获取有效的弹道
     * @return
     */
    private synchronized int getAvaliableLine() {
        for (int i = 0; i < mLines; i ++) {
            if(mLinesAvaliable.get(i) == null) {
                return i;
            }
            ZGDanmaku danmaku = mLinesAvaliable.get(i);
            if(danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 设置速度
     *
     * @param speed dp/s
     */
    public void setSpeed(float speed) {
        float pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        this.mRenderer.setSpeed(pxSpeed);
    }

    /**
     * 设置行数
     *
     * @param lines
     */
    public void setLines(int lines) {
        this.mLines = lines;
    }

    /**
     * 设置行距
     *
     * @param lineSpace
     */
    public void setLineSpace(int lineSpace) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, lineSpace);
        this.mLineSpace = pxLineSpace;
    }

    /**
     * 发一条弹幕
     *
     * @param text
     */
    public void shotDanmamku(String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(mCanvas, mPainter, text);

        if (!isInited || !shotDanmakuItem(item)) {
            cacheDanmaku(item);
        }
    }

    public synchronized void cacheDanmaku(ZGDanmakuItem item) {
        mCachedDanmaku.offer(item);
    }

}
