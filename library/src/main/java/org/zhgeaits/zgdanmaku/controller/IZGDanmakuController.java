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
package org.zhgeaits.zgdanmaku.controller;

import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

/**
 * Created by zhgeaits on 16/2/25.
 * 弹幕控制器接口
 */
public interface IZGDanmakuController {

    /**
     * 启动弹幕
     */
    void start();

    /**
     * 停止弹幕
     */
    void stop();

    /**
     * 隐藏弹幕
     */
    void hide();

    /**
     * 显示弹幕
     */
    void show();

    /**
     * 暂停弹幕
     */
    void pause();

    /**
     * 重新开始弹幕
     */
    void resume();

    boolean isPause();

    boolean isHide();

    /**
     * 设置弹幕行数
     * @param lines
     */
    void setLines(int lines);

    /**
     * 设置弹幕行距
     * @param leading
     */
    void setLeading(float leading);

    /**
     * 设置弹幕速度
     * @param speed
     */
    void setSpeed(float speed);

    /**
     * 添加一个弹幕
     * @param danmakuItem
     */
    void addDanmaku(ZGDanmakuItem danmakuItem);
}
