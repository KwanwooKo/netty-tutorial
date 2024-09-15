package echo.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 받은 메시지를 그대로 다시 클라이언트로 전송
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] readBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(readBytes);

        System.out.println("Server received msg: " + new String(readBytes));

        ByteBuf sendBuf = ctx.alloc().buffer();
        sendBuf.writeBytes(readBytes);

        ctx.writeAndFlush(sendBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close(); // 예외 발생 시 채널 닫기
    }
}
