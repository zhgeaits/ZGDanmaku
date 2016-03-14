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
package org.zhgeaits.zgdanmaku.view;

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhgeaits on 16/2/26.
 * 渲染器基类，包含通用的方法
 */
public class ZGBaseDanmakuRenderer implements IZGDanmakuRenderer {

    protected List<ZGDanmaku> mDanmakus;              //弹幕临界区
    protected IZGRenderListener mListener;            //初始化成功监听器
    protected int mViewWidth;                         //窗口宽度
    protected int mViewHeight;                        //窗口高度
    protected float mSpeed;                           //速度，单位px/s
    protected long mLastTime;                         //绘制上一帧的时间
    protected boolean isHide = false;                 //是否打开弹幕
    protected boolean isPaused = false;               //是否暂停弹幕
    protected boolean isInited = false;               //是否初始化完了

    public ZGBaseDanmakuRenderer() {
        mDanmakus = new ArrayList<>();
    }

    @Override
    public List<ZGDanmaku> getRendererDanmakuList() {
        return mDanmakus;
    }

    @Override
    public void setRendererDanmakuList(List<ZGDanmaku> rendererDanmakuList) {
        this.mDanmakus = rendererDanmakuList;
    }

    @Override
    public boolean isOKToRenderer() {
        return isInited;
    }

    @Override
    public int getViewWidth() {
        return mViewWidth;
    }

    @Override
    public int getViewHeight() {
        return mViewHeight;
    }

    @Override
    public void setListener(IZGRenderListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    @Override
    public void setHide(boolean hide) {
        isHide = hide;
    }

    @Override
    public void setPause(boolean pause) {
        isPaused = pause;
        if (!isPaused) {
            mLastTime = 0;
        }
    }

    @Override
    public boolean isPause() {
        return isPaused;
    }

    @Override
    public boolean isHide() {
        return isHide;
    }
}
