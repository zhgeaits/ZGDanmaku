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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhgeaits on 16/8/9.
 * Bitmap的缓存
 * 来自:
 * https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
 * https://developer.android.com/training/displaying-bitmaps/manage-memory.html
 * <p>
 */
public class BitmapPool {

    private static BitmapPool instance;

    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    public static BitmapPool getInstance() {
        if (instance == null) {
            synchronized (BitmapPool.class) {
                if (instance == null) {
                    instance = new BitmapPool();
                }
            }
        }
        return instance;
    }

    private BitmapPool() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
        }
    }

    private void size() {
        long size = 0;
        synchronized (mReusableBitmaps) {
            final Iterator<SoftReference<Bitmap>> iterator
                    = mReusableBitmaps.iterator();
            Bitmap item;

            while (iterator.hasNext()) {
                item = iterator.next().get();

                if (null != item && item.isMutable()) {
                    size += item.getByteCount();
                }
            }
        }
//        ZGLog.i("size=" + (size/1024.0/1024.0));
    }

    /**
     * 缓存起来或者回收掉.
     * @param bitmap
     */
    public synchronized void cacheOrRecycle(Bitmap bitmap) {
        if (bitmap != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                mReusableBitmaps.add(new SoftReference<Bitmap>(bitmap));
            } else {
                bitmap.recycle();
            }
        }
//        size();
    }

    /**
     * This method iterates through the reusable bitmaps, looking for one to use for inBitmap:
     *
     * @param options
     * @return
     */
    public synchronized Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator
                        = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap.
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();

        if (bitmap == null) return 0;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
