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

import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 加载顶点Shader与片元Shader的工具类
 */
public class ShaderUtils {

    /**
     * 加载制定shader的方法
     *
     * @param shaderType shader的类型  GLES20.GL_VERTEX_SHADER   GLES20.GL_FRAGMENT_SHADER
     * @param source     shader的脚本字符串
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        //创建一个新shader
        int shader = GLES20.glCreateShader(shaderType);

        //若创建成功则加载shader
        if (shader != 0) {

            //加载shader的源代码
            GLES20.glShaderSource(shader, source);

            //编译shader
            GLES20.glCompileShader(shader);

            //存放编译成功shader数量的数组
            int[] compiled = new int[1];

            //获取Shader的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

            //若编译失败则显示错误日志并删除此shader
            if (compiled[0] == 0) {
                ZGLog.e("ES20_ERROR:" + "Could not compile shader " + shaderType + ":");
                ZGLog.e("ES20_ERROR:\n" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        ZGLog.d("loadShader result= " + shader + " shaderType=" + shaderType + ", source=" + source);
        return shader;
    }

    /**
     * 创建shader程序
     *
     * @param vertexSource
     * @param fragmentSource
     * @return 创建成功返回非0值，失败返回0
     */
    public static int createProgram(String vertexSource, String fragmentSource) {

        //加载顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        //加载片元着色器
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        //创建程序
        int program = GLES20.glCreateProgram();

        //若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {

            //向程序中加入顶点着色器
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");

            //向程序中加入片元着色器
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");

            //链接程序
            GLES20.glLinkProgram(program);

            //存放链接成功program数量的数组
            int[] linkStatus = new int[1];

            //获取program的链接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

            //若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                ZGLog.e("ES20_ERROR" + "Could not link program: ");
                ZGLog.e("ES20_ERROR" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        ZGLog.d("createProgram program=" + program + ", \nvertexSource[" + vertexSource + "]" + ", \nfragmentSource[" + fragmentSource + "]");
        return program;
    }

    /**
     * 检查每一步操作是否有错误的方法
     *
     * @param operation
     */
    public static void checkGlError(String operation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            ZGLog.e("ES20_ERROR" + operation + ": glError " + error);
            throw new RuntimeException(operation + ": glError " + error);
        }
    }

    /**
     * 从sh脚本中加载shader内容的方法
     *
     * @param fname
     * @param resources
     * @return
     */
    public static String loadFromAssetsFile(String fname, Resources resources) {
        String result = "";
        try {
            InputStream in = resources.getAssets().open(fname);
            int ch;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");

            ZGLog.d("loadFromAssetsFile result=" + result);
        } catch (Exception e) {
            ZGLog.e("loadFromAssetsFile", e);
        }
        return result;
    }
}
