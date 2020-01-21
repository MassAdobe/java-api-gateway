package com.guangl.gateway.constants;

/**
 * @ClassName: CmnConstants
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 普通常数
 * @Date: Created in 2019-12-19 15:35
 * @Version: 1.0.0
 * @param: * @param null
 */
public class CmnConstants {
    /**
     * 空信息
     */
    public final static String EMPTY = "";
    // --------------------------------------------------------
    // redis config information
    /**
     * 网关和sso使用的User信息的main-key(hash)
     */
    public final static String REDIS_USER_MAIN_KEY = "users";
    /**
     * 网关和sso使用的User信息的sub-key(hash)
     */
    public final static String REDIS_USER_SUB_KEY = "u_";
    /**
     * 网关和sso使用的Permission信息main-key(hash)
     */
    public final static String REDIS_PERMISSION_MAIN_KEY = "prmss_";
    /**
     * 网关和sso使用的Permission信息sub-key(hash)
     */
    public final static String REDIS_PERMISSION_SUB_KEY = "p_";
    // --------------------------------------------------------

}
