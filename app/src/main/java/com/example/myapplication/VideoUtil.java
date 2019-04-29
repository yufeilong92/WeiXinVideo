package com.example.myapplication;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author LLhon
 * @Project diaoyur_android
 * @Package com.kangoo.util.video
 * @Date 2018/4/11 12:25
 * @description 视频工具类
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class VideoUtil {

    /**
     * 裁剪视频本地目录路径
     */
    public static String getTrimmedVideoDir(Context context, String dirName) {
        String dirPath = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dirPath = context.getExternalCacheDir() + File.separator
                + dirName; // /mnt/sdcard/Android/data/<package name>/files/...
        } else {
            dirPath = context.getCacheDir() + File.separator
                + dirName; // /data/data/<package name>/files/...
        }
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dirPath;
    }


}
