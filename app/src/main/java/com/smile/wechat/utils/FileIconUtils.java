package com.smile.wechat.utils;

import com.smile.wechat.R;

/**
 * @创建者 CYN
 * @描述 文件图标工具类
 */
public class FileIconUtils {

    /**
     * 根据文件后缀名得到对应的图标资源id
     */
    public static int getFileIconResId(String suffix) {
        if (suffix.equals("doc") || suffix.equals("docx")) {
            return R.mipmap.ic_word;
        } else if (suffix.equals("xls") || suffix.equals("xlsx")) {
            return R.mipmap.ic_excel;
        } else if (suffix.equals("ppt") || suffix.equals("pptx")) {
            return R.mipmap.ic_ppt;
        } else if (suffix.equals("rar") || suffix.equals("zip")) {
            return R.mipmap.ic_zip;
        } else {
            return R.mipmap.ic_file;
        }
    }

}
