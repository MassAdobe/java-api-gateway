package com.guangl.gateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

public class Md5Util {

    private final static Logger logger = LoggerFactory.getLogger(Md5Util.class);

    public static String encode(String str) {
        try {
            str = DigestUtils.md5DigestAsHex(str.getBytes());
        } catch (Exception e) {
            logger.error("ENCODE ERROR:{}", e);

        }
        return str;
    }

    /**
     * 带盐值加密
     *
     * @param str  待加密字符串
     * @param salt 盐值
     */
    public static String encode(String str, String salt) {
        return encode(str + salt);
    }

    public static void main(String[] args) {
        String a = "http://%s/nacos/v1/ns/instance?port=%s&metadata=urls=%s";
        String format = String.format(a, "localhost", "22", "{*/xyz*:{*abc*:*ABC*}!*/123*:{*456*:*789*}}");
        System.out.println(format);
    }

}
