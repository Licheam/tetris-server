import java.util.Random;

public class TestTetris {
    public static void main(String[] args){
        Tetris tetris = new Tetris(10, 10, 2);
        int cnt = 0;
        Random random = new Random();
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
            cnt++;
            if(cnt != 25) continue;
            cnt = 0;
            System.out.println("Nexts :" + frame.nexts);
            for(int i = 9; i >= 0; i--){
                System.out.println(frame.scene.substring(i*10, i*10+10));
            }
            System.out.println();
            tetris.left(0);
            tetris.left(1);
        }
    }
}
