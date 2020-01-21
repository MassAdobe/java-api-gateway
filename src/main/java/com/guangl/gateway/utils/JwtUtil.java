package com.guangl.gateway.utils;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.JwtConstant;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * 工具类
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private static String encryptJWTKey = "U0GULExyENhspJr34yNjQ1NA";

    private static String verifySecret;

    private static Long refreshPeriod;

    @NacosValue(value = "${jwt.config.encrypt.jwt-key}", autoRefreshed = true)
    public void setEncryptJWTKey(String encryptJWTKey) {
        JwtUtil.encryptJWTKey = encryptJWTKey;
    }

    @NacosValue(value = "${jwt.config.verify.secert}", autoRefreshed = true)
    public void setVerifySecret(String verifySecret) {
        JwtUtil.verifySecret = verifySecret;
    }

    @NacosValue(value = "${jwt.config.encrypt.jwt-refresh-period}", autoRefreshed = true)
    public void setRefreshPeriod(Long refreshPeriod) {
        JwtUtil.refreshPeriod = refreshPeriod;
    }

    /**
     * 校验token是否正确
     */
    public static boolean verify(String token) {
        try {
            // 帐号加JWT私钥解密
            String secret = getClaim(token, JwtConstant.TOKEN_VERIFY_KEY, 2) + Base64Util.decodeThrowsException(encryptJWTKey);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (Exception e) {
            logger.error(Strings.lenientFormat("【JWT-UTILS-VERIFY】：JWTToken认证解密出现UnsupportedEncodingException异常！%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.TOKEN_UNSUPPORT_ENCODE_ERROR);
        }
    }

    /**
     * 获得Token中的信息无需secret解密也能获得
     * 1.int;2.string;3.long;4.array;5.boolean;6.date;7.double;8.list;9.map
     */
    public static Object getClaim(String token, String claim, int type) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            switch (type) {
                case 1:
                    return jwt.getClaim(claim).asInt();
                case 2:
                    return jwt.getClaim(claim).asString();
                case 3:
                    return jwt.getClaim(claim).asLong();
                case 4:
                    return jwt.getClaim(claim).asArray(Object.class);
                case 5:
                    return jwt.getClaim(claim).asBoolean();
                case 6:
                    return jwt.getClaim(claim).asDate();
                case 7:
                    return jwt.getClaim(claim).asDouble();
                case 8:
                    return jwt.getClaim(claim).asList(Object.class);
                case 9:
                    return jwt.getClaim(claim).asMap();
            }
        } catch (JWTDecodeException e) {
            logger.error(Strings.lenientFormat("【JWT-UTILS-getClaim】：解密Token中的公共信息出现JWTDecodeException异常！%s", e.getMessage()));
        }
        throw new GatewayException(ErrorCodeMsg.TOKEN_DECODE_ERROR);
    }

    /**
     * 校验token中的时间，过期返回true
     */
    public static boolean verifyTm(String token) {
        Date date = (Date) getClaim(token, JwtConstant.TOKEN_LOGIN_TM_KEY, 6);
        if ((new Date().getTime() - date.getTime()) / 1000 <= refreshPeriod) // 在合法区间内，可以继续使用
            return false;
        return true;
    }

    /**
     * 生成签名
     */
    public static String sign(User user) {
        try {
            // 帐号加JWT私钥加密
            String secret = verifySecret + Base64Util.decodeThrowsException("U0GULExyENhspJr34yNjQ1NA");
            Algorithm algorithm = Algorithm.HMAC256(secret);
            // 附带account帐号信息
            return JWT.create().withClaim(JwtConstant.TOKEN_VERIFY_KEY, verifySecret).withClaim(JwtConstant.TOKEN_USER_KEY, user.getGUid()).withClaim(JwtConstant.TOKEN_OSS_KEY, user.getSysId()).withClaim(JwtConstant.TOKEN_LOGIN_TM_KEY, new Date()).sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            logger.error(Strings.lenientFormat("【JWT-UTILS-SIGN】：生成JWTTOKEN失败！%s", e.getMessage()));
            throw new GatewayException(ErrorCodeMsg.TOKEN_UNSUPPORT_ENCODE_ERROR);
        }
    }

    public static void main(String[] args) throws Exception {
        User user = new User(1L, 1L, new Date());
        String sign = sign(user);
        System.out.println(sign);
        System.out.println(verify(sign));
        System.out.println(getClaim(sign, JwtConstant.TOKEN_USER_KEY, 3));
        System.out.println(getClaim(sign, JwtConstant.TOKEN_LOGIN_TM_KEY, 6));
        System.out.println(getClaim(sign, JwtConstant.TOKEN_OSS_KEY, 3));
    }

}

@Data
@AllArgsConstructor
class User {
    private Long GUid;
    private Long SysId;
    private Date LgnTm;
}


