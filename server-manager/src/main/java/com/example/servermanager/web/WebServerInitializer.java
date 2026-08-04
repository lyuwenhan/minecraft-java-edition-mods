package com.example.servermanager.web;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;

public final class WebServerInitializer extends ChannelInitializer<SocketChannel> {
	private final SslContext sslContext;
	private final WebServerConfig config;
	private final BackendService backend;

	public WebServerInitializer(
			SslContext sslContext, WebServerConfig config, BackendService backend) {
		this.sslContext = sslContext;
		this.config = config;
		this.backend = backend;
	}

	@Override
	protected void initChannel(SocketChannel channel) {
		ChannelPipeline p = channel.pipeline();
		p.addLast("ssl", sslContext.newHandler(channel.alloc()));
		p.addLast("httpCodec", new HttpServerCodec());
		p.addLast("httpAggregator", new HttpObjectAggregator(1024 * 1024));
		p.addLast("httpHandler", new HttpRequestHandler(config, backend));
		p.addLast(
				"webSocketProtocol",
				new WebSocketServerProtocolHandler(
						WebSocketServerProtocolConfig.newBuilder()
								.websocketPath("/")
								.checkStartsWith(false)
								.allowExtensions(false)
								.maxFramePayloadLength(64 * 1024)
								.build()));
		p.addLast("webSocketFrames", new WebSocketFrameHandler(backend, config.accountStore()));
	}
}
