package com.smile.wechat.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @创建者 CYN
 * @描述 IO流工具类
 */
public class IOUtils {
    /**
     * 关闭流
     */
    public static boolean close(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                LogUtils.e(e);
            }
        }
        return true;
    }
}
