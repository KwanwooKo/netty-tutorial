package echo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.*;

public class EchoClient {

    private final String host;
    private final int port;
//    private final BufferedReader br = new BufferedReader(new FileReader("src/main/resources/message.txt"));
    private final BufferedReader br = new BufferedReader(new FileReader("src/main/resources/message-no-lines.txt"));

    public EchoClient(String host, int port) throws FileNotFoundException {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
//                     .handler(new LoggingHandler(LogLevel.INFO))
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel ch) {
                             ChannelPipeline pipeline = ch.pipeline();
                             // Client, Server 둘 다 OutboundHandler 가 먼저 pipeline에 들어와야돼
                             // 왜...?
                             pipeline.addLast(new EchoClientOutboundHandler());
                             pipeline.addLast(new EchoClientInboundHandler(br));
                         }
                     });

            ChannelFuture f = bootstrap.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoClient("localhost", 8888).start();
    }
}
