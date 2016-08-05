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
import android.util.Log;

import com.eaglesakura.view.GLTextureView;
import com.eaglesakura.view.egl.SurfaceColorSpec;

import org.zhgeaits.zgdanmaku.controller.IZGDanmakuController;
import org.zhgeaits.zgdanmaku.controller.ZGDanmakuController;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

/**
 * Created by zhgeaits on 16/2/25.
 * 基于GLTextureView实现的弹幕，GLTextureView来自github开源的实现
 * https://github.com/eaglesakura/gltextureview.git
 * 相对比另外两个效率都低一些
 */
public class ZGDanmakuTextureView2 extends GLTextureView implements IZGDanmakuView {

    private Context mContext;
    private IZGDanmakuController mDanmakuController;
    private IZGDanmakuRenderer mRenderer;

    public ZGDanmakuTextureView2(Context context) {
        super(context);
        init(context);
    }

    public ZGDanmakuTextureView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZGDanmakuTextureView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        //同理setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        //同理setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setSurfaceSpec(SurfaceColorSpec.RGBA8, true, false);
        setVersion(GLESVersion.OpenGLES20);

        mRenderer = new ZGDanmakuTextureViewRenderer();
        setRenderer((GLTextureView.Renderer) mRenderer);

        //设置textureview透明
        setOpaque(false);

        //同理setRenderMode(GLTextureView.RENDERMODE_CONTINUOUSLY);
        // 设置渲染模式为被动渲染，在调用start()方法以后再设置为主动模式,
        // 主动模式有一条后台OPENGL线程每帧都调用onDrawFrame方法
        setRenderingThreadType(RenderingThreadType.BackgroundThread);

        mDanmakuController = new ZGDanmakuController(context, mRenderer);
    }

    @Override
    public void setSpeed(float speed) {
        mDanmakuController.setSpeed(speed);
    }

    @Override
    public void start() {
        Log.i("ZGDanmaku", "ZGDanmakuView start");
//        stop();
//        setRenderingThreadType(RenderingThreadType.BackgroundThread);
//        requestRender();
        mDanmakuController.start();
        onResume();
    }

    @Override
    public void stop() {
        onPause();
//        mDanmakuController.stop();
//        setRenderingThreadType(RenderingThreadType.RequestThread);
        //清屏
        mRenderer.getRendererDanmakuList().clear();
        if(isInitialized()) {
            requestRender();
        }
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
        onPause();
        if (isStarted()) {
//            setRenderingThreadType(RenderingThreadType.RequestThread);
            mDanmakuController.pause();
        }
    }

    @Override
    public void resume() {
        onResume();
        if (isStarted()) {
            mDanmakuController.resume();
//            setRenderingThreadType(RenderingThreadType.BackgroundThread);
        }
    }

    @Override
    public boolean isStarted() {
        return mDanmakuController.isStarted();
    }

    @Override
    public boolean isPaused() {
        return mDanmakuController.isPause();
    }

    @Override
    public boolean isHided() {
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
    public void shotTextDanmaku(String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmaku(String text, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        item.setTextColor(color);
        item.setTextSize(size);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmakuAt(String text, long time) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext, time);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmakuAt(String text, long time, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext, time);
        item.setTextColor(color);
        item.setTextSize(size);
        mDanmakuController.addDanmaku(item);
    }
}
