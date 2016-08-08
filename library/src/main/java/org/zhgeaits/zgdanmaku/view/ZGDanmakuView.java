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
import android.util.DisplayMetrics;

import org.zhgeaits.zgdanmaku.controller.IZGDanmakuController;
import org.zhgeaits.zgdanmaku.controller.ZGDanmakuController;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;
import org.zhgeaits.zgdanmaku.utils.ZGLog;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕view
 * 含有一个弹幕池，外面发送弹幕即往弹幕池添加一条弹幕。
 */
public class ZGDanmakuView extends GLSurfaceView implements IZGDanmakuView {

    private Context mContext;
    private IZGDanmakuController mDanmakuController;
    private IZGDanmakuRenderer mRenderer;

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

        mRenderer = new ZGDanmakuRenderer();
        setRenderer((Renderer) mRenderer);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mRenderer.setDisplayDensity(displayMetrics.density);

        //设置view为透明，并置于顶层，可以在surfaceview之上
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        //这个可以让在surfaceview之上
//        setZOrderMediaOverlay(true);

        // 设置渲染模式为被动渲染，在调用start()方法以后再设置为主动模式,
        // 主动模式有一条后台OPENGL线程每帧都调用onDrawFrame方法
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mDanmakuController = new ZGDanmakuController(context, mRenderer);
    }

    @Override
    public void setSpeed(float speed) {
        mDanmakuController.setSpeed(speed);
    }

    @Override
    public void start() {
        ZGLog.i("ZGDanmakuView start");
        stop();
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
        mDanmakuController.start();
    }

    @Override
    public void stop() {
        ZGLog.i("ZGDanmakuView stop");
        mDanmakuController.stop();
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //清屏
        mRenderer.getRendererDanmakuList().clear();
        requestRender();
    }

    @Override
    public void hide() {
        ZGLog.i("ZGDanmakuView hide");
        mDanmakuController.hide();
        if (isPaused()) {
            requestRender();
        }
    }

    @Override
    public void show() {
        ZGLog.i("ZGDanmakuView show");
        mDanmakuController.show();
        if (isPaused()) {
            requestRender();
        }
    }

    @Override
    public void pause() {
        // FIXME: 16/8/3 为什么onPause之后再调onResume没用
//        onPause();
        if (isStarted()) {
            ZGLog.i("ZGDanmakuView pause");
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mDanmakuController.pause();
        }
    }

    @Override
    public void resume() {
        // FIXME: 16/8/3 为什么onPause之后再调onResume没用
//        onResume();
        if (isStarted()) {
            ZGLog.i("ZGDanmakuView resume");
            mDanmakuController.resume();
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
    public void shotTextDanmaku(String text, int color) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        item.setTextColor(color);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmaku(String text, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext);
        item.setTextSize(size);
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
    public void shotTextDanmakuAt(String text, long time, int color) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext, time);
        item.setTextColor(color);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmakuAt(String text, long time, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext, time);
        item.setTextSize(size);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void shotTextDanmakuAt(String text, long time, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(text, mContext, time);
        item.setTextColor(color);
        item.setTextSize(size);
        mDanmakuController.addDanmaku(item);
    }

    @Override
    public void seek(long time) {
        mDanmakuController.updateTime(time);
    }
}
