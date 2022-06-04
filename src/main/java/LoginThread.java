import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class LoginThread implements Runnable {
    private static int tryLogin(String username, String password) {
        if (username.equals("admin")) return 0;
        else return 1;
    }

    private Socket socket;

    public LoginThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            Message message = Phraser.receive(is);
            while (message == null) {
                message = Phraser.receive(is);
            }
            if (message.attribute != 3 || message.parameters.length != 2) {
                Phraser.send(os, new Message(0, new String[]{"Error Frame"}));
                socket.close();
                throw new IOException("Not Login Frame");
            }

            if (tryLogin(message.parameters[0], message.parameters[1]) == 1) {
                if(Player.getPlayerStatus(message.parameters[0]) != 0) {

                }
                Phraser.send(os, new Message(13, new String[]{"Ok to login"}));
                (new Player(message.parameters[0], socket)).start();
            } else {
                Phraser.send(os, new Message(0, new String[]{"Password Authenticate Failed"}));
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
