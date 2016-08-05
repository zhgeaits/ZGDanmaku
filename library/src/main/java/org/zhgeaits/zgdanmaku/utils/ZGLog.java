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

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by zhgeaits on 16/8/5.
 */
public class ZGLog {

    public final static String TAG = "ZGDanmaku";
    private static boolean isDebug = false;

    public static void d(String msgs) {
        if (isDebug) {
            Log.d(TAG, msgs);
        }
    }

    public static void e(String msgs) {
        Log.e(TAG, msgs);
    }

    public static void i(String msgs) {
        Log.i(TAG, msgs);
    }

    public static void e(String msg, Throwable ex) {
        Log.e(TAG, msg + ":" + getExceptionMsg(ex));
    }

    private static String getExceptionMsg(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        return result;
    }

}
