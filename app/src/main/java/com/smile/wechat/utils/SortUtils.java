package com.smile.wechat.utils;

import com.smile.wechat.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @创建者 CYN
 * @描述 根据拼音进行排序整理的工具类
 */
public class SortUtils {

    /**
     * 对联系人集合进行排序
     *
     * @param list
     */
    public static void sortContacts(List<Contact> list) {
        Collections.sort(list);// 排序后由于#号排在上面，故得把#号的部分集合移到集合的最下面

        List<Contact> specialList = new ArrayList<Contact>();

        for (int i = 0; i < list.size(); i++) {
            // 将属于#号的集合分离开来
            if (list.get(i).getPinyin().equals("#")) {
                specialList.add(list.get(i));
            }
        }

        if (specialList.size() != 0) {
            list.removeAll(specialList);// 先移出掉顶部的#号部分
            list.addAll(list.size(), specialList);// 将#号的集合添加到集合底部
        }

    }

}
