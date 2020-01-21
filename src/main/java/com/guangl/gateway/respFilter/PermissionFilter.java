package com.guangl.gateway.respFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.CmnConstants;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import com.guangl.gateway.pojo.PermissionStruct;
import com.guangl.gateway.pojo.ResponseStruct;
import com.guangl.gateway.utils.RedisClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @ClassName: PermissionFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 拦截返回请求中的返回体，增加Permission
 * @Date: Created in 2019-12-30 19:53
 * @Version: 1.0.0
 * @param: * @param null
 */

@Component
public class PermissionFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(PermissionFilter.class);

    @Autowired
    private RedisClient redis;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = null;
        String webPath = exchange.getRequest().getHeaders().getFirst(ConstantsConfig.FRONT_RENDER);
        if (!Strings.isNullOrEmpty(exchange.getRequest().getHeaders().getFirst(ConstantsConfig.FRONT_RENDER))) {// 如果Web-Path存在且内容不为空
            decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            // probably should reuse buffers
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            //释放掉内存
                            DataBufferUtils.release(dataBuffer);
                            String ans = new String(content, Charset.forName("UTF-8"));
                            ObjectMapper objectMapper = new ObjectMapper();
                            ResponseStruct responseStruct = null;
                            String guid = exchange.getResponse().getHeaders().getFirst(ConstantsConfig.USER_ID);
                            if (Strings.isNullOrEmpty(webPath) || Strings.isNullOrEmpty(guid)) {
                                logger.error(Strings.lenientFormat("【PERMISSION-FILTER】：%s", ErrorCodeMsg.PERMISSION_GRAP_ERROR));
                                throw new GatewayException(ErrorCodeMsg.PERMISSION_GRAP_ERROR);
                            }
                            Object permission = redis.hget(CmnConstants.REDIS_PERMISSION_MAIN_KEY + guid, CmnConstants.REDIS_PERMISSION_SUB_KEY + webPath);// 从redis中获取permission
                            exchange.getResponse().getHeaders().remove(ConstantsConfig.USER_ID);
                            byte[] rtn = null;
                            try {
                                responseStruct = objectMapper.readValue(ans, ResponseStruct.class);
                                PermissionStruct permissionStruct = objectMapper.readValue(permission.toString(), PermissionStruct.class);
                                responseStruct.setPermission(permissionStruct.getPermissions());
                                rtn = objectMapper.writeValueAsBytes(responseStruct);
                            } catch (IOException e) {
                                logger.error(Strings.lenientFormat("【PERMISSION-FILTER】：%s", ErrorCodeMsg.JSON_DECODE_ERROR));
                                throw new GatewayException(ErrorCodeMsg.JSON_DECODE_ERROR);
                            }
                            return bufferFactory.wrap(rtn);
                        }));
                    }
                    // if body is not a flux. never got there.
                    return super.writeWith(body);
                }
            };
        }
        // replace response with decorator
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

}
