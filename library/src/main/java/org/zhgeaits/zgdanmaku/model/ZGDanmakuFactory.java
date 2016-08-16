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
package org.zhgeaits.zgdanmaku.model;

/**
 * Created by zhgeaits on 16/8/16.
 * 创建弹幕的工厂类
 */
public class ZGDanmakuFactory {

    public static ZGDanmakuItem createTextDanmaku(long id, String text) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        return item;
    }

    public static ZGDanmakuItem createTextDanmaku(long id, String text, int color) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setTextColor(color);
        return item;
    }

    public static ZGDanmakuItem createTextDanmaku(long id, String text, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setTextSize(size);
        return item;
    }

    /**
     * 带有颜色和大小
     * @param text
     * @param color
     * @param size 单位dp
     */
    public static ZGDanmakuItem createTextDanmaku(long id, String text, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setTextSize(size);
        item.setTextColor(color);
        return item;
    }

    /**
     * 创建一条弹幕, 带有颜色和大小
     * @param id
     * @param resid icon头像的id
     * @param text
     * @param color
     * @param size 单位dp
     */
    public static ZGDanmakuItem createRichDanmaku(long id, int resid, String text, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setHeadIcon(resid);
        item.setTextSize(size);
        item.setTextColor(color);
        return item;
    }

    public static ZGDanmakuItem createTextDanmaku(long id, String text, long time) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setOffsetTime(time);
        return item;
    }

    public static ZGDanmakuItem createTextDanmaku(long id, String text, long time, int color) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setOffsetTime(time);
        item.setTextColor(color);
        return item;
    }

    public static ZGDanmakuItem createTextDanmaku(long id, String text, long time, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setOffsetTime(time);
        item.setTextSize(size);
        return item;
    }

    /**
     * 带有颜色和大小
     * @param text
     * @param time
     * @param color
     * @param size 单位dp
     */
    public static ZGDanmakuItem createTextDanmaku(long id, String text, long time, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setOffsetTime(time);
        item.setTextSize(size);
        item.setTextColor(color);
        return item;
    }

    /**
     * 带有颜色和大小, 和icon头像的id
     * @param id
     * @param resid icon头像的id
     * @param text
     * @param time
     * @param color
     * @param size 单位dp
     */
    public static ZGDanmakuItem createRichDanmaku(long id, int resid, String text, long time, int color, float size) {
        ZGDanmakuItem item = new ZGDanmakuItem(id, text);
        item.setHeadIcon(resid);
        item.setTextSize(size);
        item.setTextColor(color);
        item.setOffsetTime(time);
        return item;
    }
}
