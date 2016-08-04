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
package org.zhgeaits.zgdanmaku.utils;

import android.opengl.GLES20;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhgeaits on 16/2/24.
 * 纹理池
 * 没必要每条弹幕都创建一个纹理，只要弹幕跑完了就回收纹理，
 * 下次重用这个纹理，重新画上bitmap即可。
 * 同理，gl程序也没必要每条弹幕都创建，只要创建一个即可。
 */
public class TexturePool {

    private static Set<Integer> mPool = new HashSet<>();

    private static int mProgram = -1;               //自定义渲染管线程序id
    private static int muMVPMatrixHandle = -1;      //总变换矩阵引用id
    private static int maPositionHandle = -1;       //顶点位置属性引用id
    private static int maTexCoorHandle = -1;        //顶点纹理坐标属性引用id
    private static int mOffsetXHandle = -1;                             //x偏移量引用id
    private static int mOffsetYHandle = -1;                             //y偏移量引用id
    private static int mOffsetZHandle = -1;                             //z偏移量引用id
    private static int mViewWidthHandle = -1;                           //屏幕宽引用id
    private static int mViewHeightHandle = -1;                          //屏幕高引用id

    public static void uninit() {
        mProgram = -1;
        muMVPMatrixHandle = -1;
        maPositionHandle = -1;
        maTexCoorHandle = -1;
    }

    /**
     * 从纹理池获取一个纹理id
     * @return 如果池没有就生成一个
     */
    public synchronized static int pollTextureId() {
        if (mPool.size() > 0) {
            int texureId = mPool.iterator().next();
            mPool.remove(texureId);
            return texureId;
        }
        //生成纹理ID
        //一次生成4个id, 这里怀疑，如果生成了一个id，但是还没使用，即没有绑定，那么下次还会生成这个id
        int[] textures = new int[4];
        //第一个参数是生成纹理的数量
        GLES20.glGenTextures(4, textures, 0);

        for (int i = 1; i < 4; i ++) {
            if(textures[i] > -1) {
                mPool.add(textures[i]);
            }
        }

        return textures[0];
    }

    /**
     * 往纹理池中添加一个纹理id，下次即可使用
     * @param textureId
     */
    public synchronized static void offerTextureId(int textureId) {
        if(textureId > -1) {
            mPool.add(textureId);
        }
    }

    /**
     * 获取gl程序索引
     * @param mVertexShader
     * @param mFragmentShader
     * @return
     */
    public static int getProgram(String mVertexShader, String mFragmentShader) {
        if (mProgram == -1) {
            //基于顶点着色器与片元着色器创建程序
            mProgram = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        }
        return mProgram;
    }

    /**
     * 获取总变换矩阵引用id
     * @return
     */
    public static int getMuMVPMatrixHandle() {
        if(muMVPMatrixHandle == -1) {
            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        }
        return muMVPMatrixHandle;
    }

    /**
     * 获取顶点位置属性引用id
     * @return
     */
    public static int getMaPositionHandle() {
        if(maPositionHandle == -1) {
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        }
        return maPositionHandle;
    }

    /**
     * 获取顶点纹理坐标属性引用id
     * @return
     */
    public static int getMaTexCoorHandle() {
        if(maTexCoorHandle == -1) {
            maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        }
        return maTexCoorHandle;
    }

    public static int getOffsetXHandle() {
        if (mOffsetXHandle == -1) {
            mOffsetXHandle = GLES20.glGetUniformLocation(mProgram, "offsetX");
        }
        return mOffsetXHandle;
    }

    public static int getOffsetYHandle() {
        if (mOffsetYHandle == -1) {
            mOffsetYHandle = GLES20.glGetUniformLocation(mProgram, "offsetY");
        }
        return mOffsetYHandle;
    }

    public static int getOffsetZHandle() {
        if (mOffsetZHandle == -1) {
            mOffsetZHandle = GLES20.glGetUniformLocation(mProgram, "offsetZ");
        }
        return mOffsetZHandle;
    }

    public static int getViewWidthHandle() {
        if (mViewWidthHandle == -1) {
            mViewWidthHandle = GLES20.glGetUniformLocation(mProgram, "mViewWidth");
        }
        return mViewWidthHandle;
    }

    public static int getViewHeightHandle() {
        if (mViewHeightHandle == -1) {
            mViewHeightHandle = GLES20.glGetUniformLocation(mProgram, "mViewHeight");
        }
        return mViewHeightHandle;

    }
}

