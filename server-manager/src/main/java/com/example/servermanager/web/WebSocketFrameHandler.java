package com.example.servermanager.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

public final class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
	private final BackendService backend;
	private final AccountStore accountStore;

	public WebSocketFrameHandler(BackendService backend, AccountStore accountStore) {
		this.backend = backend;
		this.accountStore = accountStore;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
		if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
			SessionRegistry.Session session = SessionRegistry.forChannel(ctx.channel());
			if (!SessionRegistry.isAuthenticated(session)
					|| !accountStore.isSessionAccountValid(session.username())) {
				ctx.writeAndFlush(new CloseWebSocketFrame(1008, "Unauthorized"))
						.addListener(ChannelFutureListener.CLOSE);
				return;
			}
			backend.sendInitial(ctx.channel());
		}
		super.userEventTriggered(ctx, event);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
		SessionRegistry.Session session = SessionRegistry.forChannel(ctx.channel());
		if (!SessionRegistry.isAuthenticated(session)
				|| !accountStore.isSessionAccountValid(session.username())) {
			ctx.writeAndFlush(new CloseWebSocketFrame(1008, "Unauthorized"))
					.addListener(ChannelFutureListener.CLOSE);
			return;
		}
		if (frame instanceof TextWebSocketFrame text) {
			try {
				JsonObject packet = JsonParser.parseString(text.text()).getAsJsonObject();
				backend.handleWebSocket(ctx.channel(), packet);
			} catch (Exception ignored) {
			}
		} else if (frame instanceof BinaryWebSocketFrame) {
			ctx.writeAndFlush(new CloseWebSocketFrame(1003, "Binary frames are not supported"));
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		SessionRegistry.detach(ctx.channel());
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}
