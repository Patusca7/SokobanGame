package pt.ipbeja.po2.sokoban2023.model;

/**
 * Position in the board
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 */
public record Position(int line, int col) {

    @Override
    public String toString() {
        return line + ", " + col;
    }

    /**
     * computes next position of box after being pushed by this keeper
     * fails if keeper position is not next to the box position
     *
     * @param p1 first position
     * @param p2 second position
     * @return box position after being pushed
     */
    public static Position boxNextPositionAfterPush(Position p1, Position p2) {
        final int dLine = p2.line() - p1.line();
        final int dCol = p2.col() - p1.col();
        assert (Math.abs(dLine) == 1) && (dCol == 0) || (dLine == 0) && (Math.abs(dCol) == 1);

        return new Position(p2.line() + dLine, p2.col() + dCol);
    }

    /**
     * Position after moving in direction dir
     *
     * @param dir movement direction
     * @return the position after moving in direction dir
     */
    public Position move(Direction dir) {
        return switch (dir) {
            case UP -> new Position(this.line() - 1, this.col());
            case DOWN -> new Position(this.line() + 1, this.col());
            case LEFT -> new Position(this.line(), this.col() - 1);
            case RIGHT -> new Position(this.line(), this.col() + 1);
        };
    }
}
