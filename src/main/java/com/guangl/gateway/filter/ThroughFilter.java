package com.guangl.gateway.filter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import com.guangl.gateway.utils.FilterUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ThroughFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 放过不需要过网关的接口，优先级为2
 * @Date: Created in 2019-12-24 14:10
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
@Data
public class ThroughFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(ThroughFilter.class);

    @NacosValue(value = "${config.filter.throughPath}", autoRefreshed = true)
    private List<String> throughList = new ArrayList<>();

    /**
     * @ClassName: ThroughFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤器主逻辑
     * @Date: Created in 2019-12-24 14:11
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (Strings.isNullOrEmpty(exchange.getRequest().getHeaders().getFirst(ConstantsConfig.HEADER_TIMESTAMP))) {
            logger.error(Strings.lenientFormat("【THROUTH-FILTER】：TIMESTAMP为空！"));
            throw new GatewayException(ErrorCodeMsg.TIMESTAMP_NULL_ERROR);
        }
        String hostTarget = FilterUtils.getHostTarget(exchange.getRequest());
        exchange.getAttributes().put(ConstantsConfig.API_PATH_KEY, hostTarget);
        if (throughList.contains(hostTarget)) {// 如果存在于不需要过滤的接口
            exchange.getAttributes().put(ConstantsConfig.THROUGH_API, ConstantsConfig.YES);
        } else {// 如果不存在，则需要过滤
            exchange.getAttributes().put(ConstantsConfig.THROUGH_API, ConstantsConfig.NO);
        }
        return chain.filter(exchange);
    }

    /**
     * @ClassName: ThroughFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为2
     * @Date: Created in 2019-12-24 14:11
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 2;
    }
}
