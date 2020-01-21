package com.guangl.gateway.filter;

import com.guangl.gateway.constants.ConstantsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @ClassName: CacheRequestBodyFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 从全局的角度获取POST的请求
 * @Date: Created in 2020-01-01 14:04
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class CacheRequestBodyFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(CacheRequestBodyFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 将 request body 中的内容 copy 一份，记录到 exchange 的一个自定义属性中
        Object cachedRequestBodyObject = exchange.getAttributeOrDefault(ConstantsConfig.CACHED_REQUEST_BODY_OBJECT_KEY, null);
        // 如果已经缓存过，略过
        if (cachedRequestBodyObject != null) {
            return chain.filter(exchange);
        }
        // 如果没有缓存过，获取字节数组存入 exchange 的自定义属性中
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                }).defaultIfEmpty(new byte[0])
                .doOnNext(bytes -> exchange.getAttributes().put(ConstantsConfig.CACHED_REQUEST_BODY_OBJECT_KEY, bytes))
                .then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
