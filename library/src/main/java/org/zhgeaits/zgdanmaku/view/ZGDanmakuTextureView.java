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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.zhgeaits.zgdanmaku.controller.IZGDanmakuController;
import org.zhgeaits.zgdanmaku.controller.ZGDanmakuController;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

/**
 * Created by zhgeaits on 16/2/25.
 * 基于GLTextureView实现的弹幕，GLTextureView来自github开源的实现
 * https://github.com/romannurik/muzei/blob/master/main/src/main/java/com/google/android/apps/muzei/render/GLTextureView.java
 * 这个效率比另外一个TextureView实现的要高，毕竟是google自己写的
 */
public class ZGDanmakuTextureView extends GLTextureView implements IZGDanmakuView {

    private Context mContext;
    private IZGDanmakuController mDanmakuController;

    public ZGDanmakuTextureView(Context context) {
        super(context);
        init(context);
    }

    public ZGDanmakuTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        setEGLContextClientVersion(2);

        //设置EGL的像素配置
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        IZGDanmakuRenderer renderer = new ZGDanmakuRenderer();
        setRenderer((GLSurfaceView.Renderer) renderer);

        //设置textureview透明
        setOpaque(false);

        setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);
        //setRenderingThreadType(RenderingThreadType.BackgroundThread);

        mDanmakuController = new ZGDanmakuController(context, renderer);
    }

    @Override
    public void setSpeed(float speed) {
        mDanmakuController.setSpeed(speed);
    }

    @Override
    public void start() {
        mDanmakuController.start();
    }

    @Override
    public void stop() {
        mDanmakuController.stop();
    }

    @Override
    public void hide() {
        mDanmakuController.hide();
    }

    @Override
    public void show() {
        mDanmakuController.show();
    }

    @Override
    public void pause() {
//        onPause();
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mDanmakuController.pause();
    }

    @Override
    public void resume() {
//        onResume();
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
        mDanmakuController.resume();
    }

    @Override
    public boolean isPause() {
        return mDanmakuController.isPause();
    }

    @Override
    public boolean isHide() {
        return mDanmakuController.isHide();
    }

    @Override
    public void setLines(int lines) {
        mDanmakuController.setLines(lines);
    }

    @Override
    public void setLeading(float leading) {
        mDanmakuController.setLeading(leading);
    }

    @Override
    public void shotTextDanmamku(String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        mDanmakuController.addDanmaku(item);
    }
}
