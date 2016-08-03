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

import org.zhgeaits.zgdanmaku.model.ZGDanmakuItem;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by zhgeaits on 16/2/25.
 * 弹幕池
 */
public class ZGDanmakuPool {

    private PriorityQueue<ZGDanmakuItem> mCachedDanmaku;

    public ZGDanmakuPool() {
        mCachedDanmaku = new PriorityQueue<>();
    }

    public synchronized void offer(ZGDanmakuItem danmakuItem) {
        mCachedDanmaku.offer(danmakuItem);
        wakeIfNeed();
    }

    public synchronized ZGDanmakuItem poll() {
        return mCachedDanmaku.poll();
    }

    public synchronized int size() {
        return mCachedDanmaku.size();
    }

    public synchronized void wakeIfNeed() {
        notifyAll();
    }

    public synchronized boolean waitIfNeed() throws InterruptedException{
        if (size() == 0) {
            wait();
            return true;
        } else {
            return false;
        }
    }

    public synchronized void clear() {
        mCachedDanmaku.clear();
    }

}
