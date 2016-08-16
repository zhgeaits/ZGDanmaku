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
import android.util.SparseArray;

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.ZGDanmakuPool;
import org.zhgeaits.zgdanmaku.utils.ZGLog;
import org.zhgeaits.zgdanmaku.utils.ZGTimer;
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
     * 原本应该16ms对应60帧的,不过,这里可以再设置小一些
     */
    private final static int DEFAUTL_FRAME_INTERVAL = 10;

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
    private volatile AtomicBoolean mPaused;                 //暂停锁
    private boolean isSeek;                                 //是否执行了seek

    public ZGDanmakuDispatcher(ZGDanmakuPool mDanmakuPool, IZGDanmakuRenderer mRenderer,
                               String mVertexShader, String mFragmentShader) {
        mLinesAvaliable = new SparseArray<ZGDanmaku>();
        this.mDanmakuPool = mDanmakuPool;
        this.mRenderer = mRenderer;
        this.mVertexShader = mVertexShader;
        this.mFragmentShader = mFragmentShader;
        mStop = true;
        mPaused = new AtomicBoolean(false);
    }

    /**
     * 判断某弹道是否可用
     *
     * @param line
     * @return
     */
    private synchronized boolean isLineAvaliable(int line) {

        ZGDanmaku danmaku = mLinesAvaliable.get(line);

        if (danmaku == null) {
            return true;
        }

        if (danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth()) {
            mLinesAvaliable.remove(line);
            return true;
        }

        return false;
    }

    /**
     * 寻找一条追不上的弹道
     * @return
     */
    private synchronized int findFittestLine(ZGDanmakuItem item) {

        int line = -1;
        item.updateDetalX(mRenderer.getViewWidth(), mRenderer.getViewHeight(), mRenderer.getViewportSizeFactor());
        float scrollTime = mRenderer.getViewWidth() / item.getDetalX();

        for (int i = 0; i < mLines; i++) {
            ZGDanmaku danmaku = mLinesAvaliable.get(i);

            if (danmaku == null) {
                return i;
            }

            if (danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth() + mLineLeading * 5) {
                if (item.getDetalX() <= danmaku.getDetalX()) {
                    line = i;
                    break;
                }
                float preTime = (mRenderer.getViewWidth() - danmaku.getCurrentOffsetX() + danmaku.getDanmakuWidth()) / danmaku.getDetalX();
                float time = Math.min(preTime, scrollTime);
                float speed = item.getDetalX() - danmaku.getDetalX();
                float distance = time * speed;
                if (distance <= danmaku.getCurrentOffsetX() - danmaku.getDanmakuWidth()) {
                    line = i;
                    break;
                }
            }
        }
        return line;
    }

    /**
     * 从ZGDanmakuItem生成一个ZGDanmaku
     *
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
        danmaku.setDetalX(item.getDetalX());
        danmaku.setShader(mVertexShader, mFragmentShader);

        return danmaku;
    }

    /**
     * 占用某弹道
     *
     * @param line
     * @param danmaku
     */
    private synchronized void inValidableLine(int line, ZGDanmaku danmaku) {
        ZGLog.d("ZGDanmakuDispatcher inValidableLine line=" + line);
        mLinesAvaliable.put(line, danmaku);
    }

    /**
     * 清空弹道
     */
    private synchronized void resetLines() {
        mLinesAvaliable.clear();
    }

    /**
     * 判断是否发这条弹幕
     * 如果弹幕出现时间在可发送区间内,或者是即时发送的弹幕则进行发送.
     * @param item
     * @return
     */
    private boolean shouldShow(ZGDanmakuItem item) {
        if (item.getOffsetTime() <= ZGTimer.getInstance().getTime() && ZGTimer.getInstance().getTime() <= item.getLateTime()) {
            return true;
        }
        return false;
    }

    /**
     * 是否该丢弃这个弹幕了
     * @param item
     * @return
     */
    private boolean shouldDrop(ZGDanmakuItem item) {
        if (ZGTimer.getInstance().getTime() > item.getLateTime()) {
            return true;
        }
        return false;
    }

    /**
     * 设置行距
     *
     * @param leading
     */
    public void setLeading(float leading) {
        this.mLineLeading = leading;
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
     * 停止弹幕
     */
    public void quit() {
        ZGLog.i("ZGDanmakuDispatcher quit.");
        mStop = true;
        resetLines();
        mDanmakuPool.wakeIfNeed();
    }

    /**
     * 是否已经暂停
     *
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
     * 更新时间
     * @param time
     */
    public synchronized void updateTime(long time) {
        ZGTimer.getInstance().syncTime(time);
        isSeek = true;
        //不清空弹幕池的话,业务可能会重复塞弹幕
        mDanmakuPool.clear();
        ZGLog.i("updateTime time:" + time);
    }

    /**
     * 是否已经暂停了
     *
     * @return
     */
    public boolean isStop() {
        return mStop;
    }

    @Override
    public void run() {
        ZGLog.i("ZGDanmakuDispatcher running");

        resetLines();
        mStop = false;
        if (!isSeek) {
            ZGTimer.getInstance().syncTime(0);
        }
        long lastTime, intervalTime;
        long currentTime = SystemClock.elapsedRealtime();
        while (!mStop) {

            //暂停
            synchronized (mPaused) {
                if (mPaused.get()) {
                    ZGLog.i("dispatcher paused");
                    try {
                        mPaused.wait();
                        mPaused.set(false);
                    } catch (InterruptedException e) {
                    }
                    currentTime = SystemClock.elapsedRealtime();
                    ZGLog.i("dispatcher resume");
                }
            }

            lastTime = currentTime;
            currentTime = SystemClock.elapsedRealtime();
            intervalTime = currentTime - lastTime;

            if (intervalTime < DEFAUTL_FRAME_INTERVAL) {
                SystemClock.sleep(DEFAUTL_FRAME_INTERVAL - intervalTime);
                currentTime = SystemClock.elapsedRealtime();
                intervalTime = currentTime - lastTime;
            }

            ZGTimer.getInstance().addInterval(intervalTime);
            if (isSeek) {
                isSeek = false;
                ZGLog.i("seek happened at time:" + ZGTimer.getInstance().getTime());
                //seek以后,只清空弹幕池的弹幕,还在屏幕的弹幕(就是在渲染器里面的弹幕)还继续跑
                //更好的做法是全部调整屏幕弹幕的位置
                continue;
            }

            ZGLog.d("ZGDanmakuDispatcher run intervalTime:" + intervalTime + ", at time:" + ZGTimer.getInstance().getTime());

            //todo 这样性能OK?
            //读取临界区的弹幕，然后复制到一个新的list，并去掉已经跑出屏幕的弹幕
            List<ZGDanmaku> rendererList = mRenderer.getRendererDanmakuList();

            //如果临界区没有弹幕,并且弹幕池没有弹幕了,则进行阻塞,这样的能耗低.
            if (rendererList.size() == 0) {
                resetLines();
                if (mDanmakuPool.waitIfNeed()) {
                    currentTime = SystemClock.elapsedRealtime();
                }
            }

            List<ZGDanmaku> nextRendererList = new ArrayList<ZGDanmaku>();
            for (int i = 0; i < rendererList.size(); i++) {
                if (!rendererList.get(i).isFinished()) {
                    nextRendererList.add(rendererList.get(i));
                }
            }

            if (nextRendererList.size() + mLines < MAX_READERING_NUMBER) {
                List<ZGDanmakuItem> postLater = new ArrayList<ZGDanmakuItem>();
                //取出时间最小的弹幕
                ZGDanmakuItem item = mDanmakuPool.poll();
                while(item != null) {
                    if (shouldShow(item)) {
                        int line = findFittestLine(item);
                        if (line == -1) {
                            continue;
                        }
                        ZGDanmaku danmaku = generateDanmaku(item, line);
                        nextRendererList.add(danmaku);
                        ZGLog.d("ZGDanmakuDispatcher shot item:" + item.getText());
                        break;
                    } else if (!shouldDrop(item)){
                        ZGLog.d("ZGDanmakuDispatcher post later item:" + item.getText());
                        postLater.add(item);
                    } else {
                        ZGLog.d("ZGDanmakuDispatcher drop item:" + item.getText());
                    }
                    item = mDanmakuPool.poll();
                }
                if (postLater.size() > 0) {
                    ZGLog.d("ZGDanmakuDispatcher postLater size:" + postLater.size());
                    mDanmakuPool.addAll(postLater);
                }
            }

            //替换临界区的弹幕
            mRenderer.setRendererDanmakuList(nextRendererList);
        }
    }
}
