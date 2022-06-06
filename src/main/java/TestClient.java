import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TestClient implements Runnable {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6709);
            System.out.println("Connected");

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            Thread tc = new Thread(new TestClient(is));
            tc.start();
            Scanner in = new Scanner(System.in);
            while (true) {
                int attr = in.nextInt();
                int cnt = in.nextInt();
                in.nextLine();
                String[] paras = new String[cnt];
                for (int i = 0; i < cnt; i++) {
                    paras[i] = in.nextLine();
                }
                System.out.println("Message sending!");
                Phraser.send(os, new Message(attr, paras));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    InputStream is;

    TestClient(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = Phraser.blockToReceive(is);
//            System.out.println(message.attribute);
//            for (String para : message.parameters) {
//                System.out.println(para);
//            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
