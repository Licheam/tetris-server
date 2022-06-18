/*
Class:  Frame
Writer: qypan
Usage:  A class to store the information about the scene in one frame, the scores of players and the next blocks of each player.
 */
public class Frame {
    public int state, score;
    public String scene, nexts;

    /*
    Function:  Frame
    Usage:     Initialize the class with given parameters.
    Parameters:
        state: if the game is over.
        game:  the scene of the frame.
        score: the total score of the game.
        nexts: the next blocks of each user.
     */
    public Frame(int state, char[][] game, int score, String nexts) {
        this.state = state;
        int n = game.length, m = game[0].length;
        this.scene = "";
        for (char[] chars : game) {
            this.scene = this.scene + new String(chars);
        }
        this.score = score;
        this.nexts = nexts;
    }
}
