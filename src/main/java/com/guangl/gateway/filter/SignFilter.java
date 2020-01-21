package com.guangl.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guangl.gateway.constants.ConstantsConfig;
import com.guangl.gateway.enums.ErrorCodeMsg;
import com.guangl.gateway.exception.GatewayException;
import com.guangl.gateway.pojo.RequestUser;
import com.guangl.gateway.utils.FilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * @ClassName: SignFilter
 * @Author: MassAdobe
 * @Email: massadobe8@gmail.com
 * @Description: 验签过滤器，优先级为7
 * @Date: Created in 2019-12-23 16:02
 * @Version: 1.0.0
 * @param: * @param null
 */
@Component
public class SignFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(SignFilter.class);

    private static final String THROUGH_USER_JSON = "{\"guid\":-1,\"sysid\":-1}";

    /**
     * @ClassName: SignFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 过滤器主逻辑
     * @Date: Created in 2019-12-23 16:02
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String throughApi = exchange.getAttribute(ConstantsConfig.THROUGH_API);
        String ans = null;
        ObjectMapper objectMapper = new ObjectMapper();
        if (ConstantsConfig.NO.equals(throughApi)) {// 如果不是非过滤接口，放入相关用户要素
            try {
                // 放入用户基本要素，给后续服务使用
                ans = objectMapper.writeValueAsString(new RequestUser(exchange.getAttribute(ConstantsConfig.USER_ID), exchange.getAttribute(ConstantsConfig.SYS_ID)));
            } catch (IOException e) {
                logger.error(Strings.lenientFormat("【SIGN-FILTER-TOKEN】：用户信息json解析错误！%s", e));
                throw new GatewayException(ErrorCodeMsg.JSON_DECODE_ERROR);
            }
        } else {// 如果是过滤接口，塞入默认的要素
            ans = THROUGH_USER_JSON;
        }
        String salt = exchange.getAttributes().remove(ConstantsConfig.GATEWAY_SYS_SALT).toString();// 获取盐值顺便删除
        ServerHttpRequest host = exchange.getRequest().mutate().header(ConstantsConfig.USER_INFO, ans).build();
        // 如果是POST请求
        if (exchange.getRequest().getMethod() == HttpMethod.POST) {
            Object cachedRequestBodyObject = exchange.getAttributeOrDefault(ConstantsConfig.CACHED_REQUEST_BODY_OBJECT_KEY, null);
            if (null != cachedRequestBodyObject) {
                byte[] body = (byte[]) cachedRequestBodyObject;
                String response = new String(body);
                logger.info(Strings.lenientFormat("【SIGN-FILTER-POST】：BODY：%s", response));
                // 通过当前请求体和盐值校验sign是否正确 校验是否存在xss和sql注入攻击
                if (FilterUtils.postSignCheck(response, salt)) {
                    logger.error(Strings.lenientFormat("【SIGN-FILTER-POST】：Sign校验没有通过，拒绝请求！"));
                    throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
                }
                DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        if (body.length > 0) {
                            return Flux.just(dataBufferFactory.wrap(body));
                        }
                        return Flux.empty();
                    }
                };
                return chain.filter(exchange.mutate().request(decorator).build());
            }
            // 为空，说明已经读过，或者 request body 原本即为空，不做操作，传递到下一个过滤器链
            return chain.filter(exchange);
        } else if (exchange.getRequest().getMethod() == HttpMethod.GET) {// 如果是GET请求
            String path = exchange.getRequest().getURI().toString();
            logger.info(Strings.lenientFormat("【SIGN-FILTER-GET】：BODY：【%s】", path));
            if (FilterUtils.getSignCheck(path, salt)) {
                logger.error(Strings.lenientFormat("【SIGN-FILTER-GET】：Sign校验没有通过，拒绝请求！"));
                throw new GatewayException(ErrorCodeMsg.SIGN_TIME_VALID_ERROR);
            }
        } else {// 如果都不是，直接返回错误
            throw new GatewayException(ErrorCodeMsg.REQUEST_METHOD_FORBIDDEN_ERROR);
        }
        return chain.filter(exchange.mutate().request(host).build());
    }


    /**
     * @ClassName: SignFilter
     * @Author: MassAdobe
     * @Email: massadobe8@gmail.com
     * @Description: 优先级为7
     * @Date: Created in 2019-12-23 16:03
     * @Version: 1.0.0
     * @param: * @param null
     */
    @Override
    public int getOrder() {
        return 7;
    }
}

