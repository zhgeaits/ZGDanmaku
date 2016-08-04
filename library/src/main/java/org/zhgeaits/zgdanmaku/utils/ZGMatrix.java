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

import android.opengl.Matrix;

/**
 * Created by zhgeaits on 16/8/3.
 * 实例版的MatrixUtils,这样不必每次都在onDrawFrame计算
 * 初始化的时候把位置移到右上角,并先乘了摄像机和投影矩阵.
 * 在每次绘制的时候再计算转换矩阵和最终的矩阵,放到OpenGLES里面,因为GPU是并行计算的.
 */
public class ZGMatrix {

    private float[] mInitMatrix = new float[16];

    public void initMatrix() {
        Matrix.setIdentityM(mInitMatrix, 0);
        //首先把弹幕移动到右上角
        Matrix.translateM(mInitMatrix, 0, 2.0f, 0, 0);
        //可以先乘摄像机矩阵和投影矩阵,在每次绘制的时候再成转换矩阵
        Matrix.multiplyMM(mInitMatrix, 0, MatrixUtils.getCameraMatrix(), 0, mInitMatrix, 0);
        Matrix.multiplyMM(mInitMatrix, 0, MatrixUtils.getProjectMatrix(), 0, mInitMatrix, 0);
    }

    public float[] getInitMatrix() {
        return mInitMatrix;
    }

}
