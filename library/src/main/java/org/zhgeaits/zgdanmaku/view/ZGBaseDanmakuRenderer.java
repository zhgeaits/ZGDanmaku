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

import org.zhgeaits.zgdanmaku.model.ZGDanmaku;
import org.zhgeaits.zgdanmaku.utils.MatrixUtils;
import org.zhgeaits.zgdanmaku.utils.ZGLog;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeaits on 16/2/26.
 * 渲染器基类，包含通用的方法和绘制逻辑.
 * GL线程每帧都会回调drawFrame;
 * 初始化纹理，绑定纹理都必须在GL线程去做，不然画不出来东西.
 * 尽量不在drawFrame锁住临界区，获取锁和释放锁会比较耗时,影响帧率.
 * 每次GL线程都完成渲染临界区的所有弹幕，走出屏幕的弹幕交给外面的线程处理。
 */
public abstract class ZGBaseDanmakuRenderer implements IZGDanmakuRenderer {

    /**
     * 默认每帧的时间间隔,16ms,即60帧/s
     * 实际上OpenGLES是基于硬件的,Android这边的GLSurfaceView都是60帧/s
     */
    private final static int DEFAUTL_FRAME_INTERVAL = 16;

    protected List<ZGDanmaku> mDanmakus;                        //弹幕临界区
    protected IZGRenderListener mListener;                      //初始化成功监听器
    protected int mViewWidth;                                   //窗口宽度
    protected int mViewHeight;                                  //窗口高度
    protected float mSpeed;                                     //速度，单位px/s
    protected float mDetalOffset;                               //每帧的偏移量
    protected long mCurrentTime;                                //当前帧的时间
    protected long mLastTime;                                   //绘制上一帧的时间
    protected long mIntervalTime;                               //每帧的时间间隔
    protected boolean isHide = false;                           //是否打开弹幕
    protected boolean isInited = false;                         //是否初始化完了

    public ZGBaseDanmakuRenderer() {
        mDanmakus = new ArrayList<ZGDanmaku>();
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
    public boolean isHide() {
        return isHide;
    }

    @Override
    public void resume() {
        mCurrentTime = SystemClock.elapsedRealtime();
    }

    protected void surfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        ZGLog.i("surfaceCreated ");

        //开启混色功能，这样是为了让png图片的透明能显示
        GLES20.glEnable(GLES20.GL_BLEND);

        //指定混色方案
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void surfaceChanged(GL10 gl10, int width, int height) {

        ZGLog.i("surfaceChanged width:" + width + ", height:" + height);

        this.mViewWidth = width;
        this.mViewHeight = height;

        //设置视窗大小及位置为整个view范围
        GLES20.glViewport(0, 0, width, height);

        //计算产生正交投影矩阵
        //一般会设置前两个参数为-width / height，width / height，使得纹理不会变形，
        //但是我这里不这样设置，为了控制位置，变形这个问题在顶点坐标那里处理即可
        MatrixUtils.setProjectOrtho(-1, 1, -1, 1, 0, 1);

        //产生摄像机9参数位置矩阵
        MatrixUtils.setCamera(0, 0, 1, 0f, 0f, 0f, 0f, 1, 0);

        isInited = true;
        if (mListener != null) {
            mListener.onInited();
        }

        mCurrentTime = SystemClock.elapsedRealtime();

        if (mDanmakus != null && mDanmakus.size() > 0) {
            for (int i = 0; i < mDanmakus.size(); i++) {
                ZGDanmaku danmaku = mDanmakus.get(i);
                if (danmaku != null) {
                    danmaku.setViewSize(width, height);
                    danmaku.initVertexData();
                }
            }
        }
    }

    protected void drawFrame(GL10 gl10) {

        mLastTime = mCurrentTime;
        mCurrentTime = SystemClock.elapsedRealtime();
        mIntervalTime = mCurrentTime - mLastTime;

        /**
         * 如果帧率快了,则调整一下
         */
        if (mIntervalTime < DEFAUTL_FRAME_INTERVAL) {
            try {
                Thread.sleep(DEFAUTL_FRAME_INTERVAL - mIntervalTime);
            } catch (InterruptedException e) {
            }
            mCurrentTime = SystemClock.elapsedRealtime();
            mIntervalTime = mCurrentTime - mLastTime;
        }

        mDetalOffset = mSpeed * ((float) (mIntervalTime) / 1000.0f);

        // 设置屏幕背景色RGBA
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // 清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // 绘制弹幕纹理
        List<ZGDanmaku> danmakus = mDanmakus;

        for (int i = 0; i < danmakus.size(); i++) {
            ZGDanmaku danmaku = danmakus.get(i);
            danmaku.addDetalOffsetX(mDetalOffset);
            if (!isHide) {
                danmaku.drawDanmaku();
            }
        }

        long now = SystemClock.elapsedRealtime() - mCurrentTime;
        if (now > 16) {
            ZGLog.i("oops, intervalTime:" + mIntervalTime + ", now:" + now + ", size:" + mDanmakus.size() + ", isHide:" + isHide);
        }
    }

    public void surfaceDestroyed(GL10 gl) {
    }
}
