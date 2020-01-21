package com.guangl.gateway.constants;

/**
 * @ClassName: ConstantsConfig
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: TODO
 * @Date: Created in 2019-12-13 12:04
 * @Version: 1.0.0
 * @param: * @param null
 */
public class ConstantsConfig {

    /**
     * log的配置名称
     */
    public final static String LOG_CONFIG_LOCATION_NAME = "logging.fileLocation";
    /**
     * windows系统log日志存放地址
     */
    public final static String WIN_LOG_PATH = "C:\\usr\\local\\data\\logs";
    /**
     * mac系统log日志存放地址
     */
    public final static String MAC_LOG_PATH = "/usr/local/data/logs";
    /**
     * linux系统log日志存放地址
     */
    public final static String LINUX_LOG_PATH = "/data/logs";
    /**
     * 网关设置deltaTime
     */
    public final static String DELTATIME = "DeltaTm";
    /**
     * USER信息
     */
    public final static String USER_ID = "uid";
    /**
     * SYSID信息
     */
    public final static String SYS_ID = "sid";
    /**
     * USER信息
     */
    public final static String USER_INFO = "user";
    /**
     * nacos.config地址
     */
//    public final static String NACOS_ADDRS = "nacos1.guangl.io:8848,nacos2.guangl.io:8848,nacos3.guangl.io:8848";
    public final static String NACOS_ADDRS = "127.0.0.1:8848";
    /**
     * nacos.config.group
     */
    public final static String NACOS_GROUP = "gateway";
    /**
     * nacos.config配置名
     */
    public final static String NACOS_FILE_NAME = "guangl-gateway.yml";
    /**
     * nacos.config.extend配置名
     */
    public final static String NACOS_EXTEND_FILE_NAME = "guangl-gateway-extend.yml";
    /**
     * nacos.config热更新
     */
    public final static boolean NACOS_REFRESH = true;
    /**
     * tk.mybatis扫描包址
     */
    public final static String TK_MYBATIS_SCAN_ADDR = "com.guangl.gateway.dao";
    /**
     * 是否在白名单中的头信息KEY
     */
    public final static String WHITE_LIST_THROUGH = "wlt";
    /**
     * 全局是
     */
    public final static String YES = "y";
    /**
     * 全局否
     */
    public final static String NO = "n";
    /**
     * 真实IP的KEY
     */
    public final static String REAL_IP = "real-ip";
    /**
     * 存在于无需过滤接口列表中KEY
     */
    public final static String THROUGH_API = "ta";
    /**
     * 网关设置的头信息
     */
    public final static String AUTHORIZATION = "Access-Token";
    /**
     * 前端返回需要相关用户相关页面的渲染权限
     */
    public final static String FRONT_RENDER = "Web-Path";
    /**
     * 是否需要的标志位
     */
    public final static String WANTED_FRONT_RENDER = "yes";
    /**
     * 获取POST请求BODY时默认WebFlux存储KEY
     */
    public static final String CACHED_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    /**
     * Salt盐值
     */
    public static final String GATEWAY_SYS_SALT = "Salt";
    /**
     * 是否有该API权限
     */
    public static final String API_AUTHORIZATION = "api";
    /**
     * 请求头中的timestamp
     */
    public static final String HEADER_TIMESTAMP = "Timestamp";
    /**
     * SYSTEM的Salt盐值KEY
     */
    public static final String SYSTEM_SALT_KEY = "tfboys";
    /**
     * SIGN签名的KEY
     */
    public static final String SYSTEM_SIGN_KEY = "sign";
    /**
     * API-PATH的KEY
     */
    public static final String API_PATH_KEY = "ak";
    /**
     * 请求体中Timestamp，用作sign校验
     */
    public final static String REQUEST_BODY_TIMESTAMP = "Rocketgirl101";
}
