import java.io.IOException;
import java.util.Collections;

public class CooperativeRoom extends GameRoom {

    public static GameRoom createRoom(String id) {
        if (getRoom(id) == null) return new CooperativeRoom(id);
        else return getRoom(id);
    }

    @Override
    public boolean operate(int op, Player player) {
        if (this.getStatus() != 2) return false;
        switch (op) {
            case 0 -> tetris.land(getPlayers().indexOf(player));
            case 1 -> tetris.rotate(getPlayers().indexOf(player));
            case 2 -> tetris.down(getPlayers().indexOf(player));
            case 3 -> tetris.left(getPlayers().indexOf(player));
            case 4 -> tetris.right(getPlayers().indexOf(player));
            default -> {
                return false;
            }
        }
        return true;
    }

    private final Tetris tetris;

    private CooperativeRoom(String id) {
        super(id);
        this.tetris = new Tetris(20, 20, 2);

    }

    @Override
    public void run() {
        try {
            if (!status.compareAndSet(1, 2)) return;

            for (int i = 0; i < 2; i++) {
                Phraser.send(getPlayers().get(i).getOs(), new Message(17, new String[]{getPlayers().get(i ^ 1).getName(), "H"}));
            }

            Thread tetrisThread = new Thread(tetris);
            tetrisThread.start();

            while (tetrisThread.isAlive() && getStatus() == 2) {
                if (tetris.hasFrame()) {
                    Frame frame = tetris.getFrame();
                    if (frame.state == Tetris.fail) {
                        for (Player player : getPlayers())
                            Phraser.send(player.getOs(), new Message(12, new String[]{Integer.toString(frame.score), Integer.toString(frame.score)}));
                        close();
                        return;
                    } else {
                        for (Player player : getPlayers())
                            Phraser.send(player.getOs(), new Message(9, new String[]{frame.scene, frame.nexts, Integer.toString(frame.score), Integer.toString(frame.score)}));
                    }
                }
            }
            for (Player player : getPlayers()) {
                try {
                    if(!tetrisThread.isAlive()){
                        Phraser.send(player.getOs(), new Message(11, new String[]{"Tetris is dead!"}));
                    } else if(getStatus() != 2) {
                        Phraser.send(player.getOs(), new Message(11, new String[]{"Game is closed by outside"}));
                    } else {
                        Phraser.send(player.getOs(), new Message(11, new String[]{"Game is closed by Unknown issues"}));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            close();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }
}
