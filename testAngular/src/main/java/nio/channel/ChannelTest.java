package nio.channel;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ChannelTest {


    public static void channelTest() throws IOException {
    // System.out.println(System.getProperty(“user.dir”));
        RandomAccessFile aFile = new RandomAccessFile("E:\\IDEA\\FirstProject0911\\testAngular\\src\\main\\resources\\data\\nio-data.text","rw");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);
        int byteRead = inChannel.read(buf);
        while(byteRead != -1){
            System.out.println("Read "+byteRead);
            buf.flip();
            byte[] bytes = new byte[byteRead];
            int index = 0;
            while(buf.hasRemaining()){
                bytes[index] = buf.get();
                index ++;
            }
            System.out.println(new String(bytes,"utf-8"));
            System.out.println(index+" index....");
            buf.clear();
            byteRead = inChannel.read(buf);
        }
        aFile.close();
    }

    public static void main(String[] args) throws IOException {
        channelTest();
    }
}
