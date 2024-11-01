package pt.ipbeja.po2.sokoban2023.model;

/**
 * A Box is just a mobile element
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public final class Box extends MobileElement {
    /**
     * Creates a Box at pos
     *
     * @param pos initial position for the box
     */
    public Box(Position pos) {
        super(pos);
    }

}
