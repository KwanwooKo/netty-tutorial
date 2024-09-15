package echo.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class EchoServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        ByteBuf inBuf = (ByteBuf) msg;
        ByteBuf sendBuf = inBuf.copy();
        byte[] readBytes = new byte[inBuf.readableBytes()];
        inBuf.readBytes(readBytes);

        System.out.println("Server send msg: " + new String(readBytes));

        ctx.writeAndFlush(sendBuf);

        // hellohellohellohello
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
