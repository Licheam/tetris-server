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
        if (!status.compareAndSet(1, 2)) return;

        Thread tetrisThread = new Thread(tetris);
        tetrisThread.start();

        while (tetrisThread.isAlive()) {

        }
    }
}
