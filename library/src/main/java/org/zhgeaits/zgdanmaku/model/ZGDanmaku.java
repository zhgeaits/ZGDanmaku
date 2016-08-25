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
package org.zhgeaits.zgdanmaku.model;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Build;

import org.zhgeaits.zgdanmaku.utils.TexturePool;
import org.zhgeaits.zgdanmaku.utils.ZGMatrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕类
 * 这个用于渲染在Opengl
 */
public class ZGDanmaku {

    /**
     * 这里默认设置纹理id为-1值，在OpenGLES底下，负值是无效的，如果不设置负值，那么int默认是0，是有效的纹理id
     * 当一个弹幕还没有绑定到纹理的时候，业务端触发所有弹幕回收，就会触发回收纹理0,这样在纹理池就有重复的纹理了,
     * 会导致同一个bitmap映射到不通的纹理,但是相同的纹理会在不同的矩阵参数下进行绘制,出现混乱现象了.
     */
    private int mTextureId = -1;                            //绑定的纹理id
    private int mProgram;                                   //自定义渲染管线程序id
    private int muMVPMatrixHandle;                          //总变换矩阵引用id
    private int maPositionHandle;                           //顶点位置属性引用id
    private int maTexCoorHandle;                            //顶点纹理坐标属性引用id

    private int mOffsetXHandle;                             //x偏移量引用id
    private int mOffsetYHandle;                             //y偏移量引用id
    private int mOffsetZHandle;                             //z偏移量引用id
    private int mViewWidthHandle;                           //屏幕宽引用id
    private int mViewHeightHandle;                          //屏幕高引用id

    private String mVertexShader;                           //顶点着色器
    private String mFragmentShader;                         //片元着色器

    private FloatBuffer mVertexBuffer;                      //顶点坐标数据缓冲
    private FloatBuffer mTexCoorBuffer;                     //顶点纹理坐标数据缓冲

    private Bitmap mBitmap;                                 //弹幕纹理
    private float offsetX;                                  //偏移的x坐标，范围是0-mViewWidth
    private float offsetY;                                  //偏移的y坐标，范围是0-mViewHeight
    private int mViewWidth;                                 //窗口宽度
    private int mViewHeight;                                //窗口高度
    private int mDanmakuWidth;                              //弹幕宽度
    private int mDanmakuHeight;                             //弹幕高度
    private int mVertexCount = 4;                           //纹理顶点个数，这个是矩形，四个顶点
    private boolean isInited = false;                       //是否已经初始化
    private ZGMatrix mMatrix;                               //矩阵
    private float mDetalX;                                  //步长, 每毫秒移动的距离
    private float mStep;                                    //每帧移动的距离

    public ZGDanmaku(Bitmap bitmap) {
        this.mBitmap = bitmap;
        if (mBitmap == null) {
            return;
        }
        this.mDanmakuWidth = mBitmap.getWidth();
        this.mDanmakuHeight = mBitmap.getHeight();

        //初始化矩阵
        mMatrix = new ZGMatrix();
        mMatrix.initMatrix();
    }

    public boolean init() {
        if (mBitmap == null) {
            return false;
        }

        //初始化顶点坐标与纹理坐标
        initVertexData();

        //初始化着色器
        initShader();

        //生成纹理，必须在opengl的线程绑定纹理才有用
        if(!initTexture()) {
            return false;
        }

        isInited = true;
        return isInited;
    }

    /**
     * 设置着色器
     *
     * @param vertexShader   顶点着色器
     * @param fragmentShader 片元着色器
     */
    public void setShader(String vertexShader, String fragmentShader) {
        this.mVertexShader = vertexShader;
        this.mFragmentShader = fragmentShader;
    }

    /**
     * 设置view的宽高
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        this.mViewWidth = width;
        this.mViewHeight = height;
    }

    /**
     * 设置行偏移量
     * @param offsety
     */
    public void setOffsetY(float offsety) {
        this.offsetY = offsety;
    }

    /**
     * 获取Y偏移量
     * @return
     */
    public float getOffsetY() {
        return offsetY;
    }

    /**
     * 设置列偏移量
     * @param offsetx
     */
    public void setOffsetX(float offsetx) {
        this.offsetX = offsetx;
    }

    /**
     * 增加列偏移量
     * @param detalOffsetx
     */
    public void addDetalOffsetX(float detalOffsetx) {
        this.offsetX += detalOffsetx;
    }

    /**
     * 移动
     * @param time
     */
    public void move(long time) {
        if (mStep == 0) {
            mStep = mDetalX * time;
        }
        if (time > 0) {
            this.offsetX += mStep;
        }
    }

    /**
     * 获取1ms移动的距离
     * @return
     */
    public float getDetalX() {
        return mDetalX;
    }

    /**
     * 设置1ms移动的距离
     * @param detalX
     */
    public void setDetalX(float detalX) {
        this.mDetalX = detalX;
    }

    /**
     * 获取当前的行偏移量
     * @return
     */
    public float getCurrentOffsetX() {
        return this.offsetX;
    }

    /**
     * 获取弹幕宽度
     * @return
     */
    public int getDanmakuWidth() {
        return mDanmakuWidth;
    }

    /**
     * 获取弹幕高度
     * @return
     */
    public int getDanmakuHeight() {
        return mDanmakuHeight;
    }

    /**
     * 判断是否已经结束了
     * @return
     */
    public boolean isFinished() {
        if(offsetX > mViewWidth + getDanmakuWidth()) {
            uninit();
            return true;
        }
        return false;
    }

    /**
     * 初始化顶点坐标与纹理坐标
     */
    public void initVertexData() {
        //顶点坐标数据
        //顶点坐标系：窗口取值范围是-1至1，所以，左上角坐标是(-1,1),中点坐标是(0,0)，右下角坐标是(1, -1)
        //其实就是把坐标给归一化了，下面是计算弹幕的归一化宽和高
        //实际上顶点坐标是正负1的，所以弹幕在坐标系中要乘以2，放大两倍
        float danmakuHeight = (float) mBitmap.getHeight() / mViewHeight * 2.0f;
        float danmakuWidth = (float) mBitmap.getWidth() / mViewWidth * 2.0f;

        //弹幕四个角的顶点坐标，我默认把它绘制在屏幕的左上角了，这样方便理解偏移计算
        //为什么不是顺时针或者逆时针，实际上opengl只能绘制三角形，所以，
        //这里其实是绘制了两个三角形的，前三个点和后三个点分别是三角形
        float vertices[] = new float[]
                {
                        -1, 1, 0,                                   //左上角
                        -(1 - danmakuWidth), 1, 0,                  //右上角
                        -1, 1 - danmakuHeight, 0,                   //左下角
                        -(1 - danmakuWidth), 1 - danmakuHeight, 0   //右下角
                };

        //一个float是4个字节，所以*4
        //关于ByteBuffer，可以看nio相关知识：http://zhgeaits.me/java/2013/06/17/Java-nio.html
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        //把纹理绘制到矩形中去，就需要指定读取纹理的坐标
        //纹理坐标系：范围是0-1，左上角坐标是(0,0),右下角坐标是(1,1)
        float texCoor[] = new float[]
                {
                        0, 0,   //左上角
                        1, 0,   //右上角
                        0, 1,   //左下角
                        1, 1    //右下角
                };

        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    /**
     * 初始化着色器
     */
    public void initShader() {
        mProgram = TexturePool.getProgram(mVertexShader, mFragmentShader);
        maPositionHandle = TexturePool.getMaPositionHandle();
        maTexCoorHandle = TexturePool.getMaTexCoorHandle();
        muMVPMatrixHandle = TexturePool.getMuMVPMatrixHandle();

        mOffsetXHandle = TexturePool.getOffsetXHandle();
        mOffsetYHandle = TexturePool.getOffsetYHandle();
        mOffsetZHandle = TexturePool.getOffsetZHandle();
        mViewWidthHandle = TexturePool.getViewWidthHandle();
        mViewHeightHandle = TexturePool.getViewHeightHandle();
    }

    /**
     * 初始化纹理
     */
    private boolean initTexture() {
        mTextureId = TexturePool.pollTextureId();

        if(mTextureId < 0) {
            return false;
        }

        //绑定纹理，并指定纹理的采样方式
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //把bitmap映射到纹理
        //纹理类型在OpenGL ES中必须为GL10.GL_TEXTURE_2D
        //第二个参数是纹理的层次，0表示基本图像层，可以理解为直接贴图
        //最后一个参数是纹理边框尺寸
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        //使用glTexImage2D的原因是它比texImage2D效率更高一些,据说texImage2D会把bitmap重新创建一次,我没有去验证
        int byteCount;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            byteCount = mBitmap.getByteCount();
        } else {
            byteCount = mBitmap.getRowBytes() * mBitmap.getHeight();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteCount);
        byteBuffer.order(ByteOrder.nativeOrder());
        mBitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.position(0);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(),
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

        mBitmap.recycle();
//        BitmapPool.getInstance().cacheOrRecycle(mBitmap);

        return true;
    }

    public void uninit() {
        TexturePool.offerTextureId(mTextureId);
        mTextureId = -1;
    }

    /**
     * 绘制弹幕
     */
    public void drawDanmaku() {

        if((!isInited && !init()) || mTextureId < 0) {
            return;
        }

        //使用shader程序
        GLES20.glUseProgram(mProgram);

        //将屏幕宽高和偏移坐标
        GLES20.glUniform1f(mViewWidthHandle, mViewWidth);
        GLES20.glUniform1f(mViewHeightHandle, mViewHeight);
        GLES20.glUniform1f(mOffsetXHandle, -offsetX);
        GLES20.glUniform1f(mOffsetYHandle, -offsetY);
        GLES20.glUniform1f(mOffsetZHandle, 0);

        //将矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMatrix.getInitMatrix(), 0);

        //传递顶点位置数据
        //坐标是xyz三维，所以size是3，每个float是4个字节，所以stride是3 * 4
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);

        //传递纹理坐标数据
        //坐标是xy二维的，所以size是2
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);

        //允许使用坐标数据数组
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
    }
}
