package echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

//    private String msg = "Hello    Kwanwoo I want to test message";
    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        new Thread(() -> {
            while (true) {
                String msg = null;
                try {
                    msg = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ByteBuf message = ctx.alloc().buffer(4);    // 이 크기보다 더 많이 들어오면 자동으로 확장됨
                message.writeBytes(msg.getBytes());      // 여기서 message 작성 -> channelRead 메서드에서 처리
                ctx.writeAndFlush(message);
            }
        }).start();
        System.out.println("It is not a Thread");       // Thread 로직 이외는 바로 돌아가고, block I/O는 스레드가 대기
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Received: " + in.toString(io.netty.util.CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
