package com.coursemanager.utils;

import java.math.BigInteger;

/**
 * @author hhl
 * @date 2024/06/07 19:09
 * @description 生成随机数据
 */
public class RandomData {

    /**
     * 生成六位随机课程编号
     * @return 课程编号
     */
    public static String generateCourseNum() {
        StringBuffer timeStringBuffer = new StringBuffer(String.valueOf(System.currentTimeMillis()).substring(4));
        String courseNum = "";
        while(courseNum.length() != 6) {
            courseNum = new BigInteger(String.valueOf(timeStringBuffer.reverse())).toString(36).toUpperCase();
        }
        return courseNum;
    }
}
