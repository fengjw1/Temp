package com.ktc.ecuador.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

/**
 * @author longzj
 */
public class GlideModuleConfig extends AppGlideModule {
    private int memorySize = (int) (Runtime.getRuntime().maxMemory()) / 10;

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {

        int diskSize = 1024 * 1024 * 100;
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskSize));
        // 默认内存和图片池大小
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize));
        builder.setMemoryCache(new LruResourceCache(memorySize));
        builder.setBitmapPool(new LruBitmapPool(memorySize));

    }


    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

    }
}
