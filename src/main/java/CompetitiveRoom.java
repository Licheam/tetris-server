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
        if(!status.compareAndSet(1,2)) return;

        Thread[] tetrisThreads = new Thread[2];
        for (int i = 0; i < 2; i++) {
            tetrisThreads[i] = new Thread(tetrises[i]);
            tetrisThreads[i].start();
        }




    }
}
