package jp.wasabeef.glide.transformations;

/**
 * Copyright (C) 2018 Wasabeef
 * Copyright 2014 Google, Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.security.MessageDigest;

public class WatermarkTransformation extends BitmapTransformation {
    public static final int VERSION = 1;
    public static final String ID =
            "jp.wasabeef.glide.transformations.MaskTransformation." + VERSION;

    private Paint mWatermarkPaint = new Paint();

    private int mMargin;
    private Bitmap mWmBitmap;
    private Gravity mGravity;   //position

    public enum Gravity {
        TOP_START, TOP_END, BOTTOM_START, BOTTOM_END
    }

    public WatermarkTransformation(int margin, Bitmap wmBitmap, Gravity gravity) {
        mMargin = margin;
        mWmBitmap = wmBitmap;
        mGravity = gravity;
    }

    @Override
    protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                               @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (mWmBitmap != null && !mWmBitmap.isRecycled()) {
            int width = toTransform.getWidth();
            int height = toTransform.getHeight();
            //if width > height get min length
            int watermarkValue = Math.round(Math.min(width, height) / 6.0f);
            int dstWidth, dstHeight;
            float aspect = mWmBitmap.getHeight() * 1.0f / mWmBitmap.getWidth();
            if (width <= height) {
                dstWidth = watermarkValue;
                dstHeight = (int) (aspect * dstWidth);
            } else {
                dstHeight = watermarkValue;
                dstWidth = (int) (dstHeight / aspect);
            }
            Bitmap watermark = Bitmap.createScaledBitmap(mWmBitmap, dstWidth, dstHeight, false);
            Bitmap bitmap = pool.get(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(toTransform, new Matrix(), mWatermarkPaint);
            drawWatermark(canvas, watermark, width, height, dstWidth, dstHeight);
            return bitmap;
        }
        return null;
    }

    private void drawWatermark(Canvas canvas, Bitmap watermark, int width, int height, int dstWidth, int dstHeight) {
        int left = 0, top = 0;
        switch (mGravity) {
            case TOP_START: {
                left = mMargin;
                top = mMargin;
                break;
            }
            case BOTTOM_START: {
                top = height - dstHeight - mMargin;
                left = mMargin;
                break;
            }
            case TOP_END: {
                top = mMargin;
                left = width - dstWidth - mMargin;
                break;
            }
            case BOTTOM_END: {
                top = height - dstHeight - mMargin;
                left = width - dstWidth - mMargin;
                break;
            }
        }
        canvas.drawBitmap(watermark, left, top, mWatermarkPaint);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((ID + mMargin).getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WatermarkTransformation
                && ((WatermarkTransformation) o).mMargin == mMargin
                && ((WatermarkTransformation) o).mGravity == mGravity;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + mMargin * 100 + mGravity.ordinal() * 10 + 1;
    }
}
