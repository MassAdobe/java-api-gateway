package com.guangl.gateway.filter;

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
 * @ClassName: TokenRefreshFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: Token刷新过滤器，同时校验该用户是否有相关API访问权限和是否前端需要返回相关权限信息，优先级5
 * @Date: Created in 2019-12-23 16:00
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class TokenRefreshFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(TokenRefreshFilter.class);

    /**
     * @ClassName: TokenRefreshFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤器主逻辑
     * @Date: Created in 2019-12-23 16:01
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String throughApi = exchange.getAttribute(ConstantsConfig.THROUGH_API);
        if (ConstantsConfig.NO.equals(throughApi)) {// 如果是非过滤接口，直接透传；不是需要检查TOKEN是否合法
            // 获取用户后查询是否有API访问权限 如果没有权限则直接报错 短路下列逻辑
            if (!Strings.isNullOrEmpty(exchange.getAttribute(ConstantsConfig.API_AUTHORIZATION)) && ConstantsConfig.NO.equals(exchange.getAttributes().remove(ConstantsConfig.API_AUTHORIZATION).toString())) {
                logger.error(Strings.lenientFormat("【TOKEN-REFRESH-FILTER】：用户：%d 没有访问接口权限！", exchange.getAttribute(ConstantsConfig.USER_ID)));
                throw new GatewayException(ErrorCodeMsg.REQUEST_METHOD_FORBIDDEN_ERROR);
            }
        }
        return chain.filter(exchange);
    }

    /**
     * @ClassName: TokenRefreshFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为5
     * @Date: Created in 2019-12-23 16:01
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 5;
    }
}
