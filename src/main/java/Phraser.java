import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Phraser {

    private static int bytesToInt(byte[] b) {
        if (b.length < 4) return 0;
        int r = 0;
        for (int i = 0; i < 4; i++)
            r += (int) (b[i]) << (i * 8);
        return r;
    }

    private static Byte[] intToBytes(int x) {
        Byte[] bytes = new Byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (x % (1 << 8));
            x >>= 8;
        }
        return bytes;
    }

    public static Message blockToReceive(InputStream is) throws IOException {
        Message message;
        while ((message = receive(is)) == null) ;
        return message;
    }

    public static Message receive(InputStream is) throws IOException {
        byte[] buff = new byte[4];
        int sz = is.read(buff);
        if (sz == 0) return null;
        else if (sz < 4) throw new IOException("Frame Head Damage");
//        bytesToInt(buff);

        sz = is.read(buff);
        if (sz < 4) throw new IOException("Frame Attribute Damage");
        int attr = bytesToInt(buff);

        sz = is.read(buff);
        if (sz < 4) throw new IOException("Frame Attribute Damage");
        int cnt = bytesToInt(buff);
        String[] paras = new String[cnt];

        for (int i = 0; i < cnt; i++) {
            sz = is.read(buff);
            if (sz < 4) throw new IOException("Frame Parameter Damage");
            int len = bytesToInt(buff);
            byte[] para = new byte[len];
            sz = is.read(para);
            if (sz < len) throw new IOException("Frame Parameter Damage");
            paras[i] = new String(para, StandardCharsets.UTF_8);
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
        for (int i = 0; i < message.parameters.length; i++) {
            buff.addAll(Arrays.asList(intToBytes(message.parameters[i].length())));
            buff.addAll(Arrays.asList(ArrayUtils.toObject(message.parameters[i].getBytes(StandardCharsets.UTF_8))));
        }
        assert buff.size() == totalBytes + 4;
        os.write((byte[]) ArrayUtils.toPrimitive(buff.toArray()));
    }
}