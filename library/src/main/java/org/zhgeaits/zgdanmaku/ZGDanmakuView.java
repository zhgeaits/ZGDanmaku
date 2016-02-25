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
package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕view
 * 含有一个弹幕池，外面发送弹幕即往弹幕池添加一条弹幕。
 * 包含一条弹幕线程，不断从GL线程的临界区获取弹幕，然后去掉已经跑完的弹幕；
 * 不断从弹幕池获取弹幕，最后整合临界区的弹幕一起更新到临界区。
 * 如果临界区和弹幕池都没有弹幕了，则阻塞，直到新来一条弹幕
 */
public class ZGDanmakuView extends GLSurfaceView {

    private Context mContext;
    private ZGDanmakuRenderer mRenderer;//渲染器
    private int mLines = 4;//默认4行
    private float mLineSpace;//行距
    private Canvas mCanvas;
    private Paint mPainter;
    private boolean isResumed = false;
    private Queue<ZGDanmakuItem> mCachedDanmaku;
    private Map<Integer, ZGDanmaku> mLinesAvaliable;
    protected final AtomicBoolean mWaiting = new AtomicBoolean(false);

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
                start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    /**
     * 启动弹幕线程
     */
    private void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!isResumed) {
                        break;
                    }

                    //读取临界区的弹幕
                    List<ZGDanmaku> rendererList = mRenderer.getDanmakus();
                    List<ZGDanmaku> swapList = new ArrayList<>();
                    for (int i = 0; i < rendererList.size(); i ++) {
                        if(!rendererList.get(i).isFinished()) {
                            swapList.add(rendererList.get(i));
                        }
                    }

                    //读取弹幕池的弹幕
                    int cacheSize;
                    synchronized (mCachedDanmaku) {
                        cacheSize = mCachedDanmaku.size();
                        for (int i = 0; i < cacheSize; i++) {
                            ZGDanmakuItem item = mCachedDanmaku.poll();
                            ZGDanmaku danmaku = generateDanmaku(item);
                            if (danmaku == null) {
                                mCachedDanmaku.offer(item);
                            } else {
                                mRenderer.updateDanmakus(danmaku);
                                swapList.add(danmaku);
                            }
                        }
                    }

                    //如果弹幕池和临界区都为空的时候就阻塞，不然弹幕池和临界区要不停刷新的
                    if(swapList.size() == 0 && cacheSize == 0) {
                        try {
                            synchronized (mWaiting) {
                                mWaiting.set(true);
                                mWaiting.wait();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    //替换临界区的弹幕
                    if(swapList.size() > 0) {
                        mRenderer.setDanmakus(swapList);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * 生成一条弹幕，如果没有可用弹道就返回null
     * @param item
     * @return
     */
    private ZGDanmaku generateDanmaku(ZGDanmakuItem item) {

        int avaliableLine = getAvaliableLine();
        if (avaliableLine == -1) {
            return null;
        }

        ZGDanmaku danmaku = new ZGDanmaku(item.getDanmakuBitmap());
        mLinesAvaliable.put(avaliableLine, danmaku);

        float offsetY = (item.getDanmakuHeight() + mLineSpace) * avaliableLine;
        danmaku.setOffsetY(offsetY);

        return danmaku;
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
     * 设置弹幕是否打开
     * @param isOpen
     */
    public void setOpen(boolean isOpen) {
        mRenderer.setOpen(isOpen);
    }

    /**
     * 弹幕是否打开
     * @return
     */
    public boolean isOpen() {
        return mRenderer.isOpen();
    }

    /**
     * 设置弹幕暂停
     * @param isPaused
     */
    public void setPaused(boolean isPaused) {
        mRenderer.setPaused(isPaused);
    }

    /**
     * 弹幕是否暂停
     * @return
     */
    public boolean isPaused() {
        return mRenderer.isPaused();
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
        synchronized (mCachedDanmaku) {
            mCachedDanmaku.offer(item);
        }

        synchronized (mWaiting) {
            if(mWaiting.get()) {
                mWaiting.set(false);
                mWaiting.notifyAll();
            }
        }
    }

}
