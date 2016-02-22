package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/2/22 0022.
 */
public class ZGDanmakuRenderer implements GLSurfaceView.Renderer {

    private List<ZGDanmaku> danmakus;
    private Context context;
    private RenderListener listener;

    public ZGDanmakuRenderer(Context context) {
        danmakus = new ArrayList<>();
        this.context = context;
    }

    public void setListener(RenderListener listener) {
        this.listener = listener;
    }

    public void addDanmaku(ZGDanmaku danmaku) {
        danmakus.add(danmaku);
    }

    public interface RenderListener {
        void onInited();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        //打开深度检测
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //关闭背面剪裁
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        if(listener != null) {
            listener.onInited();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置视窗大小及位置
        GLES20.glViewport(0, 0, width, height);
        //计算GLSurfaceView的宽高比
        float ratio = (float) width / height;
        //调用此方法计算产生透视投影矩阵
        MatrixState.setProject(-ratio, ratio, -1, 1, 1, 10);
        //调用此方法产生摄像机9参数位置矩阵
        MatrixState.setCamera(0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        List<ZGDanmaku> shouldRemoved = new ArrayList<>();
        //绘制纹理矩形
        for (int i = 0; i < danmakus.size(); i ++) {
            danmakus.get(i).drawSelf();
            if (danmakus.get(i).isFinished()) {
                shouldRemoved.add(danmakus.get(i));
            }
        }
        for (int i = 0; i < shouldRemoved.size(); i ++) {
            danmakus.remove(shouldRemoved.get(i));
        }
    }

}
