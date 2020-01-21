package com.guangl.gateway.config;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ServerHttpResponse包装类，通过BodyHandlerFunction处理响应body
 *
 * @author
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class BodyHandlerServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    /**
     * body 处理拦截器
     */
    private BodyHandlerFunction bodyHandler = initDefaultBodyHandler();

    /**
     * 构造函数
     *
     * @param bodyHandler
     * @param delegate
     */
    public BodyHandlerServerHttpResponseDecorator(
            BodyHandlerFunction bodyHandler, ServerHttpResponse delegate) {
        super(delegate);
        if (bodyHandler != null) {
            this.bodyHandler = bodyHandler;
        }
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        //body 拦截处理器处理响应
        return bodyHandler.apply(getDelegate(), body);
    }

    @Override
    public Mono<Void> writeAndFlushWith(
            Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body).flatMapSequential(p -> p));
    }

    /**
     * 默认body拦截处理器
     *
     * @return
     */
    private BodyHandlerFunction initDefaultBodyHandler() {
        return (resp, body) -> resp.writeWith(body);
    }
}