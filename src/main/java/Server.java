import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6709);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected");
                Thread serverThread = new Thread(new LoginThread(socket));
                serverThread.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
