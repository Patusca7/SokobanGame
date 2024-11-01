package pt.ipbeja.po2.sokoban2023.model;

/**
 * A Keeper is just a mobile element
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
final class Keeper extends MobileElement {

    /**
     * Creates the Keeper at pos
     *
     * @param pos initial position for the Keeper
     */
    public Keeper(Position pos) {
        super(pos);
    }

    /**
     * Moves keeper to new position
     *
     * @param newKeeperPos position to move the keeper to
     */
    public void moveTo(Position newKeeperPos) {
        line = newKeeperPos.line();
        col = newKeeperPos.col();
        position = newKeeperPos;
    }
}
