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
package org.zhgeaits.zgdanmaku.view;

import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

import java.util.List;

/**
 * Created by zhgeaits on 16/2/25.
 * 弹幕控件接口
 */
public interface IZGDanmakuView {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 隐藏
     */
    void hide();

    /**
     * 显示
     */
    void show();

    /**
     * 暂停
     */
    void pause();

    /**
     * 重启
     */
    void resume();

    /**
     * 是否已经开始
     */
    boolean isStarted();

    /**
     * 是否暂停
     * @return
     */
    boolean isPaused();

    /**
     * 是否隐藏了
     * @return
     */
    boolean isHided();

    /**
     * 设置行数
     * @param lines
     */
    void setLines(int lines);

    /**
     * 设置行距
     * @param leading 单位dp
     */
    void setLeading(float leading);

    /**
     * 设置弹道行高
     * @param height 字体的大小,单位sp
     */
    void setLineHeight(float height);

    /**
     * 设置速度
     * 每条弹幕都是不同的速度,也可以给弹幕设置固定速度
     * @param speed 单位dp/s
     */
    @Deprecated
    void setSpeed(float speed);

    /**
     * seek时间
     * @param time
     */
    void seek(long time);

    /**
     * 同步弹幕时间
     * @param time
     */
    void syncTime(long time);

    /**
     * 发送一条弹幕
     * @param item
     */
    void shotDanmaku(ZGDanmakuItem item);

    /**
     * 发送一批弹幕
     * @param items
     */
    void shotDanmakuList(List<ZGDanmakuItem> items);
}
