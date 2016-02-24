package org.zhgeaits.zgdanmaku;

import android.opengl.GLES20;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhgeaits on 16/2/24.
 */
public class TexturePool {

    private static Queue<Integer> mPool = new LinkedList<>();

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
}
