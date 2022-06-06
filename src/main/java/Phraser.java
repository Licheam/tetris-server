import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Phraser {
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String ByteArrayToHex(Byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static int bytesToInt(byte[] b) {
        if (b.length < 4) return 0;
        return ((b[3] & 0xFF) << 24) |
                ((b[2] & 0xFF) << 16) |
                ((b[1] & 0xFF) << 8) |
                ((b[0] & 0xFF));
    }

    private static Byte[] intToBytes(int x) {
        return new Byte[]{
                (byte) x,
                (byte) (x >>> 8),
                (byte) (x >>> 16),
                (byte) (x >>> 24)};
    }

    public static Message blockToReceive(InputStream is) throws IOException {
        Message message;
        while ((message = receive(is)) == null) ;
        return message;
    }

    public static Message receive(InputStream is) throws IOException {
        if(is.available() == 0) return null;
        byte[] buff = new byte[4];
        int sz = is.read(buff);
        if (sz == 0) {
            System.out.println("Receive nothing!");
            return null;
        } else if (sz < 4) throw new IOException("Frame Head Damage");
        int frameLen = bytesToInt(buff);

        byte[] frameBytes = is.readNBytes(frameLen);
        if (frameBytes.length != frameLen) {
            throw new IOException("Frame Body Losses");
        }

        ByteBuffer frameBuff = ByteBuffer.wrap(frameBytes);
        frameBuff.order(ByteOrder.LITTLE_ENDIAN);

        int attr = frameBuff.getInt();

        int cnt = frameBuff.getInt();

        String[] paras = new String[cnt];

        for (int i = 0; i < cnt; i++) {
            int len = frameBuff.getInt();
            byte[] para = new byte[len];
            frameBuff.get(para);
            paras[i] = new String(para, StandardCharsets.UTF_8);
        }

        System.out.println("Receive a message:");
        System.out.println("Attr: " + attr);
        for (String para : paras) {
            System.out.println(para);
        }
        return new Message(attr, paras);
    }

    public static void send(OutputStream os, Message message) throws IOException {
        int totalLength = 0;
        for (int i = 0; i < message.parameters.length; i++)
            totalLength += message.parameters[i].length();
        int totalBytes = 4 + 4 + message.parameters.length * 4 + totalLength;

        ArrayList<Byte> buff = new ArrayList<>();
        buff.addAll(Arrays.asList(intToBytes(totalBytes)));
        buff.addAll(Arrays.asList(intToBytes(message.attribute)));
        buff.addAll(Arrays.asList(intToBytes(message.parameters.length)));
        for (int i = 0; i < message.parameters.length; i++) {
            assert message.parameters[i].length() == Arrays.asList(ArrayUtils.toObject(message.parameters[i].getBytes(StandardCharsets.UTF_8))).size();
//            System.out.println("Sendding message with length " + message.parameters[i].length() + " aka " + ByteArrayToHex(intToBytes(message.parameters[i].length())));
            System.out.println("'" + message.parameters[i] + "'");
            buff.addAll(Arrays.asList(intToBytes(message.parameters[i].length())));
            buff.addAll(Arrays.asList(ArrayUtils.toObject(message.parameters[i].getBytes(StandardCharsets.UTF_8))));
        }
        assert buff.size() == totalBytes + 4;
        System.out.println("Sending a message:");
        System.out.println("Attr: " + message.attribute);
        for (String para : message.parameters) {
            System.out.println(para);
        }
        os.write(ArrayUtils.toPrimitive(buff.toArray(new Byte[0])));
    }
}