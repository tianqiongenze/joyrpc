package io.joyrpc.transport.netty4.binder;

/*-
 * #%L
 * joyrpc
 * %%
 * Copyright (C) 2019 joyrpc.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.joyrpc.extension.Extensible;
import io.joyrpc.transport.channel.Channel;
import io.joyrpc.transport.channel.ChannelHandlerChain;
import io.joyrpc.transport.codec.Codec;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import java.util.function.BiFunction;

/**
 * 处理器绑定
 */
@Extensible("handlerBinder")
public interface HandlerBinder {

    String HANDLER = "handler";
    String DECODER = "decoder";
    String ENCODER = "encoder";
    String CODEC = "codec";
    String HTTP_AGGREGATOR = "http-aggregator";
    String HTTP_RESPONSE_CONVERTER = "http-response-converter";

    /**
     * 返回业务处理函数元数据数组
     *
     * @return 业务处理函数元数据数组
     */
    HandlerMeta<ChannelHandlerChain>[] handlers();

    /**
     * 返回解码器元数据数组
     *
     * @return 解码器元数据数组
     */
    HandlerMeta<Codec>[] decoders();

    /**
     * 返回编码器元数据数组
     *
     * @return 编码器元数据数组
     */
    HandlerMeta<Codec>[] encoders();

    /**
     * 绑定处理链
     *
     * @param pipeline 管道
     * @param codec    编解码
     * @param chain    处理链
     * @param channel  连接通道
     */
    default void bind(final ChannelPipeline pipeline, final Codec codec, final ChannelHandlerChain chain, final Channel channel) {
        if (codec != null) {
            //解码器
            for (HandlerMeta<Codec> meta : decoders()) {
                pipeline.addLast(meta.name, meta.function.apply(codec, channel));
            }
            //编码器
            for (HandlerMeta<Codec> meta : encoders()) {
                pipeline.addLast(meta.name, meta.function.apply(codec, channel));
            }
        }
        //处理链
        if (chain != null) {
            for (HandlerMeta<ChannelHandlerChain> meta : handlers()) {
                pipeline.addLast(meta.name, meta.function.apply(chain, channel));
            }
        }
    }

    /**
     * 处理器元数据
     *
     * @param <T>
     */
    class HandlerMeta<T> {
        /**
         * 名称
         */
        protected String name;
        /**
         * 函数
         */
        protected BiFunction<T, Channel, ChannelHandler> function;

        /**
         * 构造函数
         *
         * @param name     名称
         * @param function 函数
         */
        public HandlerMeta(String name, BiFunction<T, Channel, ChannelHandler> function) {
            this.name = name;
            this.function = function;
        }

        public String getName() {
            return name;
        }

        public BiFunction<T, Channel, ChannelHandler> getFunction() {
            return function;
        }
    }

    /**
     * 处理链元数据
     */
    class HandlerChainMeta extends HandlerMeta<ChannelHandlerChain> {

        /**
         * 构造函数
         *
         * @param name     名称
         * @param function 函数
         */
        public HandlerChainMeta(String name, BiFunction<ChannelHandlerChain, Channel, ChannelHandler> function) {
            super(name, function);
        }
    }

    /**
     * 编解码元数据
     */
    class CodecMeta extends HandlerMeta<Codec> {

        /**
         * 构造函数
         *
         * @param name     名称
         * @param function 函数
         */
        public CodecMeta(String name, BiFunction<Codec, Channel, ChannelHandler> function) {
            super(name, function);
        }
    }

}
