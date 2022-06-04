import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {

    public static int getPlayerStatus(String name) {
        if (!players.containsKey(name)) return 0;
        else return players.get(name).getStatus();
    }

    public static Player getPlayer(String name) {
        if(players.containsKey(name))
            return players.get(name);
        return null;
    }

    public int getStatus() {
        return status.get();
    }

    public String getName() {
        return name;
    }

    public OutputStream getOs() {
        return os;
    }

    public InputStream getIs() {
        return is;
    }

    public void offline() {
        try {
            this.status.set(0);
            players.remove(name);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Hashtable<String, Player> players = new Hashtable<>();
    private final AtomicInteger status;//0 offline, 1 online, 2 playing
    private final String name;
    private final Socket socket;
    private final InputStream is;
    private final OutputStream os;

    public Player(String name, Socket socket) throws IOException {
        this.socket = socket;
        this.name = name;
        this.status = new AtomicInteger(1);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        if (players.containsKey(name)) {
            players.get(name).offline();
        }
        players.put(name, this);
    }

    public void start() {
        try {
            while (getStatus() == 2) {
                Message message = Phraser.blockToReceive(is);

                if (message.attribute == 4) {
                    Phraser.send(os, new Message(0, new String[]{"Not Support"}));
                } else if (message.attribute == 5) {
                    Phraser.send(os, new Message(0, new String[]{"Not Support"}));
                } else if (message.attribute == 6) {
                    if (getStatus() == 2) {
                        Phraser.send(os, new Message(0, new String[]{"In Game"}));
                        continue;
                    }
                    if (message.parameters.length != 2 || message.parameters[0].length() != 6 || message.parameters[1].length() != 1) {
                        Phraser.send(os, new Message(0, new String[]{"Error Parameters"}));
                        throw new IOException("Error Parameters");
                    }
                    String id = message.parameters[0];

                    if (message.parameters[1].equals("H") || message.parameters[1].equals("J")) {
                        GameRoom room;
                        if (message.parameters[1].equals("H")) {
                            if (CooperativeRoom.getRoom(id) == null) {
                                Phraser.send(os, new Message(8, new String[]{"Create New Room"}));
                                message = Phraser.blockToReceive(is);
                                if (message.attribute == 0) continue;
                                else if (message.attribute != 1) throw new IOException("Error Frame");
                            }
                            room = CooperativeRoom.createRoom(id);
                        } else {
                            if (CompetitiveRoom.getRoom(id) == null) {
                                Phraser.send(os, new Message(8, new String[]{"Create New Room"}));
                                message = Phraser.blockToReceive(is);
                                if (message.attribute == 0) continue;
                                else if (message.attribute != 1) throw new IOException("Error Frame");
                            }
                            room = CompetitiveRoom.createRoom(id);
                        }

                        if (room.getClass() != CooperativeRoom.class) {
                            Phraser.send(os, new Message(0, new String[]{"Already used"}));
                            continue;
                        }
                        if (room.getPlayers().size() == 1) {
                            Phraser.send(os, new Message(7, new String[]{room.getPlayers().get(0).getName()}));
                            message = Phraser.blockToReceive(is);
                            if (message.attribute == 0) continue;
                            else if (message.attribute != 1) throw new IOException("Error Frame");
                        }


                        room.join(this);
                        if (room.getPlayers().size() == 2) {
                            room.getThread().start();
                        } else {
                            while (room.getStatus() == 1) {
                                message = Phraser.receive(is);
                                if (message != null) {
                                    if (message.attribute == 0) {
                                        room.close();
                                        break;
                                    } else throw new IOException("Error Frame");
                                }
                            }
                        }

                        if (room.getStatus() == 0) break;

                        while (room.getThread().isAlive()) {
                            message = Phraser.receive(is);
                            if (message == null) continue;
                            if (message.attribute == 0) {
                                room.close();
                                break;
                            } else if (message.attribute == 10) {
                                if (message.parameters.length != 1) throw new IOException("Error Frame");
                                switch (message.parameters[0]) {
                                    case "u" -> room.operate(1, this);
                                    case "d" -> room.operate(2, this);
                                    case "l" -> room.operate(3, this);
                                    case "r" -> room.operate(4, this);
                                    case "s" -> room.operate(0, this);
                                    default -> throw new IOException("Error Frame");
                                }
                            } else if(message.attribute == 1) {
                                break;
                            }
                        }
                    } else {
                        Phraser.send(os, new Message(0, new String[]{"No such game type"}));
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
