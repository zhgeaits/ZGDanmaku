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
import android.util.AttributeSet;

import com.eaglesakura.view.GLTextureView;
import com.eaglesakura.view.egl.SurfaceColorSpec;

import org.zhgeaits.zgdanmaku.controller.IZGDanmakuController;
import org.zhgeaits.zgdanmaku.controller.ZGDanmakuController;
import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

/**
 * Created by zhgeaits on 16/2/25.
 * 基于GLTextureView实现的弹幕，GLTextureView来自github开源的实现
 * https://github.com/eaglesakura/gltextureview.git
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

    public ZGDanmakuTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        //同理setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setSurfaceSpec(SurfaceColorSpec.RGBA8, true, false);
        setVersion(GLESVersion.OpenGLES20);

        IZGDanmakuRenderer renderer = new ZGDanmakuTextureViewRenderer();
        setRenderer((GLTextureView.Renderer) renderer);

        //设置textureview透明
        setOpaque(false);

        //同理setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setRenderingThreadType(RenderingThreadType.BackgroundThread);

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
        mDanmakuController.pause();
    }

    @Override
    public void resume() {
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
