package org.zhgeaits.zgdanmaku;

import android.opengl.Matrix;

/**
 * 存储系统矩阵状态的类
 */
public class MatrixUtils {

    private static float[] mProjectMatrix = new float[16];//4x4矩阵 投影用
    private static float[] mCameraMatrix = new float[16];//摄像机位置朝向9参数矩阵
    private static float[] mTranslateMatrix = new float[16];//具体物体的移动旋转矩阵
    private static float[] mFinalMatrix;//最后起作用的总变换矩阵

    /**
     * 获取不变换初始矩阵
     */
    public static void setInitStack() {
        //setRotateM这个暂时不理解怎么用
//        Matrix.setRotateM(mTranslateMatrix, 0, 0, 1, 0, 0);
        Matrix.setIdentityM(mTranslateMatrix, 0);
    }

    /**
     * 设置沿xyz轴移动
     *
     * @param x
     * @param y
     * @param z
     */
    public static void transtate(float x, float y, float z) {
        Matrix.translateM(mTranslateMatrix, 0, x, y, z);
    }

    /**
     * 设置绕xyz轴转动
     *
     * @param angle
     * @param x
     * @param y
     * @param z
     */
    public static void rotate(float angle, float x, float y, float z)/**/ {
        Matrix.rotateM(mTranslateMatrix, 0, angle, x, y, z);
    }


    /**
     * 设置摄像机
     *
     * @param cx  摄像机位置x
     * @param cy  摄像机位置y
     * @param cz  摄像机位置z
     * @param tx  摄像机目标点x
     * @param ty  摄像机目标点y
     * @param tz  摄像机目标点z
     * @param upx 摄像机UP向量X分量
     * @param upy 摄像机UP向量Y分量
     * @param upz 摄像机UP向量Z分量
     */
    public static void setCamera(float cx, float cy, float cz, float tx, float ty, float tz, float upx, float upy, float upz) {
        Matrix.setLookAtM(mCameraMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }

    /**
     * 设置透视投影参数
     *
     * @param left   near面的left
     * @param right  near面的right
     * @param bottom near面的bottom
     * @param top    near面的top
     * @param near   near面距离
     * @param far    far面距离
     */
    public static void setProject(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mProjectMatrix, 0, left, right, bottom, top, near, far);
    }

    /**
     * 设置正交投影参数
     *
     * @param left   near面的left
     * @param right  near面的right
     * @param bottom near面的bottom
     * @param top    near面的top
     * @param near   near面距离
     * @param far    far面距离
     */
    public static void setProjectOrtho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mProjectMatrix, 0, left, right, bottom, top, near, far);
    }

    /**
     * 获取具体物体的总变换矩阵
     *
     * @return
     */
    public static float[] getFinalMatrix() {
        mFinalMatrix = new float[16];
        Matrix.multiplyMM(mFinalMatrix, 0, mCameraMatrix, 0, mTranslateMatrix, 0);
        Matrix.multiplyMM(mFinalMatrix, 0, mProjectMatrix, 0, mFinalMatrix, 0);
        return mFinalMatrix;
    }
}
