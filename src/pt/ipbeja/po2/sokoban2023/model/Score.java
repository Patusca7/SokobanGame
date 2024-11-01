package pt.ipbeja.po2.sokoban2023.model;

/**
 * Class that defines a score for the game
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 */
public class Score {
    private final String levelName;
    private final String playerName;
    private final int moves;

    /**
     * Constructor for an object of the class Score
     *
     * @param levelName  the name of the level the score belongs to
     * @param playerName the name of the player that got the score
     * @param moves      the amount of moves made by the player
     */
    public Score(String levelName, String playerName, int moves) {
        this.levelName = levelName;
        this.playerName = playerName;
        this.moves = moves;
    }

    /**
     * function that returns the steps made by the player
     *
     * @return steps made by the player
     */
    public int getMoves() {
        return moves;
    }

    /**
     * function that returns the name of the level where the score was
     *
     * @return the name of the level where the score was made
     */
    public String getLevelName() {
        return levelName;
    }

    @Override
    public String toString() {
        return playerName + " " + moves;
    }

}
