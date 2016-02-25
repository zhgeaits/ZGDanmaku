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

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhgeaits on 16/2/24.
 * 纹理池
 * 没必要每条弹幕都创建一个纹理，只要弹幕跑完了就回收纹理，
 * 下次重用这个纹理，重新画上bitmap即可。
 * 同理，gl程序也没必要每条弹幕都创建，只要创建一个即可。
 */
public class TexturePool {

    private static Queue<Integer> mPool = new LinkedList<>();

    private static int mProgram = -1;//自定义渲染管线程序id
    private static int muMVPMatrixHandle = -1;//总变换矩阵引用id
    private static int maPositionHandle = -1; //顶点位置属性引用id
    private static int maTexCoorHandle = -1; //顶点纹理坐标属性引用id

    /**
     * 从纹理池获取一个纹理id
     * @return 如果池没有就生成一个
     */
    public synchronized static int pollTextureId() {
        if (mPool.size() > 0) {
            return mPool.poll();
        }

        //生成纹理ID
        int[] textures = new int[1];

        //第一个参数是生成纹理的数量
        GLES20.glGenTextures(1, textures, 0);

        return textures[0];
    }

    /**
     * 往纹理池中添加一个纹理id，下次即可使用
     * @param textureId
     */
    public synchronized static void offerTextureId(int textureId) {
        mPool.offer(textureId);
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
}

