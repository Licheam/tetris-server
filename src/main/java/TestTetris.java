public class TestTetris {
    public static void main(String[] args){
        Tetris tetris = new Tetris(20, 10, 2);
        new Thread(tetris).start();
        while(true){
            try{
                Thread.sleep(20);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(tetris.hasFrame() == false) continue;
            Frame frame = tetris.getFrame();
            if(frame.state == Tetris.fail) break;
            System.out.println("Nexts :" + frame.nexts);
        }
    }
}
