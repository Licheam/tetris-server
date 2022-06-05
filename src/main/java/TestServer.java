import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TestServer implements Runnable {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6709);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected");
                Thread serverThread = new Thread(new TestServer(socket));
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    OutputStream os;
    InputStream is;

    TestServer(Socket socket) throws IOException {
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
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

class Receiver implements Runnable {

    @Override
    public void run() {

    }
}
