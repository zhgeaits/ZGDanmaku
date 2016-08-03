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

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.ZGDanmakuPool;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhgeaits on 16/8/2.
 * 弹幕分发器
 * 1. 不断从GL线程的临界区获取弹幕，然后去掉已经跑完的弹幕；
 * 2. 不断从弹幕池获取弹幕，最后整合临界区的弹幕一起更新到临界区。
 * 如果临界区和弹幕池都没有弹幕了，则阻塞，直到新来一条弹幕
 */
public class ZGDanmakuDispatcher extends Thread {

    /**
     * 每帧的时间间隔
     */
    private final static int DEFAUTL_FRAME_INTERVAL = 16;

    /**
     * 最大的绘制弹幕数量
     */
    private final static int MAX_READERING_NUMBER = 70;

    private ZGDanmakuPool mDanmakuPool;                     // 弹幕池
    private IZGDanmakuRenderer mRenderer;                   //渲染器
    private Map<Integer, ZGDanmaku> mLinesAvaliable;
    private String mVertexShader;                           //顶点着色器
    private String mFragmentShader;                         //片元着色器
    private int mLines;
    private float mLineLeading;
    private boolean mStop;
    private long time;

    public ZGDanmakuDispatcher(ZGDanmakuPool mDanmakuPool, IZGDanmakuRenderer mRenderer,
                               String mVertexShader, String mFragmentShader) {
        mLinesAvaliable = new HashMap<>();
        this.mDanmakuPool = mDanmakuPool;
        this.mRenderer = mRenderer;
        this.mVertexShader = mVertexShader;
        this.mFragmentShader = mFragmentShader;
    }

    private boolean isLineAvaliable(int line) {
        ZGDanmaku danmaku = mLinesAvaliable.get(line);

        if(danmaku == null) {
            return true;
        }

        if (danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth()) {
            return true;
        }

        return false;
    }

    private ZGDanmaku generateDanmaku(ZGDanmakuItem item, int line) {

        ZGDanmaku danmaku = new ZGDanmaku(item.getDanmakuBitmap());
        inValidableLine(line, danmaku);

        updateDanmaku(danmaku, item, line);

        return danmaku;
    }

    private void updateDanmaku(ZGDanmaku danmaku, ZGDanmakuItem item, int line) {
        float offsetY = (item.getDanmakuHeight() + mLineLeading) * line;
        danmaku.setOffsetY(offsetY);
        danmaku.setViewSize(mRenderer.getViewWidth(), mRenderer.getViewHeight());
        danmaku.setShader(mVertexShader, mFragmentShader);
    }

    private void inValidableLine(int line, ZGDanmaku danmaku) {
        mLinesAvaliable.put(line, danmaku);
    }

    public void setLeading(float leading) {
        this.mLineLeading = leading;
    }

    public void setLines(int lines) {
        this.mLines = lines;
    }

    public void quit() {
        mStop = true;
        mDanmakuPool.wakeIfNeed();
    }

    @Override
    public void run() {
        long lastTime, intervalTime;
        long currentTime = SystemClock.elapsedRealtime();
        while (!mStop) {

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

//            Log.i("zhangge-test", "ZGDanmakuDispatcher run intervalTime:" + intervalTime + ", time:" + time);

            //读取临界区的弹幕，然后复制到一个新的list，并去掉已经跑出屏幕的弹幕
            List<ZGDanmaku> rendererList = mRenderer.getRendererDanmakuList();
            List<ZGDanmaku> nextRendererList = new ArrayList<>();
            for (int i = 0; i < rendererList.size(); i++) {
                if (!rendererList.get(i).isFinished()) {
                    nextRendererList.add(rendererList.get(i));
                }
            }

            if (nextRendererList.size() < 70) {
                //获取有效的弹道，然后读取弹幕池的弹幕进行绘制
                for (int i = 0; i < mLines; i ++) {
                    if(isLineAvaliable(i)) {
                        ZGDanmakuItem item = mDanmakuPool.poll();
                        if (item != null) {
                            if(item.getShowTime() <= time || item.getShowTime() == -1) {
                                ZGDanmaku danmaku = generateDanmaku(item, i);
                                nextRendererList.add(danmaku);
                            } else {
                                mDanmakuPool.offer(item);
                            }
//                            Log.i("zhangge-test", "ZGDanmakuDispatcher run intervalTime:" + intervalTime + ", time:" + time + ", item.getShowTime():" + item.getShowTime());
                        }

                    }
                }
            }

            //如果弹幕池和临界区都为空的时候就阻塞，不然弹幕池和临界区要不停刷新的
//            if (nextRendererList.size() == 0) {
//                try {
//                    mDanmakuPool.waitIfNeed();
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }

            //替换临界区的弹幕
            if (nextRendererList.size() > 0) {
                mRenderer.setRendererDanmakuList(nextRendererList);
            }
        }
    }
}
