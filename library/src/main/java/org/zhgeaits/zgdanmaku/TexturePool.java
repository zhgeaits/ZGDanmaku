package org.zhgeaits.zgdanmaku;

import android.opengl.GLES20;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhgeaits on 16/2/24.
 */
public class TexturePool {

    private static Queue<Integer> mPool = new LinkedList<>();

    private static int mProgram = -1;//自定义渲染管线程序id
    private static int muMVPMatrixHandle = -1;//总变换矩阵引用id
    private static int maPositionHandle = -1; //顶点位置属性引用id
    private static int maTexCoorHandle = -1; //顶点纹理坐标属性引用id

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

    public synchronized static void offerTextureId(int textureId) {
        mPool.offer(textureId);
    }

    public static int getProgram(String mVertexShader, String mFragmentShader) {
        if (mProgram == -1) {
            //基于顶点着色器与片元着色器创建程序
            mProgram = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        }
        return mProgram;
    }

    public static int getMuMVPMatrixHandle() {
        if(muMVPMatrixHandle == -1) {
            //获取总变换矩阵引用id
            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        }
        return muMVPMatrixHandle;
    }

    public static int getMaPositionHandle() {
        if(maPositionHandle == -1) {
            //获取顶点位置属性引用id
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        }
        return maPositionHandle;
    }

    public static int getMaTexCoorHandle() {
        if(maTexCoorHandle == -1) {
            //获取顶点纹理坐标属性引用id
            maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        }
        return maTexCoorHandle;
    }
}

