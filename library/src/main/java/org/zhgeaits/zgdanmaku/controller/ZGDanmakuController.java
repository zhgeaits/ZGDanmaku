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

import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.DimensUtils;
import org.zhgeaits.zgdanmaku.utils.ShaderUtils;
import org.zhgeaits.zgdanmaku.utils.ZGDanmakuPool;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuRenderer;
import org.zhgeaits.zgdanmaku.view.IZGRenderListener;

/**
 * Created by zhgeaits on 16/2/26.
 * 弹幕控制器
 */
public class ZGDanmakuController implements IZGDanmakuController {

    private Context mContext;
    private IZGDanmakuRenderer mRenderer;
    private ZGDanmakuDispatcher mDispatcher;
    private ZGDanmakuPool mDanmakuPool;
    private String mVertexShader;                       //顶点着色器
    private String mFragmentShader;                     //片元着色器

    public ZGDanmakuController(Context context, IZGDanmakuRenderer renderer) {

        mRenderer = renderer;
        mContext = context;

        mDanmakuPool = new ZGDanmakuPool();

        loadShader();

        mDispatcher = new ZGDanmakuDispatcher(mDanmakuPool, mRenderer, mVertexShader, mFragmentShader);

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
        mDispatcher.quit();
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
        mDispatcher.setLines(lines);
    }

    @Override
    public void setLeading(float leading) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, leading);
        mDispatcher.setLeading(pxLineSpace);
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

    private void loadShader() {
        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", mContext.getResources());

        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", mContext.getResources());
    }

    private void startInternal() {
        mDispatcher.start();
    }
}
