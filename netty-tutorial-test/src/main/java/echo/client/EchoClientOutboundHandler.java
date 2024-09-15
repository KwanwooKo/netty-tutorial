package echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class EchoClientOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        ByteBuf inBuf = (ByteBuf) msg;
        byte[] readBytes = new byte[inBuf.readableBytes()];
        inBuf.readBytes(readBytes);

        System.out.println("Client send: " + new String(readBytes));
        ByteBuf sendBuf = ctx.alloc().buffer();
        sendBuf.writeBytes(readBytes);

        ctx.writeAndFlush(sendBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
