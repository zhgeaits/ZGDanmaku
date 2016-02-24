package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕渲染器
 */
public class ZGDanmakuRenderer implements GLSurfaceView.Renderer {

    private Context mContext;
    private List<ZGDanmaku> mDanmakus;              //弹幕临界区
    private RenderListener mListener;               //初始化成功监听器
    private String mVertexShader;                   //顶点着色器
    private String mFragmentShader;                 //片元着色器
    private int mViewWidth;                         //窗口宽度
    private int mViewHeight;                        //窗口高度
    private float mSpeed;                           //速度，单位px/s
    private long mLastTime;                         //绘制上一帧的时间
    private boolean isOpen = true;                  //是否打开弹幕
    private boolean isPaused = false;               //是否暂停弹幕

    public ZGDanmakuRenderer(Context context) {
        mDanmakus = new ArrayList<>();
        this.mContext = context;
    }

    /**
     * 设置初始化成功监听器
     * @param listener
     */
    public void setListener(RenderListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置速度
     * @param speed 单位px/s
     */
    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    /**
     * 获取临界区弹幕
     * @return
     */
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
     * 设置弹幕的shader和view宽高
     * @param danmaku
     */
    public void updateDanmakus(ZGDanmaku danmaku) {
        danmaku.setShader(mVertexShader, mFragmentShader);
        danmaku.setViewSize(mViewWidth, mViewHeight);
    }

    /**
     * 设置弹幕是否打开
     * @param isOpen
     */
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * 弹幕是否打开
     * @return
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 设置弹幕是否暂停
     * @param isPaused
     */
    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    /**
     * 弹幕是否暂停
     * @return
     */
    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //关闭背面剪裁
        //GLES20.glDisable(GLES20.GL_CULL_FACE);

        //开启混色功能，这样是为了让png图片的透明能显示
        GLES20.glEnable(GLES20.GL_BLEND);

        //指定混色方案
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtils.loadFromAssetsFile("vertex.sh", mContext.getResources());

        //加载片元着色器的脚本内容
        mFragmentShader = ShaderUtils.loadFromAssetsFile("frag.sh", mContext.getResources());

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

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

        if(mListener != null) {
            mListener.onInited();
        }

        mLastTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

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

    public interface RenderListener {
        void onInited();
    }

}
