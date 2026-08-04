package com.example.servermanager.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public final class WebServer {
	private final Logger logger;
	private final WebServerConfig config;
	private final BackendService backend;
	private final AtomicBoolean running = new AtomicBoolean(false);

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel httpsChannel;

	public WebServer(Logger logger, WebServerConfig config, BackendService backend) {
		this.logger = logger;
		this.config = config;
		this.backend = backend;
	}

	public void start() throws Exception {
		if (!running.compareAndSet(false, true)) {
			throw new IllegalStateException("Web server is already running");
		}

		try {
			SslContext sslContext =
					SslContextBuilder.forServer(
									config.certificatePath().toFile(),
									config.privateKeyPath().toFile())
							.build();

			bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
			workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());

			httpsChannel =
					baseBootstrap()
							.childHandler(new WebServerInitializer(sslContext, config, backend))
							.bind(config.host(), config.httpsPort())
							.sync()
							.channel();

			logger.info(
					"Server Manager listening on https://{}:{} using certificate {}",
					config.host(),
					config.httpsPort(),
					config.certificatePath());
		} catch (Exception exception) {
			closeChannels();
			shutdownEventLoops();
			running.set(false);
			throw exception;
		}
	}

	private ServerBootstrap baseBootstrap() {
		return new ServerBootstrap()
				.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
	}

	public void stop() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		closeChannels();
		shutdownEventLoops();
		logger.info("Server Manager web server stopped");
	}

	private void closeChannels() {
		if (httpsChannel != null) {
			httpsChannel.close().awaitUninterruptibly();
			httpsChannel = null;
		}
	}

	private void shutdownEventLoops() {
		if (workerGroup != null) {
			workerGroup.shutdownGracefully().awaitUninterruptibly();
			workerGroup = null;
		}
		if (bossGroup != null) {
			bossGroup.shutdownGracefully().awaitUninterruptibly();
			bossGroup = null;
		}
	}
}
