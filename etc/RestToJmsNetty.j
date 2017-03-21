package com.cheeray.rest;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.CharsetUtil;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;

import com.cheeray.rest.config.JmsConfig;
import com.cheeray.rest.config.RestConfig;

/**
 * Publish rest data to JMS queue.
 * 
 * @author Chengwei.Yan
 * 
 */
public class RestToJmsNetty {

	public RestToJmsNetty(RestConfig rest, JmsConfig outbound)
			throws CertificateException, SSLException, InterruptedException {
		final SslContext sslCtx;
		final boolean ssl = rest.getScheme().equalsIgnoreCase("https");
		if (ssl) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(),
					ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new HttpSnoopServerInitializer(sslCtx));

			Channel ch = b.bind(rest.getPort()).sync().channel();

			System.err.println("Open your web browser and navigate to "
					+ (ssl ? "https" : "http") + "://127.0.0.1:"
					+ rest.getPort() + '/');

			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public class HttpSnoopServerInitializer extends
			ChannelInitializer<SocketChannel> {

		private final SslContext sslCtx;

		public HttpSnoopServerInitializer(SslContext sslCtx) {
			this.sslCtx = sslCtx;
		}

		@Override
		public void initChannel(SocketChannel ch) {
			ChannelPipeline p = ch.pipeline();
			if (sslCtx != null) {
				p.addLast(sslCtx.newHandler(ch.alloc()));
			}
			p.addLast(new HttpRequestDecoder());
			// Uncomment the following line if you don't want to handle
			// HttpChunks.
			// p.addLast(new HttpObjectAggregator(1048576));
			p.addLast(new HttpResponseEncoder());
			// Remove the following line if you don't want automatic content
			// compression.
			// p.addLast(new HttpContentCompressor());
			p.addLast(new HttpSnoopServerHandler());
		}
	}

	public static class HttpSnoopServerHandler extends
			SimpleChannelInboundHandler<Object> {

		private HttpRequest request;
		/** Buffer that stores the response content */
		private final StringBuilder buf = new StringBuilder();

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) {
			ctx.flush();
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
			if (msg instanceof HttpRequest) {
				HttpRequest request = this.request = (HttpRequest) msg;

				HttpMethod m = request.method();
				System.out.println(m.name());
				DecoderResult rs = request.decoderResult();
				System.out.println(rs.toString());
				// if (HttpHeaderUtil.is100ContinueExpected(request)) {
				// send100Continue(ctx);
				// }

				buf.setLength(0);
				buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
				buf.append("===================================\r\n");

				buf.append("VERSION: ").append(request.protocolVersion())
						.append("\r\n");
				buf.append("HOSTNAME: ")
						.append(request.headers().get(HOST, "unknown"))
						.append("\r\n");
				buf.append("REQUEST_URI: ").append(request.uri())
						.append("\r\n\r\n");

				HttpHeaders headers = request.headers();
				if (!headers.isEmpty()) {
					for (Map.Entry<String, String> h : headers) {
						String key = h.getKey();
						String value = h.getValue();
						buf.append("HEADER: ").append(key).append(" = ")
								.append(value).append("\r\n");
					}
					buf.append("\r\n");
				}

				QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
						request.uri());
				Map<String, List<String>> params = queryStringDecoder
						.parameters();
				if (!params.isEmpty()) {
					for (Entry<String, List<String>> p : params.entrySet()) {
						String key = p.getKey();
						List<String> vals = p.getValue();
						for (String val : vals) {
							buf.append("PARAM: ").append(key).append(" = ")
									.append(val).append("\r\n");
						}
					}
					buf.append("\r\n");
				}

				appendDecoderResult(buf, request);
			}

			if (msg instanceof HttpContent) {
				HttpContent httpContent = (HttpContent) msg;

				ByteBuf content = httpContent.content();
				if (content.isReadable()) {
					buf.append("CONTENT: ");
					buf.append(content.toString(CharsetUtil.UTF_8));
					buf.append("\r\n");
					appendDecoderResult(buf, request);
				}

				if (msg instanceof LastHttpContent) {
					buf.append("END OF CONTENT\r\n");

					LastHttpContent trailer = (LastHttpContent) msg;
					if (!trailer.trailingHeaders().isEmpty()) {
						buf.append("\r\n");
						for (CharSequence name : trailer.trailingHeaders()
								.names()) {
							for (CharSequence value : trailer.trailingHeaders()
									.getAll(name)) {
								buf.append("TRAILING HEADER: ");
								buf.append(name).append(" = ").append(value)
										.append("\r\n");
							}
						}
						buf.append("\r\n");
					}

					if (!writeResponse(trailer, ctx)) {
						// If keep-alive is off, close the connection once the
						// content is fully written.
						ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
								ChannelFutureListener.CLOSE);
					}
				}
			}
		}

		private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
			DecoderResult result = o.decoderResult();
			if (result.isSuccess()) {
				return;
			}

			buf.append(".. WITH DECODER FAILURE: ");
			buf.append(result.cause());
			buf.append("\r\n");
		}

		private boolean writeResponse(HttpObject currentObj,
				ChannelHandlerContext ctx) {
			// Decide whether to close the connection or not.
			boolean keepAlive = HttpUtil.isKeepAlive(request);
			// Build the response object.
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
					Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

			response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

			if (keepAlive) {
				// Add 'Content-Length' header only for a keep-alive connection.
				response.headers().setInt(CONTENT_LENGTH,
						response.content().readableBytes());
				// Add keep alive header as per:
				// -
				// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
				response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}

			// Encode the cookie.
			/*
			 * String cookieString = request.headers().getAndConvert(COOKIE); if
			 * (cookieString != null) { Set<Cookie> cookies =
			 * ServerCookieDecoder.decode(cookieString); if (!cookies.isEmpty())
			 * { // Reset the cookies if necessary. for (Cookie cookie: cookies)
			 * { response.headers().add(SET_COOKIE,
			 * ServerCookieEncoder.encode(cookie)); } } } else { // Browser sent
			 * no cookie. Add some. response.headers().add(SET_COOKIE,
			 * ServerCookieEncoder.encode("key1", "value1"));
			 * response.headers().add(SET_COOKIE,
			 * ServerCookieEncoder.encode("key2", "value2")); }
			 */

			// Write the response.
			ctx.write(response);

			return keepAlive;
		}

		// private static void send100Continue(ChannelHandlerContext ctx) {
		// FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
		// CONTINUE);
		// ctx.write(response);
		// }

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
