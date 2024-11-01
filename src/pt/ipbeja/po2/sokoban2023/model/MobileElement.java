package pt.ipbeja.po2.sokoban2023.model;

import java.util.Objects;

/**
 * Abstract class that defines all mobile elements
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 */
public abstract class MobileElement {
    int line;
    int col;
    Position position;

    /**
     * constructor of a mobile element
     * @param pos current position of the element
     */
    public MobileElement(Position pos) {
        line = pos.line();
        col = pos.col();
        position = pos;
    }

    /**
     * function that returns current position
     * @return current mobile element position
     */
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MobileElement that)) return false;
        return line == that.line && col == that.col && position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, col, position);
    }
}
