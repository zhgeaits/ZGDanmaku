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

/**
 * Created by zhgeaits on 16/8/11.
 * 用于同步的计时器
 */
public class ZGTimer {

    private static ZGTimer instance;

    private long time;

    public static ZGTimer getInstance() {
        if (instance == null) {
            synchronized (ZGTimer.class) {
                if (instance == null) {
                    instance = new ZGTimer();
                }
            }
        }
        return instance;
    }

    public long getTime() {
        return time;
    }

    public synchronized void addInterval(long interval) {
        time += interval;
    }

    public synchronized void syncTime(long time) {
        this.time = time;
    }
}
