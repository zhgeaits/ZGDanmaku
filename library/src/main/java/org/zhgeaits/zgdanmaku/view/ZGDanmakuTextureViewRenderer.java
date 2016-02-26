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

import android.opengl.GLES20;
import android.os.SystemClock;

import com.eaglesakura.view.GLTextureView;

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.utils.MatrixUtils;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeaits on 16/2/26.
 */
public class ZGDanmakuTextureViewRenderer extends ZGBaseDanmakuRenderer implements GLTextureView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        //开启混色功能，这样是为了让png图片的透明能显示
        GLES20.glEnable(GLES20.GL_BLEND);

        //指定混色方案
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        //设置视窗大小及位置为整个view范围
        GLES20.glViewport(0, 0, width, height);

        //计算产生正交投影矩阵
        //一般会设置前两个参数为-width / height，width / height，使得纹理不会变形，
        //但是我这里不这样设置，为了控制位置，变形这个问题在顶点坐标那里处理即可
        MatrixUtils.setProjectOrtho(-1, 1, -1, 1, 0, 1);

        //产生摄像机9参数位置矩阵
        MatrixUtils.setCamera(0, 0, 1, 0f, 0f, 0f, 0f, 1, 0);

        isInited = true;
        if(mListener != null) {
            mListener.onInited();
        }

        mLastTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        long currentTime = SystemClock.elapsedRealtime();
        float intervalTime = (float)(currentTime - mLastTime) / 1000.0f;
        float detalOffset = mSpeed * intervalTime;

        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        //绘制弹幕纹理
        List<ZGDanmaku> danmakus = mDanmakus;
        int size = danmakus.size();
        for (int i = 0; i < size; i ++) {
            ZGDanmaku danmaku = danmakus.get(i);

            if(!isPaused) {
                float newOffset = detalOffset + danmaku.getCurrentOffsetX();
                danmaku.setOffsetX(newOffset);
            }

            if (!isHide) {
                danmaku.drawDanmaku();
            }
        }

        mLastTime = currentTime;
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {
    }

}
