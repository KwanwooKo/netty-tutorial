package echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;

import java.io.*;

@RequiredArgsConstructor
public class EchoClientInboundHandler extends ChannelInboundHandlerAdapter {

//    private final String msg = "Hello    Kwanwoo I want to test message";
    private final BufferedReader br;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        new Thread(() -> {
            try {
                String msg = br.readLine();
                ByteBuf byteBuf = ctx.alloc().buffer();    // 이 크기보다 더 많이 들어오면 자동으로 확장됨
                byteBuf.writeBytes(msg.getBytes());        // 여기서 message 작성 -> channelRead 메서드에서 처리
                ctx.writeAndFlush(byteBuf);                // 아마 byteBuf 의 크기를 자동지정해서 flush까지 붙여야 하는듯.
                                                           // 실제 전문에서는 길이 이용해서 통신하기 때문에 flush 빼야할 수도?

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//            try {
//                while ((msg = br.readLine()) != null) {
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Client Received: " + in.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

