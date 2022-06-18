import java.time.Duration;
import java.time.Instant;
import java.util.*;

class blockInfo{
    int[] blockIdxs;
    int idx, x, y, dx, dy, rotate = 0, land = 0, operating = 0;
    char color = '_';

    /*
    Function: blockInfo
    Usage:    Initialize the class the block information "blockIdxs", the block statement information "idx",
              the position information "x, y", and the color information "color".
     */
    blockInfo(int[] blockIdxs, int idx, int x, int y, char color){
        this.blockIdxs = blockIdxs;
        this.idx = idx;
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = 0;
        this.color = color;
    }

    /*
    Function: rotate
    Usage:    Set the rotate operation.
     */
    public void rotate(){
        if(operating == 1) return;
        rotate++;
        operating = 1;
    }

    /*
    Function: down
    Usage:    Set the down operation.
     */
    public void down(){
        if(operating == 1) return;
        dy--;
        operating = 1;
    }

    /*
    Function: left
    Usage:    Set the left operation.
     */
    public void left(){
        if(operating == 1) return;
        dx--;
        operating = 1;
    }

    /*
    Function: right
    Usage:    Set the right operation.
     */
    public void right(){
        if(operating == 1) return;
        dx++;
        operating = 1;
    }

    /*
    Function: land
    Usage:    Set the land operation.
     */
    public void land(){
        if(operating == 1) return;
        land = 1;
        operating = 1;
    }

    /*
    Function: operated
    Usage:    Clear the operation information.
     */
    public void operated(){
        dx = dy = rotate = land = operating = 0;
    }
}

public class Tetris implements Runnable{
    // The state parameter
    public static final int fail = 0, ongoing = 1;

    // There are 7 kind of blocks.
    private static final int numSymbols = 7;

    // The symbols for each block.
    public static final String symbols = "IOTZSJL";

    // The concrete information of each block in every statement.
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

    //The statement identifiers of each kind of block.
    public static int[] I = {0, 1}, O={2}, T={3, 4, 5, 6}, Z={7, 8}, S={9, 10}, J={11, 12, 13, 14}, L={15, 16, 17, 18};

    //The initial x-axis information about each user.
    private int[] X;

    //The information about current scene.
    private char[][] game, frame = null;

    //The information about next block for each user.
    private char[] nexts;

    //The scene statement, the count number of ongoing frames, the speed of the movement of the blocks.
    private Integer state = null, framecnt = 0, speed = 2;

    //The scene size, the total score, the number of users.
    public int n, m, score, members;

    //The time stamp for thread speed control.
    private Instant frameStamp = Instant.now(), timeStamp = Instant.now();

    //The moving blocks information for each user.
    private HashMap<Integer, blockInfo> movingBlocks;

    //The frame information generated.
    private LinkedList<Frame> frames;

    //Random number generator.
    private final Random random = new Random();

    /*
    Function: Tetris
    Usage:    Initialize the class with a nxm squares scene and a num of "members" members.
     */
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

    /*
    Function: rotate
    Usage:    Rotate the id's block.
     */
    public synchronized void rotate(Integer id){
        movingBlocks.get(id).rotate();
    }

    /*
    Function: left
    Usage:    Move the id's block one square left.
     */
    public synchronized void left(Integer id){
        movingBlocks.get(id).left();
    }

    /*
    Function: right
    Usage:    Move the id's block one square right.
     */
    public synchronized void right(Integer id){
        movingBlocks.get(id).right();
    }

    /*
    Function: down
    Usage:    Move the id's block one square down.
     */
    public synchronized void down(Integer id){
        movingBlocks.get(id).down();
    }

    /*
    Function: land
    Usage:    Change the information of the id's block for dropping to the bottom.
     */
    public synchronized void land(Integer id){
        movingBlocks.get(id).land();
    }

    /*
    Function: getFrame
    Usage:    The function for outside class to get the fixed frame.
     */
    public synchronized Frame getFrame(){
        return frames.poll();
    }

    /*
    Function: hasFrame
    Usage:    Check if there are frames which didn't send.
     */
    public synchronized boolean hasFrame(){
        return !frames.isEmpty();
    }

    /*
    Function: newFrame
    Usage:    Generate a new frame array to store the static blocks.
     */
    public void newFrame(){
        frame = new char[n][m];
        for(int i = 0; i < n; i++)
            System.arraycopy(game[i], 0, frame[i], 0, m);
    }

    /*
    Function: fixToFrame
    Usage:    Fix a block to the frame.
     */
    public void fixToFrame(blockInfo block){
        int[][] blk = blocks[block.blockIdxs[block.idx]];
        for(int i = 0; i < 4; i++){
            int x = block.x + blk[i][0], y = block.y + blk[i][1];
            if (x < 0 || x >= m || y < 0 || y >= n) continue;
//            System.out.println(x + " " + blk[i][0] + " " + y + " " + blk[i][1]);
            frame[y][x] = block.color;
        }
//        System.out.println();
    }

    /*
    Function: generateNext
    Usage:    Generate the next block randomly for user 'i'.
     */
    private void generateNext(int i){
        int idx = random.nextInt(numSymbols);
        nexts[i] = symbols.charAt(idx);
    }

    /*
    Function: insert
    Usage:    Insert the 'symbol' block to the game and allot it to 'id' user.
     */
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

    /*
    Function: fixBlock
    Usage:    Fix a moving block.
     */
    private void fixBlock(blockInfo block){
        int[][] blk = blocks[block.blockIdxs[block.idx]];
        for(int i = 0; i < 4; i++){
            int x = block.x + blk[i][0], y = block.y + blk[i][1];
//            System.out.println(x + " " + blk[i][0] + " " + y + " " + blk[i][1]);
            game[y][x] = block.color;
        }
//        System.out.println();
    }

    /*
    Function: drawFrame
    Usage:    Convert the scene to a frame for sending it out.
     */
    private Frame drawFrame(boolean useFrame){
        char[][] curGame;
        if(!useFrame) {
            curGame = new char[n][m];
            for (int i = 0; i < n; i++)
                System.arraycopy(game[i], 0, curGame[i], 0, m);
            for (blockInfo block : movingBlocks.values()) {
                int[][] blk = blocks[block.blockIdxs[block.idx]];
                for (int i = 0; i < 4; i++) {
                    int x = block.x + blk[i][0], y = block.y + blk[i][1];
                    if (x < 0 || x >= m || y < 0 || y >= n) continue;
                    curGame[y][x] = block.color;
                }
            }
        }else{
            curGame = frame;
        }
        for(int i = 0, j = n - 1; i < j; i++, j--){
            char[] x =  curGame[i];
            curGame[i] = curGame[j];
            curGame[j] = x;
        }
        return new Frame(state, curGame, score, new String(nexts));
    }

    /*
    Function: tryLand
    Usage:    Try to fix the moving blocks.
     */
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

    /*
    Function: clearRows
    Usage:    Clear the rows all filled with blocks.
     */
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

    /*
    Function: fixBlocks
    Usage:    Check if the block cannot move anymore, fix it if so.
     */
    private synchronized void fixBlocks(){
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
//                    if((bx >= 0 && bx < m && by >= 0 && by < n && game[by][bx] != '_'))
//                        System.out.println(id + " Bound " + bx + " " + by + " " + (bx >= 0 && bx < m && by >= 0 && by < n && frame[by][bx] != '_') + " " + frame[by][bx]);
                    isOK = false;
                }else if(by-dy >= n){
                    outOfBound = true;
                }
            }
            if (isOK) block.y = ny;
            else {
                if (outOfBound){
                    state = fail;
                    continue;
                }
//                System.out.println("Fix " + id + " " + block.x + " " + block.y + " ");
                fixBlock(block);
                movingBlocks.remove(id);
            }
            clearRows();
        }
    }

    /*
    Function: speedUp
    Usage:    Speed up the movement of dropping blocks.
     */
    private void speedUp(){
        if (Duration.between(timeStamp, Instant.now()).toMillis() / 30000 == 1){
            timeStamp = timeStamp.plusSeconds(30);
            speed = speed * 2;
        }
    }

    /*
    Function: checkOutBlocks
    Usage:    Check if each user has a block to manipulate, allot one if not.
     */
    private void checkOutBlocks(){
        for(int id = 0; id < members; id++)
            if (!movingBlocks.containsKey(id) || movingBlocks.get(id) == null) {
                insert(id, nexts[id]);
                generateNext(id);
            }
    }

    /*
    Function: doOperations
    Usage:    Check out the conflicts between the operations of the users and accomplish the operations. Fix the moving blocks to a new frame with the settled blocks.
     */
    private void doOperations(){
        newFrame();
        for (int id = 0; id < members; id++) {
            checkOutBlocks();
            blockInfo block = movingBlocks.get(id);
            int nx = block.x + block.dx, ny = block.y + block.dy, idx = (block.idx + block.rotate) % block.blockIdxs.length;
            if (block.land == 1) tryLand(block);
            int[][] blks = blocks[block.blockIdxs[idx]];
            boolean isOK = true;
            for (int i = 0; i < 4; i++) {
                int bx = nx + blks[i][0], by = ny + blks[i][1];
                if (bx < 0 || bx >= m || by < 0 || (by < n && frame[by][bx] != '_')) {
                    isOK = false;
                    break;
                }
            }
            if (isOK) {
                block.x = nx;
                block.y = ny;
                block.idx = idx;
            }
            fixToFrame(block);
            block.operated();
        }
    }

    /*
    Function: run
    Usage:    The function to start the game process.
              The process of each loop is:
                    1.Do all the operations taken in the past frame time.
                    2.Fix the blocks.
                    3.If the condition is satisfied, speed up the movements.
     */
    @Override
    public void run() {
        while(state != fail) {
            frameStamp = frameStamp.plusMillis(20);
            framecnt++;
            doOperations();

            if (framecnt == 50 / speed) {
                fixBlocks();
                frames.add(drawFrame(false));
            }else frames.add(drawFrame(true));
            checkOutBlocks();
            speedUp();

            long sleepTime = 20 - Duration.between(frameStamp, Instant.now()).toMillis();
            try {
                if(sleepTime > 0)
                    Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}