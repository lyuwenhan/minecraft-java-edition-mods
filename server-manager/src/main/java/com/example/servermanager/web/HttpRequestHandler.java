package com.example.servermanager.web;

import com.google.gson.*;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final String WEB_ROOT = "/web/";
	private static final Gson GSON = new Gson();
	private final WebServerConfig config;
	private final BackendService backend;

	public HttpRequestHandler(WebServerConfig config, BackendService backend) {
		this.config = config;
		this.backend = backend;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
		QueryStringDecoder decoder = new QueryStringDecoder(request.uri(), StandardCharsets.UTF_8);
		String path = decoder.path();
		if (!request.decoderResult().isSuccess()) {
			sendText(
					ctx,
					request,
					HttpResponseStatus.BAD_REQUEST,
					"Bad Request",
					"text/plain; charset=utf-8");
			return;
		}

		if ("/".equals(path) && isWebSocketUpgrade(request)) {
			SessionRegistry.Session session = SessionRegistry.resolve(request);
			if (SessionRegistry.isAuthenticated(session)) {
				SessionRegistry.attach(ctx.channel(), session);
			}
			// Always allow the HTTP upgrade to complete. Authentication is checked
			// after the 101 response, in WebSocketFrameHandler.
			ctx.fireChannelRead(request.retain());
			return;
		}

		if ("/api".equals(path)) {
			if (request.method() != HttpMethod.POST) {
				sendText(
						ctx,
						request,
						HttpResponseStatus.METHOD_NOT_ALLOWED,
						"Method Not Allowed",
						"text/plain; charset=utf-8");
				return;
			}
			handleApi(ctx, request);
			return;
		}

		if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
			sendText(
					ctx,
					request,
					HttpResponseStatus.METHOD_NOT_ALLOWED,
					"Method Not Allowed",
					"text/plain; charset=utf-8");
			return;
		}
		String resourcePath = toResourcePath(path);
		if (resourcePath == null) {
			sendText(
					ctx,
					request,
					HttpResponseStatus.BAD_REQUEST,
					"Bad Request",
					"text/plain; charset=utf-8");
			return;
		}
		byte[] content = loadResource(resourcePath);
		if (content == null) {
			sendText(
					ctx,
					request,
					HttpResponseStatus.NOT_FOUND,
					"Not Found",
					"text/plain; charset=utf-8");
			return;
		}
		sendBytes(
				ctx,
				request,
				HttpResponseStatus.OK,
				content,
				detectContentType(resourcePath),
				request.method() == HttpMethod.HEAD,
				null);
	}

	private static String clientIp(ChannelHandlerContext ctx) {
		if (ctx.channel().remoteAddress() instanceof InetSocketAddress address) {
			return address.getAddress() == null
					? address.getHostString()
					: address.getAddress().getHostAddress();
		}
		return String.valueOf(ctx.channel().remoteAddress());
	}

	private static boolean isWebSocketUpgrade(FullHttpRequest request) {
		return request.method() == HttpMethod.GET
				&& request.headers()
						.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)
				&& HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(
						request.headers().get(HttpHeaderNames.UPGRADE));
	}

	private void handleApi(ChannelHandlerContext ctx, FullHttpRequest request) {
		JsonObject object;
		try {
			object =
					JsonParser.parseString(request.content().toString(CharsetUtil.UTF_8))
							.getAsJsonObject();
		} catch (Exception e) {
			sendJson(
					ctx,
					request,
					HttpResponseStatus.BAD_REQUEST,
					"{\"error\":\"Invalid JSON\"}",
					null);
			return;
		}
		String type = object.has("type") ? object.get("type").getAsString() : "";
		if ("login".equals(type)) {
			String clientIp = clientIp(ctx);
			if (LoginRateLimiter.isBlocked(clientIp)) {
				sendJson(
						ctx,
						request,
						HttpResponseStatus.TOO_MANY_REQUESTS,
						"{\"succeed\":false,\"reason\":\"Too many failed login attempts\"}",
						null);
				return;
			}
			String username = object.has("username") ? object.get("username").getAsString() : "";
			String password = object.has("password") ? object.get("password").getAsString() : "";
			AccountStore.LoginResult login;
			try {
				login = config.accountStore().authenticate(username, password);
			} catch (IOException exception) {
				sendJson(
						ctx,
						request,
						HttpResponseStatus.INTERNAL_SERVER_ERROR,
						"{\"succeed\":false,\"reason\":\"Account database error\"}",
						null);
				return;
			}
			if (!login.succeed()) {
				LoginRateLimiter.recordFailure(clientIp);
				sendJson(
						ctx,
						request,
						HttpResponseStatus.OK,
						GSON.toJson(Map.of("succeed", false, "reason", login.reason())),
						null);
				return;
			}
			LoginRateLimiter.recordSuccess(clientIp);
			SessionRegistry.Session session =
					SessionRegistry.rotateAuthenticated(request, login.username());
			sendJson(ctx, request, HttpResponseStatus.OK, "{\"succeed\":true}", session);
			return;
		}

		SessionRegistry.Session session = SessionRegistry.resolve(request);
		if (!SessionRegistry.isAuthenticated(session)
				|| !config.accountStore().isSessionAccountValid(session.username())) {
			sendJson(
					ctx,
					request,
					HttpResponseStatus.UNAUTHORIZED,
					"{\"error\":\"Unauthorized\"}",
					null);
			return;
		}
		if ("run-command".equals(type)) {
			List<String> commands = new ArrayList<>();
			if (object.has("data") && object.get("data").isJsonArray())
				for (JsonElement e : object.getAsJsonArray("data"))
					if (e.isJsonPrimitive()) commands.add(e.getAsString());
			sendJson(
					ctx,
					request,
					HttpResponseStatus.OK,
					GSON.toJson(backend.runCommands(commands)),
					null);
		} else
			sendJson(
					ctx,
					request,
					HttpResponseStatus.BAD_REQUEST,
					"{\"error\":\"Unknown API request\"}",
					null);
	}

	private static void sendJson(
			ChannelHandlerContext ctx,
			FullHttpRequest request,
			HttpResponseStatus status,
			String json,
			SessionRegistry.Session session) {
		sendBytes(
				ctx,
				request,
				status,
				json.getBytes(CharsetUtil.UTF_8),
				"application/json; charset=utf-8",
				false,
				session);
	}

	private static String toResourcePath(String path) {
		if (path == null || path.indexOf('\\') >= 0 || path.indexOf('\0') >= 0) return null;
		String relative = path.startsWith("/") ? path.substring(1) : path;
		if (relative.isEmpty() || relative.endsWith("/")) relative += "index.html";
		for (String segment : relative.split("/", -1))
			if (segment.equals(".") || segment.equals("..") || segment.isEmpty()) return null;
		return WEB_ROOT + relative;
	}

	private static byte[] loadResource(String path) {
		try (InputStream in = HttpRequestHandler.class.getResourceAsStream(path)) {
			return in == null ? null : in.readAllBytes();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String detectContentType(String path) {
		String t = URLConnection.guessContentTypeFromName(path);
		String lower = path.toLowerCase(Locale.ROOT);
		if (t == null) {
			if (lower.endsWith(".js")) t = "text/javascript";
			else if (lower.endsWith(".css")) t = "text/css";
			else if (lower.endsWith(".json")) t = "application/json";
			else t = "application/octet-stream";
		}
		return t.startsWith("text/") || t.equals("application/json") ? t + "; charset=utf-8" : t;
	}

	private static void sendText(
			ChannelHandlerContext ctx,
			FullHttpRequest request,
			HttpResponseStatus status,
			String text,
			String type) {
		sendBytes(
				ctx,
				request,
				status,
				text.getBytes(CharsetUtil.UTF_8),
				type,
				request.method() == HttpMethod.HEAD,
				null);
	}

	private static void sendBytes(
			ChannelHandlerContext ctx,
			FullHttpRequest request,
			HttpResponseStatus status,
			byte[] content,
			String type,
			boolean head,
			SessionRegistry.Session session) {
		FullHttpResponse response =
				new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1,
						status,
						head ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(content));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.length);
		response.headers().set("X-Content-Type-Options", "nosniff");
		response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store");
		if (session != null) {
			DefaultCookie cookie = new DefaultCookie(SessionRegistry.COOKIE_NAME, session.id());
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			cookie.setMaxAge(SessionRegistry.COOKIE_MAX_AGE_SECONDS);
			cookie.setSecure(true);
			cookie.setSameSite(CookieHeaderNames.SameSite.Strict);
			response.headers()
					.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
		}
		if (HttpUtil.isKeepAlive(request)) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			ctx.writeAndFlush(response);
		} else ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}
