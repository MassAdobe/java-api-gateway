package com.guangl.gateway.filter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
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
 * @ClassName: WhiteListFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 白名单过滤器，优先级 0，从nacos的Config中获取
 * @Date: Created in 2019-12-20 14:21
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
@Data
public class WhiteListFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(WhiteListFilter.class);

    @NacosValue(value = "${config.filter.whiteList}", autoRefreshed = true)
    private List<String> whiteList = new ArrayList<>();

    /**
     * @ClassName: WhiteListFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤主逻辑
     * @Date: Created in 2019-12-20 14:23
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = FilterUtils.getIpAddress(exchange.getRequest());
        logger.info(Strings.lenientFormat("【WHITE-LIST-FILTER】：真实IP：%s", ip));
        if (whiteList.contains(ip)) {// 判断真实IP是否在白名单中
            logger.info(Strings.lenientFormat("【WHITE-LIST-FILTER】：白名单过滤器，该IP: %s 存在于白名单列表中！", ip));
            exchange.getAttributes().put(ConstantsConfig.WHITE_LIST_THROUGH, ConstantsConfig.YES);
        } else {// 不在白名单中
            exchange.getAttributes().put(ConstantsConfig.REAL_IP, ip);
            exchange.getAttributes().put(ConstantsConfig.WHITE_LIST_THROUGH, ConstantsConfig.NO);
        }
        return chain.filter(exchange);
    }

    /**
     * @ClassName: WhiteListFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为0
     * @Date: Created in 2019-12-20 14:23
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
