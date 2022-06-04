import java.io.IOException;

public class CompetitiveRoom extends GameRoom {

    public static GameRoom createRoom(String id) {
        if (getRoom(id) == null) return new CompetitiveRoom(id);
        else return getRoom(id);
    }

    private final Tetris[] tetrises;

    @Override
    public boolean operate(int op, Player player) {
        return false;
    }

    private CompetitiveRoom(String id) {
        super(id);
        this.tetrises = new Tetris[2];
        this.tetrises[0] = new Tetris(20, 10, 1);
        this.tetrises[1] = new Tetris(20, 10, 1);
    }

    @Override
    public void run() {
        try {
            if (!status.compareAndSet(1, 2)) return;

            Thread[] tetrisThreads = new Thread[2];
            for (int i = 0; i < 2; i++) {
                tetrisThreads[i] = new Thread(tetrises[i]);
                tetrisThreads[i].start();
            }

            for (Player player : getPlayers()) {
                Phraser.send(player.getOs(), new Message(17, new String[]{"Error Parameters", "H"}));
            }
            Frame[] frames = new Frame[2];
            while ((tetrisThreads[0].isAlive() || tetrisThreads[1].isAlive()) && getStatus() == 2) {
                for (int i = 0; i < 2; i++) {
                    if (tetrises[i].hasFrame()) {
                        frames[i] = tetrises[i].getFrame();
                        if (frames[0] == null || frames[1] == null) continue;
                        if (frames[i].state == Tetris.fail) {
                            Phraser.send(getPlayers().get(i).getOs(), new Message(12, new String[]{Integer.toString(frames[i].score), Integer.toString(frames[i ^ 1].score)}));
                            if (frames[i ^ 1].state == Tetris.fail) {
                                close();
                                return;
                            }
                        }
                        for (int j = 0; j < 2; j++) {
                            if (frames[j].state == Tetris.ongoing) {
                                Phraser.send(getPlayers().get(j).getOs(), new Message(9, new String[]{frames[j].scene, frames[j ^ 1].scene, frames[j].nexts + frames[j ^ 1].nexts, Integer.toString(frames[j].score), Integer.toString(frames[j].score)}));
                            }
                        }
                    }
                }
            }
            for (Player player : getPlayers()) {
                try {
                    Phraser.send(player.getOs(), new Message(11, new String[]{"Game is forced closed"}));
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
