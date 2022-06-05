import java.time.Duration;
import java.time.Instant;
import java.util.*;

class blockInfo{
    int[] blockIdxs;
    int idx, x, y, dx, dy, rotate = 0, land = 0, operating = 0;
    char color = '_';
    blockInfo(int[] blockIdxs, int idx, int x, int y, char color){
        this.blockIdxs = blockIdxs;
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = 0;
        this.color = color;
    }
    public void Rotate(){
        if(operating == 1) return;
        rotate++;
        operating = 1;
    }
    public void Down(){
        if(operating == 1) return;
        dy--;
        operating = 1;
    }
    public void Left(){
        if(operating == 1) return;
        dx--;
        operating = 1;
    }
    public void Right(){
        if(operating == 1) return;
        dx++;
        operating = 1;
    }
    public void Land(){
        if(operating == 1) return;
        land = 1;
        operating = 1;
    }
    public void Operated(){
        dx = dy = rotate = land = operating = 0;
    }
}

public class Tetris implements Runnable{
    //    public static final int left = 1, right = 2, down = 3, rotate = 4, land = 5;
    public static final int fail = 0, ongoing = 1;
    private static final int numSymbols = 7;
    public static final String symbols = "IOTZSJL";
    private static final int[][][] blocks = {
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}},//I0
            {{0, 1}, {0, 0}, {0, -1}, {0, -2}},//I1
            {{0, -1}, {0, 0}, {-1, 0}, {-1, -1}},//O
            {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},//T0
            {{1, 0}, {0, 0}, {0, 1}, {0, -1}},//T1
            {{-1, 0}, {0, 0}, {1, 0}, {0, -1}},//T2
            {{-1, 0}, {0, 0}, {0, 1}, {0, -1}},//T3
            {{-1, 0}, {0, 0}, {0, -1}, {1, -1}},//Z0
            {{-1, 0}, {0, 0}, {-1, -1}, {0, 1}},//Z1
            {{-1, -1}, {0, 0}, {0, -1}, {1, 0}},//S0
            {{-1, 1}, {0, 0}, {-1, 0}, {0, -1}},//S1
            {{-1, 0}, {-1, 1}, {-1, -1}, {0, 1}},//J0
            {{-1, 1}, {0, 1}, {1, 1}, {1, 0}},//J1
            {{-1, -1}, {0, 0}, {0, 1}, {0, -1}},//J2
            {{-1, 0}, {0, 0}, {1, 0}, {-1, 1}},//J3
            {{-1, 1}, {-1, 0}, {-1, -1}, {0, -1}},//L0
            {{-1, 1}, {-1, 0}, {0, 1}, {1, 1}},//L1
            {{-1, 1}, {0, 0}, {0, 1}, {0, -1}},//L2
            {{-1, 0}, {0, 0}, {1, 0}, {1, 1}},//L3
    };
    public static int[] I = {0, 1}, O={2}, T={3, 4, 5, 6}, Z={7, 8}, S={9, 10}, J={11, 12, 13, 14}, L={15, 16, 17, 18};
    private int[] X;
    private char[][] game = null;
    private char[] nexts;
    private Integer state = null, framecnt = 0, speed = 2;
    public int n, m, score, members;
    private Instant frameStamp = Instant.now(), timeStamp = Instant.now();
    private HashMap<Integer, blockInfo> movingBlocks;
    private LinkedList<Frame> frames;
    private final Random random = new Random();
    public Tetris(int n, int m, int members){
        this.n = n;
        this.m = m;
        this.members = members;
        game = new char[n][m];
        X = new int[members];
        for(int i = 0, interval = m / members, sx = interval / 2; i < members; i++, sx += interval)
            X[i] = sx;
        for(int i = 0; i < n; i++)
            for(int j = 0; j < m; j++)
                game[i][j] = '_';
        framecnt = 0;
        frames = new LinkedList<>();
        movingBlocks = new HashMap<>();
        nexts = new char[members];
        state = ongoing;
        for(int i = 0; i < members; i++)
            generateNext(i);
    }
    public synchronized void rotate(Integer id){
        movingBlocks.get(id).Rotate();
    }
    public synchronized void left(Integer id){
        movingBlocks.get(id).Left();
    }
    public synchronized void right(Integer id){
        movingBlocks.get(id).Right();
    }
    public synchronized void down(Integer id){
        movingBlocks.get(id).Down();
    }
    public synchronized void land(Integer id){
        movingBlocks.get(id).Land();
    }
    public synchronized Frame getFrame(){
        return frames.poll();
    }
    public synchronized boolean hasFrame(){
        return !frames.isEmpty();
    }


    private void generateNext(int i){
        int idx = random.nextInt(numSymbols);
        nexts[i] = symbols.charAt(idx);
    }
    private void insert(int id, char symbol){
        int idx = 0, x = X[id], y = n + 1;
        int[] block;
        switch (symbol) {
            case 'I' -> block = I;
            case 'O' -> block = O;
            case 'T' -> block = T;
            case 'Z' -> block = Z;
            case 'S' -> block = S;
            case 'J' -> block = J;
            case 'L' -> block = L;
            default -> block = I;
        }
        movingBlocks.put(id, new blockInfo(block, 0, x, y, symbol));
    }
    private void fixBlock(blockInfo block){
        int[][] blk = blocks[block.blockIdxs[block.idx]];
        for(int i = 0; i < 4; i++){
            int x = block.x + blk[i][0], y = block.y + blk[i][1];
            game[y][x] = block.color;
        }
    }
    private Frame drawFrame(){
        char[][] curGame = new char[n][m];
        for(int i = 0; i < n; i++)
            System.arraycopy(game[i], 0, curGame[i], 0, m);
        for(blockInfo block : movingBlocks.values()){
            int[][] blk = blocks[block.blockIdxs[block.idx]];
            for(int i = 0; i < 4; i++){
                int x = block.x + blk[i][0], y = block.y + blk[i][1];
                if(x < 0 || x >= m || y < 0 || y >= n) continue;
                curGame[y][x] = block.color;
            }
        }
        return new Frame(state, curGame, score, Arrays.toString(nexts));
    }

    private void tryLand(blockInfo block){
        int[][] blks = blocks[block.blockIdxs[block.idx]];
        while(true){
            block.dy--;
            boolean isOK = true;
            int nx = block.x + block.dx, ny = block.y + block.dy;
            for(int i = 0; i < 4; i++) {
                int bx = nx + blks[i][0], by = ny + blks[i][1];
                if (bx < 0 || bx >= m || by < 0 || (by < n && game[by][bx] != '_')){
                    isOK = false;
                    break;
                }
            }
            if(!isOK){
                block.dy++;
                return;
            }
        }
    }

    private void clearRows(){
        int cleared = 0;
        for(int i = 0; i < n; i++){
            boolean clear = true;
            for(int j = 0; j < m; j++)
                if(game[i][j] == '_'){
                    clear = false;
                    break;
                }
            if(clear) cleared++;
            else if(cleared != 0){
                for(int j = 0; j < m; j++)
                    game[i - cleared][j] = game[i][j];
                for(int j = 0; j < m; j++)
                    game[i][j] = '_';
            }
        }
        score += cleared * cleared;
    }

    private void fixBlocks(){
        int dy = -1;
        framecnt = 0;
        for (int id = 0; id < members; id++) {
            blockInfo block = movingBlocks.get(id);
            int nx = block.x, ny = block.y + dy, idx = block.idx;
            int[][] blks = blocks[block.blockIdxs[idx]];
            boolean isOK = true, outOfBound = false;
            for (int i = 0; i < 4; i++) {
                int bx = nx + blks[i][0], by = ny + blks[i][1];
                if (bx < 0 || bx >= m || by < 0 || (by < n && game[by][bx] != '_')) {
                    isOK = false;
                }else if(by >= n){
                    outOfBound = true;
                }
            }
            if (isOK) block.y = ny;
            else {
                if (outOfBound){
                    state = fail;
                    return;
                }
                fixBlock(block);
                movingBlocks.remove(id);
            }
            clearRows();
        }
    }

    private void speedUp(){
        if (Duration.between(timeStamp, Instant.now()).toMillis() / 30000 == 1){
            timeStamp = timeStamp.plusSeconds(30);
            speed = speed * 2;
        }
    }
    private void doOperations(){
        for (int id = 0; id < members; id++) {
            if (!movingBlocks.containsKey(id) || movingBlocks.get(id) == null) {
                insert(id, nexts[id]);
                generateNext(id);
            }
            blockInfo block = movingBlocks.get(id);
            int nx = block.x + block.dx, ny = block.y + block.dy, idx = (block.idx + block.rotate) % block.blockIdxs.length;
            if (block.land == 1) tryLand(block);
            int[][] blks = blocks[block.blockIdxs[idx]];
            boolean isOK = true;
            for (int i = 0; i < 4; i++) {
                int bx = nx + blks[i][0], by = ny + blks[i][1];
                if (bx < 0 || bx >= m || by < 0 || (by < n && game[by][bx] != '_')) {
                    isOK = false;
                    break;
                }
            }
            if (isOK) {
                block.x = nx;
                block.y = ny;
                block.idx = idx;
            }
            block.Operated();
        }
    }

    @Override
    public void run() {
        while(state != fail) {
            frameStamp = frameStamp.plusMillis(20);
            framecnt++;
            doOperations();

            if (framecnt == 50 / speed) fixBlocks();
            speedUp();
            frames.add(drawFrame());

            long sleepTime = 20 - Duration.between(frameStamp, Instant.now()).toMillis();
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}