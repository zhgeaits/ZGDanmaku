package org.zhgeaits.zgdanmaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/22 0022.
 */
public class ZGDanmakuView extends GLSurfaceView {

    private ZGDanmakuRenderer mRenderer;//场景渲染器
    private boolean isInited = false;
    private List<Bitmap> cached = new ArrayList<>();

    public ZGDanmakuView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new ZGDanmakuRenderer(context);	//创建场景渲染器
        setRenderer(mRenderer);				//设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染

        mRenderer.setListener(new ZGDanmakuRenderer.RenderListener() {
            @Override
            public void onInited() {
                isInited = true;
                if(cached.size() > 0) {
                    float x = 1;
                    for (Bitmap bitmap : cached) {
                        showBitmap(bitmap, x);
                        x++;
                    }
                    cached.clear();
                }
            }
        });
    }

    public void showBitmap(Bitmap bitmap, float x) {
        ZGDanmaku danmaku = new ZGDanmaku(bitmap, this.getContext());
        danmaku.x = x;
        mRenderer.addDanmaku(danmaku);
    }

    public void shotDanmamku(String text) {
        //通过输入流加载图片===============begin===================
        InputStream is = this.getResources().openRawResource(R.drawable.ic_launcher);
        Bitmap bitmapTmp;
        try {
            bitmapTmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通过输入流加载图片===============end=====================

        if(isInited) {
            showBitmap(bitmapTmp, 0);
        } else {
            cached.add(bitmapTmp);
        }
    }


}
