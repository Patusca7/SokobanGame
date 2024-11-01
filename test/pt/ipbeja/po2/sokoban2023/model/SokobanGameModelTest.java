package pt.ipbeja.po2.sokoban2023.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SokobanGameModelTest {

    @Test
    void testKeeperMovementToTheRight() {
        Level level = new Level();
        SokobanGameModel sokoban = new SokobanGameModel(level);
        // first a view that does not check
        sokoban.registerView(messageToUI -> {
        });

        assertEquals(new Position(3, 5), sokoban.keeper().getPosition());
        // move right
        sokoban.moveKeeper(Direction.RIGHT);
        // test new position
        assertEquals(new Position(3, 6), sokoban.keeper().getPosition());
    }

    @Test
    void testKeeperMovementOutOfBounds() {
        Level level = new Level();

        SokobanGameModel sokoban = new SokobanGameModel(level);
        int nCols = sokoban.getNCols();
        // first a view that does not check
        sokoban.registerView(messageToUI -> {
        });

        //moves keeper to a position where if he moves right he will be out of bounds
        Position pos = new Position(1, nCols - 1);
        Keeper keeper = sokoban.keeper();
        keeper.moveTo(pos);

        assertEquals(new Position(1, nCols - 1), sokoban.keeper().getPosition());

        // move right
        sokoban.moveKeeper(Direction.RIGHT);

        // test new position
        assertEquals(new Position(1, nCols - 1), sokoban.keeper().getPosition());
    }

    @Test
    void testWinnerPosition() {
        Level level = new Level();

        SokobanGameModel sokoban = new SokobanGameModel(level);

        sokoban.registerView(messageToUI -> {
        });
        //moves keeper to a position where if he moves right all boxes will be stored
        Position pos = new Position(3, 6);
        Keeper keeper = sokoban.keeper();
        keeper.moveTo(pos);

        // moves boxes to a position tha all will be stored whe keeper moves left
        Position pos1 = new Position(3, 2);
        Position pos2 = new Position(3, 5);
        sokoban.moveBoxAt(pos1, pos2);

        sokoban.moveKeeper(Direction.LEFT);
        assertTrue(sokoban.boxInPos(new Position(3, 3))); //Check if position 3,3 has a box
        assertTrue(sokoban.boxInPos(new Position(3, 4)));  //Check if position 3,4 has a box
        assertTrue(sokoban.allBoxesAreStored());
    }

    @Test
    void testReturnedPositionsOnMoveToEmpty() {
        Level level = new Level();
        SokobanGameModel sokoban = new SokobanGameModel(level);

        sokoban.registerView(messageToUI -> {
            List<Position> expectedPositions = List.of(new Position(3, 6), new Position(3, 5));
            assertEquals(expectedPositions, messageToUI.positions());
        });
        sokoban.moveKeeper(Direction.RIGHT);
    }

    @Test
    void testReturnedPositionsOnMoveBox() {
        Level level = new Level();
        SokobanGameModel sokoban = new SokobanGameModel(level);

        // first a view that does not check
        sokoban.registerView(messageToUI -> {
        });

        //move
        sokoban.moveKeeper(Direction.LEFT);
        sokoban.moveKeeper(Direction.LEFT);
        sokoban.moveKeeper(Direction.LEFT);

        // now a view that checks
        sokoban.registerView(messageToUI -> {
            List<Position> expectedPositions =
                    List.of(new Position(3, 3), new Position(4, 3), new Position(2, 3));
            assertEquals(expectedPositions, messageToUI.positions());
        });
        sokoban.moveKeeper(Direction.LEFT);
    }
}

