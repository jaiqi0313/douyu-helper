package io.github.hengyunabc.douyuhelper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

@Service
public class HttpServer {
  private String host = "localhost";
  private int port = 67373 % 10000;

  EventLoopGroup bossGroup = new NioEventLoopGroup();
  EventLoopGroup workerGroup = new NioEventLoopGroup();

  HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

  @Autowired
  Manager manager;

  @PostConstruct
  public void init() throws InterruptedException {
    ServerBootstrap b = new ServerBootstrap();
    b.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);

    b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
            pipeline.addLast(new SimpleChannelInboundHandler<HttpObject>() {
              @Override
              public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
              }

              @Override
              protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
                  throws Exception {

                if (msg instanceof FullHttpRequest) {
                  FullHttpRequest request = (FullHttpRequest) msg;
                  URI uri = new URI(request.getUri());
                  String path = uri.getPath();

                  // 处理提交的flv下载url信息
                  if (StringUtils.equals(path, "/douyu/video/url")
                      && request.getMethod().equals(HttpMethod.POST)) {
                    QueryStringDecoder decoder =
                        new QueryStringDecoder(request.content().toString(CharsetUtil.UTF_8), false);
                    List<String> urlList = decoder.parameters().get("url");
                    if (!urlList.isEmpty()) {
                      manager.addDownloadUrl(urlList.get(0));
                    }
                  }

                  // 处理拉取flv的房间名的请求，如果当前有需要，则返回房间名的列表。如果不需要，则返回空数组
                  if (StringUtils.equals(path, "/douyu/video/rooms")) {
                    List<String> rooms = manager.getNeedDownloadRooms();
                    byte[] bytes = JSON.toJSONBytes(rooms);

                    DefaultFullHttpResponse response =
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(bytes));
                    response.headers().set(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
                    response.headers()
                        .set(Names.CONTENT_LENGTH, response.content().readableBytes());

                    boolean keepAlive = HttpHeaders.isKeepAlive(request);
                    if (!keepAlive) {
                      ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                    } else {
                      response.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
                      ctx.writeAndFlush(response);
                    }
                  }
                }
              }
            });
          }
        });

    b.bind(host, port).sync().channel().closeFuture().addListener(new FutureListener<Object>() {
      @Override
      public void operationComplete(Future<Object> arg0) throws Exception {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
      }
    });
  }

  @PreDestroy
  public void destory() {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }
}
