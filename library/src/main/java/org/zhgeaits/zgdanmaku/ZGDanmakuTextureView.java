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
package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import com.eaglesakura.view.GLTextureView;
import com.eaglesakura.view.egl.SurfaceColorSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeaits on 16/2/25.
 */
public class ZGDanmakuTextureView extends GLTextureView {

    private Context mContext;
    private ZGDanmakuTextviewRenderer mRenderer;//渲染器
    private int mLines = 4;//默认4行
    private float mLineSpace;//行距
    private Canvas mCanvas;
    private Paint mPainter;
    private boolean isResumed = false;
    private Queue<ZGDanmakuItem> mCachedDanmaku;
    private Map<Integer, ZGDanmaku> mLinesAvaliable;
    protected final AtomicBoolean mWaiting = new AtomicBoolean(false);

    private List<ZGDanmaku> mDanmakus;              //弹幕临界区
    private String mVertexShader;                   //顶点着色器
    private String mFragmentShader;                 //片元着色器
    private int mViewWidth;                         //窗口宽度
    private int mViewHeight;                        //窗口高度
    private float mSpeed;                           //速度，单位px/s
    private long mLastTime;                         //绘制上一帧的时间
    private boolean isOpen = true;                  //是否打开弹幕
    private boolean isPaused = false;               //是否暂停弹幕

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
        mCachedDanmaku = new LinkedList<>();
        mLinesAvaliable = new HashMap<>();
        mDanmakus = new ArrayList<>();

        setSurfaceSpec(SurfaceColorSpec.RGBA8, true, false);
//        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setVersion(GLESVersion.OpenGLES20);

        mRenderer = new ZGDanmakuTextviewRenderer();
        setRenderer(mRenderer);
//        setAlpha(1);
        setOpaque(false);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
//        setRenderingThreadType(RenderingThreadType.RequestThread);

        setSpeed(50);//默认50dp/s速度
        setLineSpace(8);//默认8dp行距

        mCanvas = new Canvas();
        mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPainter.setTextSize(DimensUtils.dip2pixel(mContext, 18));
        mPainter.setColor(0xffffffff);
        mPainter.setTextAlign(Paint.Align.LEFT);
        mPainter.setShadowLayer(2, 3, 3, 0x5a000000);

    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    private void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!isResumed) {
                        break;
                    }

                    //读取临界区的弹幕
                    List<ZGDanmaku> rendererList = getDanmakus();
                    List<ZGDanmaku> swapList = new ArrayList<>();
                    for (int i = 0; i < rendererList.size(); i++) {
                        if (!rendererList.get(i).isFinished()) {
                            swapList.add(rendererList.get(i));
                        }
                    }

                    //读取弹幕池的弹幕
                    int cacheSize;
                    synchronized (mCachedDanmaku) {
                        cacheSize = mCachedDanmaku.size();
                        for (int i = 0; i < cacheSize; i++) {
                            ZGDanmakuItem item = mCachedDanmaku.poll();
                            ZGDanmaku danmaku = generateDanmaku(item);
                            if (danmaku == null) {
                                mCachedDanmaku.offer(item);
                            } else {
                                updateDanmakus(danmaku);
                                swapList.add(danmaku);
                            }
                        }
                    }

                    //如果弹幕池和临界区都为空的时候就阻塞，不然弹幕池和临界区要不停刷新的
                    Log.i("zhangge", "swapList.size()=" + swapList.size() + ",cacheSize=" + cacheSize);
                    if (swapList.size() == 0 && cacheSize == 0) {
                        try {
                            synchronized (mWaiting) {
                                mWaiting.set(true);
                                mWaiting.wait();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    //替换临界区的弹幕
                    if (swapList.size() > 0) {
                        setDanmakus(swapList);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * 生成一条弹幕，如果没有可用弹道就返回null
     *
     * @param item
     * @return
     */
    private ZGDanmaku generateDanmaku(ZGDanmakuItem item) {

        int avaliableLine = getAvaliableLine();
        if (avaliableLine == -1) {
            return null;
        }

        ZGDanmaku danmaku = new ZGDanmaku(item.getDanmakuBitmap());
        mLinesAvaliable.put(avaliableLine, danmaku);

        float offsetY = (item.getDanmakuHeight() + mLineSpace) * avaliableLine;
        danmaku.setOffsetY(offsetY);

        return danmaku;
    }

    /**
     * 获取有效的弹道
     *
     * @return
     */
    private synchronized int getAvaliableLine() {
        for (int i = 0; i < mLines; i++) {
            if (mLinesAvaliable.get(i) == null) {
                return i;
            }
            ZGDanmaku danmaku = mLinesAvaliable.get(i);
            if (danmaku.getCurrentOffsetX() > danmaku.getDanmakuWidth()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 设置弹幕的shader和view宽高
     * @param danmaku
     */
    public void updateDanmakus(ZGDanmaku danmaku) {
        danmaku.setShader(mVertexShader, mFragmentShader);
        danmaku.setViewSize(mViewWidth, mViewHeight);
    }

    /**
     * 设置速度
     *
     * @param speed dp/s
     */
    public void setSpeed(float speed) {
        float pxSpeed = DimensUtils.dip2pixel(mContext, speed);
        this.mSpeed = pxSpeed;
    }

    /**
     * 设置行数
     *
     * @param lines
     */
    public void setLines(int lines) {
        this.mLines = lines;
    }

    /**
     * 设置行距
     *
     * @param lineSpace
     */
    public void setLineSpace(int lineSpace) {
        float pxLineSpace = DimensUtils.dip2pixel(mContext, lineSpace);
        this.mLineSpace = pxLineSpace;
    }

    public synchronized List<ZGDanmaku> getDanmakus() {
        return mDanmakus;
    }

    /**
     * 设置临界区弹幕
     * @param danmakus
     */
    public synchronized void setDanmakus(List<ZGDanmaku> danmakus) {
        this.mDanmakus = danmakus;
    }

    /**
     * 发一条弹幕
     *
     * @param text
     */
    public void shotDanmamku(String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(mCanvas, mPainter, text);
        synchronized (mCachedDanmaku) {
            mCachedDanmaku.offer(item);
        }

        synchronized (mWaiting) {
            if (mWaiting.get()) {
                mWaiting.set(false);
                mWaiting.notifyAll();
            }
        }
    }

    private class ZGDanmakuTextviewRenderer implements Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glEnable(GLES20.GL_BLEND);

            //指定混色方案
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            //加载顶点着色器的脚本内容
            mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", mContext.getResources());

            //加载片元着色器的脚本内容
            mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", mContext.getResources());
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

            start();

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
            List<ZGDanmaku> danmakus = getDanmakus();
            int size = danmakus.size();
            for (int i = 0; i < size; i ++) {
                ZGDanmaku danmaku = danmakus.get(i);

                if(!isPaused) {
                    float newOffset = detalOffset + danmaku.getCurrentOffsetX();
                    danmaku.setOffsetX(newOffset);
                }

                if (isOpen) {
                    danmaku.drawDanmaku();
                }
            }

            mLastTime = currentTime;
        }

        @Override
        public void onSurfaceDestroyed(GL10 gl) {

        }
    }
}
