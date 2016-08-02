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
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.zhgeaits.zgdanmaku.controller.IZGDanmakuController;
import org.zhgeaits.zgdanmaku.controller.ZGDanmakuController;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕view
 * 含有一个弹幕池，外面发送弹幕即往弹幕池添加一条弹幕。
 * 包含一条弹幕线程:
 */
public class ZGDanmakuView extends GLSurfaceView implements IZGDanmakuView {

    private Context mContext;
    private IZGDanmakuController mDanmakuController;

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

        //设置使用opengles 2.0
        setEGLContextClientVersion(2);

        //设置EGL的像素配置
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        IZGDanmakuRenderer renderer = new ZGDanmakuRenderer();
        setRenderer((Renderer) renderer);

        //设置view为透明，并置于顶层，可以在surfaceview之上
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        // 设置渲染模式为被动渲染，在调用start()方法以后再设置为主动模式,
        // 主动模式有一条后台OPENGL线程每帧都调用onDrawFrame方法
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mDanmakuController = new ZGDanmakuController(context, renderer);
    }

    @Override
    public void setSpeed(float speed) {
        mDanmakuController.setSpeed(speed);
    }

    @Override
    public void start() {
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
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
        // todo fix me why onPause doesn't work?
//        onPause();
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mDanmakuController.pause();
    }

    @Override
    public void resume() {
        // todo fix me: why onResume doesn't work?
//        onResume();
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
        mDanmakuController.resume();
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
    public void shotTextDanmamku(String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        mDanmakuController.addDanmaku(item);
    }
}
