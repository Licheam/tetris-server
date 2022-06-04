public class Frame {
    public int state, score;
    public String scene, nexts;

    public Frame(int state, char[][] game, int score, String nexts) {
        this.state = state;
        int n = game.length, m = game[0].length;
        this.scene = "";
        for(int i = 0; i < n; i++) {
            this.scene = this.scene + new String(game[i]);
        }
        this.score = score;
        this.nexts = nexts;
    }
}
