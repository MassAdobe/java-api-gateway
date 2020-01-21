package com.guangl.gateway.filter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @ClassName: TokenNullableFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: Token是否为空过滤器，并且校验时间是否过期，优先级为3
 * @Date: Created in 2019-12-23 15:55
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class TokenNullableFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(TokenNullableFilter.class);

    private static Long tolerateTm;

    // 可以容忍的时间（毫秒）
    @NacosValue(value = "${jwt.config.encrypt.tolerate-tm}")
    public void setTolerateTm(Long tolerateTm) {
        TokenNullableFilter.tolerateTm = tolerateTm;
    }

    /**
     * @ClassName: TokenNullableFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤主逻辑
     * @Date: Created in 2019-12-23 15:57
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String throughApi = exchange.getAttribute(ConstantsConfig.THROUGH_API);
        if (ConstantsConfig.NO.equals(throughApi)) {// 如果是非过滤接口，直接透传；不是需要查看Token是否存在
            String authorization = exchange.getRequest().getHeaders().getFirst(ConstantsConfig.AUTHORIZATION);
            if (Strings.isNullOrEmpty(authorization)) {// 如果权限TOKEN为空，直接报错
                logger.error(Strings.lenientFormat("【TOKEN-NULLABLE-FILTER】：请求中，TOKEN信息为空，拒绝请求！"));
                throw new GatewayException(ErrorCodeMsg.TOKEN_EMPTY_ERROR);
            }
            String deltaTm = exchange.getRequest().getHeaders().getFirst(ConstantsConfig.DELTATIME);// 获取容忍时间的时间跨度
            if (!Strings.isNullOrEmpty(deltaTm)) {// 如果deltaTm不为空，则校验时间是否正确
                String timeStamp = exchange.getRequest().getHeaders().getFirst(ConstantsConfig.HEADER_TIMESTAMP);// 获取当前时间戳
                // 用请求Header中的时间戳减去(当前时间加or减去偏移时间-矫正时间)的绝对值小于可容忍的时间长度
                if (Math.abs(Long.valueOf(timeStamp) - System.currentTimeMillis() - Long.valueOf(deltaTm)) > getTolerateTm()) {
                    logger.error(Strings.lenientFormat("【TOKEN-NULLABLE-FILTER】：请求中，超过容忍时间，拒绝请求！"));
                    throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
                }
            } else {// 如果deltaTm为空，则直接报错
                logger.error(Strings.lenientFormat("【TOKEN-NULLABLE-FILTER】：请求中，无容忍时间，拒绝请求！"));
                throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
            }
        }
        return chain.filter(exchange);
    }

    /**
     * @ClassName: TokenNullableFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为3
     * @Date: Created in 2019-12-23 15:57
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 3;
    }

    public static Long getTolerateTm() {
        return tolerateTm;
    }
}
