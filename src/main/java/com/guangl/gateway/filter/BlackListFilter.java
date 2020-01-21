package com.guangl.gateway.filter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
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
 * @ClassName: BlackListFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 黑名单过滤器，优先级 1，从Nacos的Config中获取常量设置的黑名单，从Redis中获取动态的黑名单
 * @Date: Created in 2019-12-23 15:52
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
@Data
public class BlackListFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(BlackListFilter.class);

    @NacosValue(value = "${config.filter.blackList}", autoRefreshed = true)
    private List<String> blackList = new ArrayList<>();

    /**
     * @ClassName: BlackListFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤主逻辑
     * @Date: Created in 2019-12-20 14:23
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String whiteListThrough = exchange.getAttributes().remove(ConstantsConfig.WHITE_LIST_THROUGH).toString();
        if (ConstantsConfig.NO.equals(whiteListThrough)) {// 如果不在白名单中，判断是否在黑名单中
            String realIp = exchange.getAttributes().remove(ConstantsConfig.REAL_IP).toString();
            if (blackList.contains(realIp)) {// 如果真实IP在黑名单中
                logger.error(Strings.lenientFormat("【BLACK-LIST-FILTER】：黑名单过滤器，该IP: %s 存在于黑名单中！", realIp));
                throw new GatewayException(ErrorCodeMsg.BLACK_LIST_ERROR);
            }
        }
        return chain.filter(exchange);
    }

    /**
     * @ClassName: BlackListFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为1
     * @Date: Created in 2019-12-20 14:23
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
