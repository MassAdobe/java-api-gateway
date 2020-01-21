package com.guangl.gateway.filter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.CmnConstants;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.constants.JwtConstant;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import com.guangl.gateway.pojo.UserStruct;
import com.guangl.gateway.utils.CommonUtils;
import com.guangl.gateway.utils.JwtUtil;
import com.guangl.gateway.utils.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * @ClassName: TokenLegalFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 校验Token是否合法的过滤器，优先级为4
 * @Date: Created in 2019-12-23 15:58
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class TokenLegalFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(TokenLegalFilter.class);

    @Autowired
    private RedisClient redis;

    private static String defaultSalt;

    @NacosValue(value = "${jwt.config.salt}", autoRefreshed = true)
    public void setDefaultSalt(String defaultSalt) {
        TokenLegalFilter.defaultSalt = defaultSalt;
    }


    /**
     * @ClassName: TokenLegalFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤器主逻辑
     * @Date: Created in 2019-12-23 15:58
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String throughApi = exchange.getAttribute(ConstantsConfig.THROUGH_API);
        if (ConstantsConfig.NO.equals(throughApi)) {// 如果不是非过滤接口，直接透传；不是需要检查TOKEN是否合法
            String token = exchange.getRequest().getHeaders().getFirst(ConstantsConfig.AUTHORIZATION);// 获取TOKEN
            if (JwtUtil.verify(token)) {
                if (JwtUtil.verifyTm(token)) {
                    logger.error(Strings.lenientFormat("【TOKEN-LEGAL-FILTER】：请求中，TOKEN过期，拒绝请求！"));
                    throw new GatewayException(ErrorCodeMsg.TOKEN_OUT_TIME_ERROR);
                }
                // 把用户信息放入参数中
                Long userId = (Long) JwtUtil.getClaim(token, JwtConstant.TOKEN_USER_KEY, 3);
                Long sysId = (Long) JwtUtil.getClaim(token, JwtConstant.TOKEN_OSS_KEY, 3);
                exchange.getAttributes().put(ConstantsConfig.USER_ID, userId);
                exchange.getAttributes().put(ConstantsConfig.SYS_ID, sysId);
                // 获取用户此次访问的接口名
                String apiPath = exchange.getAttribute(ConstantsConfig.API_PATH_KEY);
                // redis操作 获取SYSID对应的Salt值，获取用户是否存在，获取用户是否有API权限 redis中存放userId,sysid,接口权限
                ObjectMapper objectMapper = new ObjectMapper();
                UserStruct userStruct = null;
                try {
                    if (redis.hHasKey(CmnConstants.REDIS_USER_MAIN_KEY, CmnConstants.REDIS_USER_SUB_KEY + userId))
                        userStruct = objectMapper.readValue(redis.hget(CmnConstants.REDIS_USER_MAIN_KEY, CmnConstants.REDIS_USER_SUB_KEY + userId).toString(), UserStruct.class);
                    else {
                        logger.error(Strings.lenientFormat("【TOKEN-LEGAL-FILTER】：%s", ErrorCodeMsg.NOT_FOUND_USER_ERROR));
                        throw new GatewayException(ErrorCodeMsg.NOT_FOUND_USER_ERROR);
                    }
                } catch (IOException e) {
                    logger.error(Strings.lenientFormat("【TOKEN-LEGAL-FILTER】：%s", ErrorCodeMsg.JSON_DECODE_ERROR));
                    throw new GatewayException(ErrorCodeMsg.JSON_DECODE_ERROR);
                }
                // 查看用户是否已经超过了系统允许使用的时间
                if (CommonUtils.checkExpireDt(userStruct.getDate())) {
                    logger.error(Strings.lenientFormat("【LOGIN-IMPL-SIGN-UP】：用户：%s %s", String.valueOf(userId), ErrorCodeMsg.USER_BEYOND_EXPIRE_TM_ERROR));
                    throw new GatewayException(ErrorCodeMsg.USER_BEYOND_EXPIRE_TM_ERROR);
                }
                // 获取SYSID对应的Salt值，不存在查库，库中没有直接报错，重新登录
                exchange.getAttributes().put(ConstantsConfig.GATEWAY_SYS_SALT, userStruct.getSalt());
                // TODO 获取用户是否有API权限 userId和apiPath作为参数
                exchange.getAttributes().put(ConstantsConfig.API_AUTHORIZATION, ConstantsConfig.YES);
                // exchange.getAttributes().put(ConstantsConfig.API_AUTHORIZATION, ConstantsConfig.NO);
                return chain.filter(exchange);
            }
            logger.error(Strings.lenientFormat("【TOKEN-LEGAL-FILTER】：请求中，TOKEN非法，拒绝请求！"));
            throw new GatewayException(ErrorCodeMsg.UN_LAWFUL_ERROR);
        } else // 是非过滤接口，增加默认的Salt值
            exchange.getAttributes().put(ConstantsConfig.GATEWAY_SYS_SALT, defaultSalt);
        return chain.filter(exchange);
    }

    /**
     * @ClassName: TokenLegalFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为4
     * @Date: Created in 2019-12-23 15:59
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 4;
    }
}
