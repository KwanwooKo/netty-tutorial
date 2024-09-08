package example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


/**
 * Handles a server-side channel.
 * client 요청에 대해 응답하지 않음 (msg 내다 버리는거)
 */
/*
    DiscardServerHandler extends ChannelHandlerAdapter, which is an implementation of ChannelHandler.
    ChannelHandler provides various event handler methods that you can override.
    For now, it is just enough to extend ChannelHandlerAdapter rather than to implement the handler interface by yourself.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    /*
        We override the channelRead() event handler method here.
        This method is called with the received message, whenever new data is received from a client.
        In this example, the type of the received message is ByteBuf.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // Discard the received data silently.
        /*
            To implement the DISCARD protocol, the handler has to ignore the received message.
            ByteBuf is a reference-counted object which has to be released explicitly via the release() method.
            Please keep in mind that it is the handler's responsibility to release any reference-counted object passed to the handler.
            Usually, channelRead() handler method is implemented like the following:
        */
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }

        ((ByteBuf) msg).release(); // (3) 여기서는 received data 가 ByteBuf 타입
    }



    /*
        The exceptionCaught() event handler method is called with a Throwable
        when an exception was raised by Netty due to an I/O error or by a handler implementation
        due to the exception thrown while processing events.
        In most cases, the caught exception should be logged and its associated channel should be closed here,
        although the implementation of this method can be different
        depending on what you want to do to deal with an exceptional situation.
        For example, you might want to send a response message with an error code before closing the connection.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}