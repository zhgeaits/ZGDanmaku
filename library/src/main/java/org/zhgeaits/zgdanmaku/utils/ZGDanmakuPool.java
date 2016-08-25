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

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by zhgeaits on 16/2/25.
 * 弹幕池
 */
public class ZGDanmakuPool {

    private PriorityQueue<ZGDanmakuItem> mCachedDanmaku;
    private Set<ZGDanmakuItem> mUniques;

    public ZGDanmakuPool() {
        mCachedDanmaku = new PriorityQueue<ZGDanmakuItem>();
        mUniques = new HashSet<ZGDanmakuItem>();
    }

    public synchronized void offer(ZGDanmakuItem danmakuItem) {
        if (!mUniques.contains(danmakuItem)) {
            mCachedDanmaku.offer(danmakuItem);
            wakeIfNeed();
        }
    }

    public synchronized void addAll(List<ZGDanmakuItem> items) {
        ZGLog.d("ZGDanmakuPool addAll:" + items.size());
        mCachedDanmaku.addAll(items);
        mUniques.addAll(items);
        wakeIfNeed();
    }

    public synchronized ZGDanmakuItem poll() {
        ZGDanmakuItem item = mCachedDanmaku.poll();
        mUniques.remove(item);
        return item;
    }

    public synchronized ZGDanmakuItem peek() {
        return mCachedDanmaku.peek();
    }

    public synchronized void remove(ZGDanmakuItem item) {
        mCachedDanmaku.remove(item);
        mUniques.remove(item);
    }

    public synchronized int size() {
        return mCachedDanmaku.size();
    }

    public synchronized void wakeIfNeed() {
        ZGLog.d("ZGDanmakuPool wakeIfNeed");

        notifyAll();
    }

    public synchronized boolean waitIfNeed() {
        if (size() == 0) {
            ZGLog.d("ZGDanmakuPool waitIfNeed");

            try {
                wait();
            } catch (InterruptedException e) {
                ZGLog.e("waitIfNeed error", e);
            }

            ZGLog.d("ZGDanmakuPool waitIfNeed resume");

            return true;
        } else {
            return false;
        }
    }

    public synchronized void clear() {
        ZGLog.i("ZGDanmakuPool clear size:" + mCachedDanmaku.size());
        mCachedDanmaku.clear();
        mUniques.clear();
    }

}
