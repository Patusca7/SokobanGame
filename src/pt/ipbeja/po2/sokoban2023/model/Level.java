package pt.ipbeja.po2.sokoban2023.model;

import java.util.Set;

/**
 * Level data
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */

public record Level(String levelName, Position keeperPosition, Set<Position> boxesPositions, String boardContent) {
    public Level() {
        this("Level1", new Position(3, 5), Set.of(new Position(3, 2), new Position(3, 3)), """
                FFWWWWFF
                FFWFFWFF
                WWWFFWWW
                WFFEEFFW
                WFFFFFFW
                WWWWWWWW""");
    }
}
