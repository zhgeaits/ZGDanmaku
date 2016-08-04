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
package org.zhgeaits.zgdanmaku.controller;

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.ZGDanmakuPool;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhgeaits on 16/8/2.
 * 弹幕分发器
 * 1. 不断从GL线程的临界区获取弹幕，然后去掉已经跑完的弹幕；
 * 2. 不断从弹幕池获取弹幕，最后整合临界区的弹幕一起更新到临界区。
 * 如果临界区和弹幕池都没有弹幕了，则阻塞，直到新来一条弹幕
 */
public class ZGDanmakuDispatcher implements Runnable {

    /**
     * 每帧的时间间隔
     */
    private final static int DEFAUTL_FRAME_INTERVAL = 16;

    /**
     * 最大的绘制弹幕数量
     */
    private final static int MAX_READERING_NUMBER = 150;

    private ZGDanmakuPool mDanmakuPool;                     // 弹幕池
    private IZGDanmakuRenderer mRenderer;                   //渲染器
    private SparseArray<ZGDanmaku> mLinesAvaliable;         //可用行
    private String mVertexShader;                           //顶点着色器
    private String mFragmentShader;                         //片元着色器
    private int mLines;                                     //弹幕行数
    private float mLineLeading;                             //行距
    private boolean mStop;                                  //是否暂停
    private long time;                                      //当前时间轴
    private volatile AtomicBoolean mPaused;                 //暂停锁

    public ZGDanmakuDispatcher(ZGDanmakuPool mDanmakuPool, IZGDanmakuRenderer mRenderer,
                               String mVertexShader, String mFragmentShader) {
        mLinesAvaliable = new SparseArray<>();
        this.mDanmakuPool = mDanmakuPool;
        this.mRenderer = mRenderer;
        this.mVertexShader = mVertexShader;
        this.mFragmentShader = mFragmentShader;
        mStop = true;
        mPaused = new AtomicBoolean(false);
    }

    /**
     * 判断某弹道是否可用
     * @param line
     * @return
     */
    private synchronized boolean isLineAvaliable(int line) {
        ZGDanmaku danmaku = mLinesAvaliable.get(line);

        if(danmaku == null) {
            return true;
        }

        if (danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth()) {
            mLinesAvaliable.remove(line);
            return true;
        }

        return false;
    }

    /**
     * 从ZGDanmakuItem生成一个ZGDanmaku
     * @param item
     * @param line
     * @return
     */
    private ZGDanmaku generateDanmaku(ZGDanmakuItem item, int line) {

        ZGDanmaku danmaku = new ZGDanmaku(item.getDanmakuBitmap());
        inValidableLine(line, danmaku);

        float offsetY = (item.getDanmakuHeight() + mLineLeading) * line;
        danmaku.setOffsetY(offsetY);
        danmaku.setViewSize(mRenderer.getViewWidth(), mRenderer.getViewHeight());
        danmaku.setShader(mVertexShader, mFragmentShader);

        return danmaku;
    }

    /**
     * 占用某弹道
     * @param line
     * @param danmaku
     */
    private synchronized void inValidableLine(int line, ZGDanmaku danmaku) {
        mLinesAvaliable.put(line, danmaku);
    }

    /**
     * 清空弹道
     */
    private synchronized void resetLines() {
        mLinesAvaliable.clear();
    }

    /**
     * 设置行距
     * @param leading
     */
    public void setLeading(float leading) {
        this.mLineLeading = leading;
    }

    /**
     * 设置行数
     * @param lines
     */
    public void setLines(int lines) {
        this.mLines = lines;
    }

    /**
     * 停止弹幕
     */
    public void quit() {
        mStop = true;
        resetLines();
        mDanmakuPool.wakeIfNeed();
    }

    /**
     * 是否已经暂停
     * @return
     */
    public boolean isPaused() {
        synchronized (mPaused) {
            return mPaused.get();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        synchronized (mPaused) {
            mPaused.set(true);
        }
    }

    /**
     * 重新启动
     */
    public void resume() {
        synchronized (mPaused) {
            mPaused.set(false);
            mPaused.notifyAll();
        }
    }

    /**
     * 是否已经暂停了
     * @return
     */
    public boolean isStop() {
        return mStop;
    }

    @Override
    public void run() {
        Log.i("ZGDanmaku", "ZGDanmakuDispatcher running");

        resetLines();
        mStop = false;
        time = 0;
        long lastTime, intervalTime;
        long currentTime = SystemClock.elapsedRealtime();
        while (!mStop) {

            //暂停
            synchronized (mPaused) {
                if (mPaused.get()) {
                    Log.i("ZGDanmaku", "dispatcher paused");
                    try {
                        mPaused.wait();
                        mPaused.set(false);
                    } catch (InterruptedException e) {
                    }
                    currentTime = SystemClock.elapsedRealtime();
                    Log.i("ZGDanmaku", "dispatcher resume");
                }
            }

            lastTime = currentTime;
            currentTime = SystemClock.elapsedRealtime();
            intervalTime = currentTime - lastTime;

            if (intervalTime < DEFAUTL_FRAME_INTERVAL) {
                try {
                    Thread.sleep(DEFAUTL_FRAME_INTERVAL - intervalTime);
                } catch (InterruptedException e) {
                }
                currentTime = SystemClock.elapsedRealtime();
                intervalTime = currentTime - lastTime;
            }

            time += intervalTime;

//            Log.i("ZGDanmaku", "ZGDanmakuDispatcher run intervalTime:" + intervalTime + ", time:" + time);

            //读取临界区的弹幕，然后复制到一个新的list，并去掉已经跑出屏幕的弹幕
            //todo 这样性能OK?
            List<ZGDanmaku> rendererList = mRenderer.getRendererDanmakuList();

            //如果临界区没有弹幕,并且弹幕池没有弹幕了,则进行阻塞,这样的能耗低.
            if (rendererList.size() == 0) {
                try {
                    resetLines();
                    if (mDanmakuPool.waitIfNeed()) {
                        currentTime = SystemClock.elapsedRealtime();
                    }
                } catch (InterruptedException e) {
                }
            }

            List<ZGDanmaku> nextRendererList = new ArrayList<>();
            for (int i = 0; i < rendererList.size(); i++) {
                if (!rendererList.get(i).isFinished()) {
                    nextRendererList.add(rendererList.get(i));
                }
            }

            // 处理无效的弹道
            for (int i = 0; i < mLinesAvaliable.size(); i ++) {
                if (!nextRendererList.contains(mLinesAvaliable.get(i))) {
                    mLinesAvaliable.put(i, null);
                }
            }

            //todo 能否预先计算偏移的位置?

            if (nextRendererList.size() + mLines < MAX_READERING_NUMBER) {
                //获取有效的弹道，然后读取弹幕池的弹幕进行绘制
                for (int i = 0; i < mLines; i ++) {
                    if(isLineAvaliable(i)) {
                        //取出时间最小的弹幕
                        ZGDanmakuItem item = mDanmakuPool.poll();
                        if (item != null) {
                            //如果弹幕偏移时间比较小或者是即时发送的弹幕则进行发送.
                            if(item.getOffsetTime() <= time || item.getOffsetTime() == -1) {
                                ZGDanmaku danmaku = generateDanmaku(item, i);
                                nextRendererList.add(danmaku);
                            } else {
                                mDanmakuPool.offer(item);
                            }
                        }

                    }
                }
            }

            //替换临界区的弹幕
            mRenderer.setRendererDanmakuList(nextRendererList);
        }
    }
}
