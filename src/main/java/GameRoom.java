import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameRoom implements Runnable {

    public static GameRoom getRoom(String id) {
        return rooms.getOrDefault(id, null);
    }

    public int getStatus() {
        return status.get();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int join(Player player) {
        if (getStatus() == 2) return -1;
        else {
            players.add(player);
            return players.indexOf(player);
        }
    }

    public abstract boolean operate(int op, Player player);

    public void close() {
        status.set(0);
        rooms.remove(id);
    }

    public synchronized Thread getThread() {
        return roomThread;
    }

    private final Thread roomThread;
    protected static final Hashtable<String, GameRoom> rooms = new Hashtable<>();
    protected AtomicInteger status;//0 offline, 1 waiting, 2 running
    private final String id;
    private final List<Player> players;


    protected GameRoom(String id) {
        this.id = id;
        this.status.set(1);
        this.players = Collections.synchronizedList(new ArrayList<Player>());
        this.roomThread = new Thread(this);
        rooms.put(id, this);
    }


}
