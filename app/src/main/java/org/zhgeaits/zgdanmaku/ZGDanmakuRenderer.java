package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhgeatis on 2016/2/22 0022.
 * 弹幕渲染
 */
public class ZGDanmakuRenderer implements GLSurfaceView.Renderer {

    private List<ZGDanmaku> mDanmakus;
    private Context mContext;
    private RenderListener mListener;
    private String mVertexShader;//顶点着色器
    private String mFragmentShader;//片元着色器
    private int mViewWidth;//窗口宽度
    private int mViewHeight;//窗口高度

    public ZGDanmakuRenderer(Context context) {
        mDanmakus = new ArrayList<>();
        this.mContext = context;
    }

    public void setListener(RenderListener listener) {
        this.mListener = listener;
    }

    /**
     * 添加一个弹幕
     * @param danmaku
     */
    public void addDanmaku(ZGDanmaku danmaku) {
        danmaku.setShader(mVertexShader, mFragmentShader);
        danmaku.setViewSize(mViewWidth, mViewHeight);
        danmaku.init();
        mDanmakus.add(danmaku);
    }

    /**
     * 获取所有的弹幕
     * @return
     */
    public List<ZGDanmaku> getAllDanmakus() {
        return mDanmakus;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //设置屏幕背景色RGBA
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        //关闭背面剪裁
        GLES20.glDisable(GLES20.GL_CULL_FACE);

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
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        //清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        //绘制弹幕纹理
        //这样的写法是很不合理的，onDrawFrame回调是很频繁的，不应该new那么多list
        //我只是为了测试
        List<ZGDanmaku> shouldRemoved = new ArrayList<>();
        for (int i = 0; i < mDanmakus.size(); i ++) {
            mDanmakus.get(i).drawDanmaku();
            if (mDanmakus.get(i).isFinished()) {
                shouldRemoved.add(mDanmakus.get(i));
            }
        }

        //清除已经移出屏幕的弹幕
        for (int i = 0; i < shouldRemoved.size(); i ++) {
            mDanmakus.remove(shouldRemoved.get(i));
        }
    }

    public interface RenderListener {
        void onInited();
    }

}
