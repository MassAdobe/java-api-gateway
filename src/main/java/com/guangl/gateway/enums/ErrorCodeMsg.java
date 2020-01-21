package com.guangl.gateway.enums;

/**
 * @ClassName: ErrorCodeMsg
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: TODO
 * @Date: Created in 2019-12-18 14:00
 * @Version: 1.0.0
 * @param: * @param null
 */
public enum ErrorCodeMsg {

    // 系统级别(0,0->999)
    SUCCESS(0, "成功"),
    UNKNOWN_ERROR(999, "未知错误"),
    SERVER_ERROR(998, "服务错误"),
    PARAM_ERROR(997, "参数错误"),
    PAGE_OR_API_ERROR(996, "页面或接口错误"),
    REDIS_ERROR(994, "缓存错误"),
    REDIS_INCR_ERROR(993, "递增因子必须大于0"),
    USER_BEYOND_EXPIRE_TM_ERROR(992, "用户已经超出了系统给定的使用时间"),
    // 业务错误(gateway:1000->1999)
    UN_LAWFUL_ERROR(1999, "非法"),
    NOT_FOUND_USER_ERROR(1998, "该用户不存在"),
    TOKEN_EMPTY_ERROR(1997, "Token为空"),
    TOKEN_OUT_TIME_ERROR(1996, "Token过期"),
    TOKEN_UNSUPPORT_ENCODE_ERROR(1995, "JWTToken认证解密出现UnsupportedEncodingException异常"),
    TOKEN_DECODE_ERROR(1994, "解密Token中的公共信息出现JWTDecodeException异常"),
    BASE64_DECRYPT_ERROR(1993, "Base64解密错误"),
    SIGN_TIME_VALID_ERROR(1992, "校签或者时间校验错误"),
    BASE64_ENCRYPT_ERROR(1991, "Base64加密错误"),
    JSON_DECODE_ERROR(1990, "JSON解析错误"),
    REQUEST_METHOD_FORBIDDEN_ERROR(1989, "不允许此类请求方法"),
    BLACK_LIST_ERROR(1988, "该用户存在于黑名单中"),
    TIMESTAMP_NULL_ERROR(1987, "timestamp参数为空"),
    PARAM_CONTAINS_XSS_SQL_ERROR(1986, "该请求存在XSS或SQL注入攻击"),
    PERMISSION_GRAP_ERROR(1985, "获取权限信息错误"),


    // 数据级别
    ;

    private int code;
    private String message;

    ErrorCodeMsg(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
