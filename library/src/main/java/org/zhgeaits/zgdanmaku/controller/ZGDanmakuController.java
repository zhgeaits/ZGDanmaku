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

import android.content.Context;
import android.util.Log;

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.DimensUtils;
import org.zhgeaits.zgdanmaku.utils.ShaderUtils;
import org.zhgeaits.zgdanmaku.utils.ZGDanmakuPool;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuRenderer;
import org.zhgeaits.zgdanmaku.view.IZGRenderListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhgeaits on 16/2/26.
 * 弹幕控制器
 */
public class ZGDanmakuController implements IZGDanmakuController {

    private Context mContext;
    private Map<Integer, ZGDanmaku> mLinesAvaliable;
    private int mLines;
    private float mLineLeading;
    private boolean mStop;
    private String mVertexShader;                       //顶点着色器
    private String mFragmentShader;                     //片元着色器
    private IZGDanmakuRenderer mRenderer;
    private ZGDanmakuPool mDanmakuPool;

    public ZGDanmakuController(Context context, IZGDanmakuRenderer renderer) {
        mRenderer = renderer;
        mContext = context;
        mLinesAvaliable = new HashMap<>();
        mDanmakuPool = new ZGDanmakuPool();
        loadShader();

        //默认50dp/s速度
        setSpeed(50);

        //默认8dp行距
        setLeading(2);
    }

    @Override
    public void start() {
        if(mRenderer.isOKToRenderer()) {
            startInternal();
        } else {
            mRenderer.setListener(new IZGRenderListener() {
                @Override
                public void onInited() {
                    start();
                }
            });
        }
    }

    @Override
    public void stop() {
        mStop = true;
        mDanmakuPool.wakeIfNeed();
    }

    @Override
    public void hide() {
        mRenderer.setHide(true);
    }

    @Override
    public void show() {
        mRenderer.setHide(false);
    }

    @Override
    public void pause() {
        mRenderer.setPause(true);
    }

    @Override
    public void resume() {
        mRenderer.setPause(false);
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isPause() {
        return mRenderer.isPause();
    }

    @Override
    public boolean isHide() {
        return mRenderer.isHide();
    }

    @Override
    public void setLines(int lines) {
        this.mLines = lines;
    }

    @Override
    public void setLeading(float leading) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, leading);
        this.mLineLeading = pxLineSpace;
    }

    @Override
    public void setSpeed(float speed) {
        float pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        this.mRenderer.setSpeed(pxSpeed);
    }

    @Override
    public void addDanmaku(ZGDanmakuItem danmakuItem) {
        mDanmakuPool.offer(danmakuItem);
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

    private void inValidableLine(int line, ZGDanmaku danmaku) {
        mLinesAvaliable.put(line, danmaku);
    }

    protected void loadShader() {
        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", mContext.getResources());

        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", mContext.getResources());
    }

    private ZGDanmaku generateDanmaku(ZGDanmakuItem item, int line) {

        ZGDanmaku danmaku = new ZGDanmaku(item.getDanmakuBitmap());
        inValidableLine(line, danmaku);

        updateDanmaku(danmaku, item, line);

        return danmaku;
    }

    protected void updateDanmaku(ZGDanmaku danmaku, ZGDanmakuItem item, int line) {
        float offsetY = (item.getDanmakuHeight() + mLineLeading) * line;
        danmaku.setOffsetY(offsetY);
        danmaku.setViewSize(mRenderer.getViewWidth(), mRenderer.getViewHeight());
        danmaku.setShader(mVertexShader, mFragmentShader);
    }

    private void startInternal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    if(mStop) {
                        break;
                    }

                    //读取临界区的弹幕，然后复制到一个新的list，并去掉已经跑出屏幕的弹幕
                    List<ZGDanmaku> rendererList = mRenderer.getRendererDanmakuList();
                    List<ZGDanmaku> nextRendererList = new ArrayList<>();
                    for (int i = 0; i < rendererList.size(); i++) {
                        if (!rendererList.get(i).isFinished()) {
                            nextRendererList.add(rendererList.get(i));
                        }
                    }

                    //获取有效的弹道，然后读取弹幕池的弹幕进行绘制
                    for (int i = 0; i < mLines; i ++) {
                        if(isLineAvaliable(i)) {
                            ZGDanmakuItem item = mDanmakuPool.poll();
                            if(item != null) {
                                ZGDanmaku danmaku = generateDanmaku(item, i);
                                nextRendererList.add(danmaku);
                            }
                        }
                    }

                    //如果弹幕池和临界区都为空的时候就阻塞，不然弹幕池和临界区要不停刷新的
                    if (nextRendererList.size() == 0) {
                        try {
                            mDanmakuPool.waitIfNeed();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    //替换临界区的弹幕
                    if (nextRendererList.size() > 0) {
                        mRenderer.setRendererDanmakuList(nextRendererList);
                    }

                    try {
                        //todo 根据帧率设置睡眠时间
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }
}
