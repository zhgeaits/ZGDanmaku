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

import com.eaglesakura.view.GLTextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeaits on 16/2/26.
 * ZGDanmakuTextureView2的渲染器
 */
@Deprecated
public class ZGDanmakuTextureViewRenderer extends ZGBaseDanmakuRenderer implements GLTextureView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawFrame(gl);
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
        surfaceDestroyed(gl);
    }

}
