package echo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class EchoClientOutboundHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        ByteBuf inBuf = (ByteBuf) msg;
        inBuf.markReaderIndex(); // 읽기 인덱스 마크

        // 남아 있는 데이터를 다 처리할 때까지 반복
        while (inBuf.readableBytes() > 7) { // 최소 7바이트는 있어야 길이(4) + HDR(3)을 처리할 수 있음
            // 길이 데이터를 4바이트로 읽음
            byte[] lenBytes = new byte[4];
            inBuf.readBytes(lenBytes);

            // HDR를 3바이트로 읽음
            byte[] hdr = new byte[3];
            inBuf.readBytes(hdr);

            // HDR 문자열이 일치하지 않으면 처리 중단
            if (!"HDR".equals(new String(hdr, "UTF-8"))) {
                inBuf.resetReaderIndex(); // 인덱스 초기화
                continue;           // HDR 위치가 안 맞음 -> byte 를 정상적으로 읽지 못함
            }

            // 길이 값을 int로 변환
            int len = Integer.parseInt(new String(lenBytes, "UTF-8").trim()); // 길이 부분을 안전하게 변환

            // 남아있는 데이터가 len만큼 있는지 확인 (없으면 중단)
            if (inBuf.readableBytes() < len) {
                inBuf.resetReaderIndex(); // 인덱스 복구
                continue;
            }

            // 실제 데이터 읽기
            byte[] readBytes = new byte[len];
            inBuf.readBytes(readBytes);

            System.out.println("Client send: " + new String(readBytes, "UTF-8"));

            // 새로운 버퍼에 데이터를 작성하여 전송
            ByteBuf sendBuf = ctx.alloc().buffer(len);
            sendBuf.writeBytes(readBytes);

            ctx.writeAndFlush(sendBuf);

            // 다음 데이터를 읽기 위해 계속
            inBuf.markReaderIndex(); // 다음 메시지를 위해 다시 인덱스 마크
        }
    }



//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//
//        ByteBuf inBuf = (ByteBuf) msg;
//        inBuf.markReaderIndex();
//
//        byte[] lenBytes = new byte[4];
//        inBuf.readBytes(lenBytes);
//        byte[] hdr = new byte[3];
//        inBuf.readBytes(hdr);
//
//        // HDR이랑 일치하지 않으면 종료
//        if (!"HDR".equals(new String(hdr))) {
//            inBuf.resetReaderIndex();
//            return;
//        }
//
//        int len = Integer.parseInt(new String(lenBytes));
//        System.out.println("len: " + len);
//
//        byte[] readBytes = new byte[len];
//        inBuf.readBytes(readBytes);
//
//        System.out.println("Client send: " + new String(readBytes));
//        ByteBuf sendBuf = ctx.alloc().buffer(len);
//        sendBuf.writeBytes(readBytes);
//
//        ctx.writeAndFlush(sendBuf);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
