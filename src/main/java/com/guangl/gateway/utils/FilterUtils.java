package com.guangl.gateway.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: FilterUtils
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 所有过滤的通用方法
 * @Date: Created in 2019-12-24 14:30
 * @Version: 1.0.0
 * @param: * @param null
 */
public class FilterUtils {

    private final static Logger logger = LoggerFactory.getLogger(FilterUtils.class);

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 获取请求真实IP
     * @Date: Created in 2019-12-24 14:30
     * @Version: 1.0.0
     * @param: * @param null
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip))
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1)
                ip = ip.split(",")[0];
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = headers.getFirst("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = headers.getFirst("WL-Proxy-Client-IP");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = headers.getFirst("HTTP_CLIENT_IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = headers.getFirst("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        return ip;
    }

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 获取请求的host地址，即url
     * @Date: Created in 2019-12-24 17:54
     * @Version: 1.0.0
     * @param: * @param null
     */
    public static String getHostTarget(ServerHttpRequest request) {
        RequestPath path = request.getPath();
        return path.toString();
    }

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: POST请求：校验Sign是否OKAY，如果OKAY返回false，如果不OKAY，返回true
     * @Date: Created in 2020-01-01 14:48
     * @Version: 1.0.0
     * @param: * @param null
     */
    @SuppressWarnings("all")
    public static boolean postSignCheck(String response, String salt) throws GatewayException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder encode = new StringBuilder();
        try {
            Map<String, Object> map = objectMapper.readValue(response, Map.class);
            String sign = map.remove(ConstantsConfig.SYSTEM_SIGN_KEY).toString();
            String timestamp = map.remove(ConstantsConfig.REQUEST_BODY_TIMESTAMP).toString();
            if (Strings.isNullOrEmpty(sign)) {// 如果签名为空，则直接报错
                logger.error(Strings.lenientFormat("【FILTER-UTILS】：Sign校验没有通过，拒绝请求！"));
                throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
            }
            Map<String, Object> sortedMap = sortMapByKey(map);
            // 形成inner={addr=上海市徐汇区上中路, name=张三}&listing=[Aa, Bb, Cc, Dd]&nextId=63276633&testId=43533435&
            if (!map.isEmpty())
                for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                    if (null == entry.getValue())
                        encode.append(entry.getKey() + "=&");
                    else
                        encode.append(entry.getKey() + "=" + objectMapper.writeValueAsString(entry.getValue()).toString().replaceAll(" ", "") + "&");
                }
            // 只剩下参数时校验是否有SQL注入或者XSS攻击问题
            if (checkXssSql(encode.toString()))
                throw new GatewayException(ErrorCodeMsg.PARAM_CONTAINS_XSS_SQL_ERROR);
            // 形成inner={addr=上海市徐汇区上中路, name=张三}&listing=[Aa, Bb, Cc, Dd]&nextId=63276633&testId=43533435&timestamp=213879234873
            encode.append(ConstantsConfig.HEADER_TIMESTAMP + "=" + timestamp);
            // 第一次md5
            String decrpt = Md5Util.encode(encode.toString());
            // 形成 第一次md5值&salt=fewjfiaf*((#W
            decrpt += "&" + ConstantsConfig.SYSTEM_SALT_KEY + "=" + salt;
            // 第二次md5并且比较是否和传入的sign值相同SYSTEM_SIGN_KEY
            if (Strings.isNullOrEmpty(decrpt) || !sign.equals(Md5Util.encode(decrpt))) {// 如果为空或者不相同，则直接返回报错
                throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
            }
            // logger.info(Strings.lenientFormat("【FILTER-UTILS】：sorted-map: %s", encode.toString()));
        } catch (IOException e) {
            logger.error(Strings.lenientFormat("【FILTER-UTILS】：signCheck ERROR: %s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.JSON_DECODE_ERROR);
        }
        return false;
    }

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: GET请求：校验Sign是否OKAY，如果OKAY返回false，如果不OKAY，返回true
     * @Date: Created in 2020-01-01 18:27
     * @Version: 1.0.0
     * @param: * @param null
     */
    @SuppressWarnings("all")
    public static boolean getSignCheck(String path, String salt) throws GatewayException {
        String sign = path.substring(path.indexOf("sign=") + 5);
        String timestamp = path.substring(path.indexOf(ConstantsConfig.REQUEST_BODY_TIMESTAMP) + 14, path.indexOf("&sign="));
        StringBuilder encode = new StringBuilder();
        if (Strings.isNullOrEmpty(sign)) {// 如果签名为空，则直接报错
            logger.error(Strings.lenientFormat("【FILTER-UTILS】：Sign校验没有通过，拒绝请求！"));
            throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
        }
        if (path.contains("&")) // 如果有问号，那么就意味除了sign以外还有参数
            encode.append(path.substring(path.indexOf("?") + 1, path.indexOf("Rocketgirl101=")));
        // 只剩下参数时校验是否有SQL注入或者XSS攻击问题
        if (checkXssSql(encode.toString()))
            throw new GatewayException(ErrorCodeMsg.PARAM_CONTAINS_XSS_SQL_ERROR);
        encode.append(ConstantsConfig.HEADER_TIMESTAMP + "=" + timestamp);
        // 第一次md5
        String decrpt = Md5Util.encode(encode.toString());
        // 形成 第一次md5值&salt=fewjfiaf*((#W
        decrpt += "&" + ConstantsConfig.SYSTEM_SALT_KEY + "=" + salt;
        String abc = Md5Util.encode(decrpt);
        // 第二次md5并且比较是否和传入的sign值相同SYSTEM_SIGN_KEY
        if (Strings.isNullOrEmpty(decrpt) || !sign.equals(Md5Util.encode(decrpt))) {// 如果为空或者不相同，则直接返回报错
            logger.error(Strings.lenientFormat("【FILTER-UTILS】：Sign校验没有通过，拒绝请求！"));
            throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
        }
        return false;
    }

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 校验是否存在SQL注入和XSS攻击
     * @Date: Created in 2020-01-06 15:16
     * @Version: 1.0.0
     * @param: * @param null
     */
    private static boolean checkXssSql(String resOrPath) {
        if (Strings.isNullOrEmpty(resOrPath))
            return false;
        resOrPath = resOrPath.trim().toLowerCase();
        Pattern pattern = null;
        Matcher matcher = null;
        for (String def : DEFENCE_ARR) {
            pattern = Pattern.compile(def);
            matcher = pattern.matcher(resOrPath);
            if (matcher.matches()) {
                logger.error(Strings.lenientFormat("【FILTER-UTILS】：存在XSS或者SQL注入的可能性，拒绝请求！"));
                return true;
            }
        }
        return false;
    }

    private static final String[] DEFENCE_ARR = {
            "<script>(.*?)</script>",
            "src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
            "</script>",
            "<script(.*?)>",
            "eval\\((.*?)\\)",
            "expression\\((.*?)\\)",
            "javascript:",
            "vbscript:",
            "onload(.*?)=",
            "^(.+)\\sand\\s(.+)|(.+)\\sor(.+)\\s$",
            "/(\\%27)|(\\’)|(\\-\\-)|(\\%23)|(#)/ix",
            "/((\\%3D)|(=))[^\\n]*((\\%27)|(\\’)|(\\-\\-)|(\\%3B)|(:))/i",
            "/\\w*((\\%27)|(\\’))((\\%6F)|o|(\\%4F))((\\%72)|r|(\\%52))/ix",
            "/((\\%27)|(\\’))union/ix(\\%27)|(\\’)",
            "/exec(\\s|\\+)+(s|x)p\\w+/ix"
    };

    /**
     * @ClassName: FilterUtils
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: map的排序
     * @Date: Created in 2020-01-06 15:15
     * @Version: 1.0.0
     * @param: * @param null
     */
    private static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        Map<String, Object> sortMap = new TreeMap<>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    // 生成请求
    @SuppressWarnings("all")
    public static void main(String[] args) throws Exception {
//        String salt = "8c4455c62d291ad198cda0788e21b6fe";// 正常请求
        String salt = "ejf())^6ef3";// 非过滤接口请求
        String timestamp = String.valueOf(System.currentTimeMillis());
//        String timestamp = "1579075183722";
        System.out.println("-----POST请求-----");
        String post = "{\"phoneNum\": \"18201750093\",\"password\": \"abcdefg\",\"confirmPwd\": \"abcdefg\",\"mark\": 1}".trim();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        StringBuilder encode = new StringBuilder();
        try {
            Map<String, Object> map = objectMapper.readValue(post, Map.class);
            Map<String, Object> sortedMappp = sortMapByKey(map);
            if (!map.isEmpty()) {
                for (Map.Entry<String, Object> entry : sortedMappp.entrySet()) {
                    if (null == entry.getValue())
                        encode.append(entry.getKey() + "=&");
                    else
                        encode.append(entry.getKey() + "=" + objectMapper.writeValueAsString(entry.getValue()).toString().replaceAll(" ", "") + "&");
                }
            }
            encode.append(ConstantsConfig.HEADER_TIMESTAMP + "=" + timestamp);
            System.out.println("第一次需要加密字符串拼装后：" + encode.toString());
            String decrpt = Md5Util.encode(encode.toString());
            System.out.println("第一次加密：" + decrpt);
            decrpt += "&" + ConstantsConfig.SYSTEM_SALT_KEY + "=" + salt;
            System.out.println("第一次加密后再次拼装：" + decrpt);
            String postAns = post.substring(0, post.length() - 1) + ",\"Rocketgirl101\":" + timestamp + ",\"sign\":\"" + Md5Util.encode(decrpt) + "\"}";
            System.out.println("时间戳：" + Strings.lenientFormat("Rocketgirl101&timestamp：%s", timestamp));
            System.out.println("结束后的结构体：" + postAns);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("-----GET请求-----");
        String get = "http://localhost:7070/gateway-test-service/hello/getWithoutFilter?name=testName&age=23&personId=123456";
        String params = "";
        if (get.contains("?")) {
            params = get.substring(get.indexOf("?") + 1) + "&";
        }
        encode = new StringBuilder();
        encode.append(params);
        encode.append(ConstantsConfig.HEADER_TIMESTAMP + "=" + timestamp);
        System.out.println("第一次需要加密字符串拼装后：" + encode.toString());
        // 第一次md5
        String decrpt = Md5Util.encode(encode.toString());
        System.out.println("第一次加密：" + decrpt);
        // 形成 第一次md5值&salt=fewjfiaf*((#W
        decrpt += "&" + ConstantsConfig.SYSTEM_SALT_KEY + "=" + salt;
        System.out.println("第一次加密后再次拼装：" + decrpt);
        // 第二次md5
        decrpt = Md5Util.encode(decrpt);
        // 拼装结果
        System.out.println("时间戳：" + Strings.lenientFormat("Rocketgirl101&timestamp：%s", timestamp));
        if (get.contains("?")) {
            System.out.println("结束后的请求：" + get + "&Rocketgirl101=" + timestamp + "&sign=" + decrpt);
        } else {
            System.out.println("结束后的请求：" + get + "?Rocketgirl101=" + timestamp + "&sign=" + decrpt);
        }
    }
}

class MapKeyComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}
