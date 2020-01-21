package com.guangl.gateway.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName: CommonUtils
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 一般工具类
 * @Date: Created in 2020-01-14 16:27
 * @Version: 1.0.0
 * @param: * @param null
 */
public class CommonUtils {

    private final static String DATE_YYYY_MM_DD = "yyyy-MM-dd";
    private final static String DEFAULT_EXPIRE_DT = "1970-01-01";

    /**
     * @ClassName: CommonUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 校验时间是否过期以及时间是否为'1970-01-01'，如果过期返回true，如果没有过期或者为'1970-01-01'则返回false
     * @Date: Created in 2020-01-14 16:16
     * @Version: 1.0.0
     * @param: * @param null
     */
    public static boolean checkExpireDt(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_YYYY_MM_DD);
        if (new Date().before(date) || sdf.format(date).equals(DEFAULT_EXPIRE_DT))
            return false;
        return true;
    }

}
