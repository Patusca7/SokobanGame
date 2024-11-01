package pt.ipbeja.po2.sokoban2023.model;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import pt.ipbeja.po2.sokoban2023.images.ImageType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

/**
 * Game board model
 * Contains the game state and reactive behaviour:
 * the interface tells the model what happened and the model tells the interface what to update
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public class SokobanGameModel {
    private final BoardModel board;
    private final Keeper keeper;
    private final Set<Box> boxes;
    private final List<Position> movesList;
    private final String SCORE_FILE_PATH = "Resources/scores.txt";
    private SokobanView view;
    private List<List<Position>> undoGameStates;
    private List<List<Position>> redoGameStates;
    private int redoCounter;

    public SokobanGameModel(Level level) {
        this.board = new BoardModel(level.boardContent());
        this.keeper = new Keeper(level.keeperPosition());
        this.boxes = this.createSetOfBoxes(level.boxesPositions());
        this.view = null;
        this.movesList = new ArrayList<>();
        this.undoGameStates = new ArrayList<>();
        this.redoGameStates = new ArrayList<>();
        this.redoCounter = 0;

        this.makeGameState(level.keeperPosition());
    }

    private Set<Box> createSetOfBoxes(Set<Position> boxesPositions) {
        Set<Box> set = new HashSet<>();
        for (Position pos : boxesPositions) {
            set.add(new Box(pos));
        }
        return set;
    }

    public Keeper keeper() {
        return this.keeper;
    }


    /**
     * Register a view (an observer) and updates it
     *
     * @param view the view to register
     */
    public void registerView(SokobanView view) {
        this.view = view;
    }

    /**
     * @return the position content or WALL it outside the board
     */
    public PositionContent getPosContent(Position pos) {
        if (this.isOutsideBoard(pos)) return PositionContent.WALL;
        else return this.board.getPosContent(pos);
    }

    public boolean isOutsideBoard(Position pos) {
        return pos.line() < 0 || pos.col() < 0 ||
                pos.line() >= board.nLines() ||
                pos.col() >= board.nCols();
    }

    /**
     * @return number of board lines
     */
    public int getNLines() {
        return this.board.nLines();
    }

    /**
     * @return number of board columns
     */
    public int getNCols() {
        return this.board.nCols();
    }

    /**
     * Game ends successfully
     *
     * @return true if all boxes are in the end position (games ends),
     * false otherwise
     */
    public boolean allBoxesAreStored() {
        for (Box box : this.boxes) {
            if (this.getPosContent(box.getPosition()) != PositionContent.END)
                return false;
        }
        return true;
    }

    /**
     * Tries to move keeper in the specified direction
     *
     * @param dir direction of movement
     * @return true if moved, false otherwise
     */
    public boolean moveKeeper(Direction dir) {
        this.redoCounter = 0;
        return this.moveKeeperTo(this.keeper.getPosition().move(dir));
    }

    /**
     * Tries to move keeper to position newPosition
     *
     * @param newPosition target position
     * @return true if moved, false otherwise
     */
    public boolean moveKeeperTo(Position newPosition) {
        Position initialPos = this.keeper.getPosition();
        List<Position> positions = this.moveTo(initialPos, newPosition);

        if (positions.size() > 0) {
            String messageToGUI = "move from " + positions.get(1) + " to " + positions.get(0);
            this.movesList.add(positions.get(0));
            this.makeGameState(positions.get(0));
            this.view.update(new MessageToUI(positions, messageToGUI));
            return true;
        }
        return false;
    }

    /**
     * Move from one position to the other and return the changed positions that need to be updated
     *
     * @param keeperPosition initial keeper position
     * @param newKeeperPos   position where the keeper wants to move
     * @return positions that where changed
     */
    private List<Position> moveTo(Position keeperPosition, Position newKeeperPos) {
        final Position possibleFinalBoxPos = Position.boxNextPositionAfterPush(keeperPosition, newKeeperPos);
        final boolean boxInNewKeeperPos = this.boxInPos(newKeeperPos);
        final boolean boxInPossibleFinalBoxPos = this.boxInPos(possibleFinalBoxPos);
        if (!boxInNewKeeperPos
                &&
                !this.getPosContent(newKeeperPos).equals(PositionContent.WALL)) {
            // move to empty position
            this.keeper.moveTo(newKeeperPos);
            return List.of(newKeeperPos, keeperPosition);
        } else if (boxInNewKeeperPos
                &&
                !this.getPosContent(possibleFinalBoxPos).equals(PositionContent.WALL)
                &&
                !boxInPossibleFinalBoxPos) {
            // move box
            this.keeper.moveTo(newKeeperPos);
            this.moveBoxAt(newKeeperPos, possibleFinalBoxPos);
            return List.of(newKeeperPos, keeperPosition, possibleFinalBoxPos);
        }
        // no movement
        return List.of();
    }

    /**
     * Move box
     *
     * @param start initial box position
     * @param end   final box position
     */
    public void moveBoxAt(Position start, Position end) {
        this.boxes.remove(new Box(start));
        this.boxes.add(new Box(end));
    }

    /**
     * Ask for text for a given position
     *
     * @param p position
     * @return text with contents of position
     */
    public String textForPosition(Position p) {
        return this.keeperInPosText(p) + this.boxInPosText(p) + endInPosText(p) + wallInPosText(p);
    }

    /**
     * Text if keeper in given position
     *
     * @param pos position
     * @return "K " or ""
     */
    private String keeperInPosText(Position pos) {
        return this.keeper.getPosition().equals(pos) ? "K " : "";
    }

    /**
     * Test if the given position has a box
     *
     * @param pos position to test
     * @return true if there is a box in pos
     */
    public boolean boxInPos(Position pos) {
        return this.boxes.contains(new Box(pos));
    }

    /**
     * Text if a box is in the given position
     *
     * @param pos position
     * @return "BOX " or ""
     */
    private String boxInPosText(Position pos) {
        return this.boxInPos(pos) ? "BOX " : "";
    }

    /**
     * Text if given position is an end position
     *
     * @param pos position
     * @return "END " or ""
     */
    private String endInPosText(Position pos) {
        return this.board.getPosContent(pos).equals(PositionContent.END) ? "END " : "";
    }

    /**
     * Text if given position is a wall
     *
     * @param pos position
     * @return "WALL " or ""
     */
    private String wallInPosText(Position pos) {
        return this.board.getPosContent(pos).equals(PositionContent.WALL) ? "WALL " : "";
    }

    /**
     * image that should be shown at position pos
     *
     * @param pos position
     * @return image type at position pos
     */
    public ImageType imageForPosition(Position pos) {
        if (board.getPosContent(pos).equals(PositionContent.WALL))
            return ImageType.WALL;
        else if (board.getPosContent(pos).equals(PositionContent.FREE)) {
            if (this.keeper.getPosition().equals(pos))
                return ImageType.KEEPER;
            else if (this.boxInPos(pos))
                return ImageType.BOX;
            else
                return ImageType.FREE;
        } else if (board.getPosContent(pos).equals(PositionContent.END)) {
            if (this.keeper.getPosition().equals(pos))
                return ImageType.KEEPER;
            else if (this.boxInPos(pos))
                return ImageType.BOXEND;
            else
                return ImageType.END;
        }
        return ImageType.END;
    }

    public List<Position> getMovesList() {
        return this.movesList;
    }

    /**
     * function creates the score file if the file does not exist
     */
    public void createScoreFile() {
        try {
            File file = new File(this.SCORE_FILE_PATH);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Writes the current Score in the Score file
     *
     * @param level      the level were the score was made
     * @param playerName the name of the player tha made the score
     */
    public void writeScoreFile(Level level, String playerName) {
        List<String> scoresList;
        try {
            scoresList = Files.readAllLines(Path.of(this.SCORE_FILE_PATH));
            scoresList.add(level.levelName() + " " + playerName + " " + this.movesList.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.write(Path.of(this.SCORE_FILE_PATH), scoresList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * function that returns if exists the score file into a list of Strings
     *
     * @return A list of Strings whit which string being a line of the score file
     */
    public List<String> getScoreFile() {
        List<String> scoresList = new ArrayList<>();
        File file = new File(this.SCORE_FILE_PATH);
        try {
            scoresList = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load");
            alert.show();
        }
        return scoresList;
    }

    /**
     * function that writes the high scores in a text area
     *
     * @param textArea where the scores will be written
     * @param level    the current level of the game
     */
    public void writeHighScores(TextArea textArea, Level level) {
        List<String> scoreFile = this.getScoreFile();
        List<Score> scoreList = new ArrayList<>();
        List<Score> currentLevelScores = new ArrayList<>();
        List<Score> topScores;

        //Makes all scores in score files na object of the class Score
        for (String line : scoreFile) {
            scoreList.add(new Score(line.substring(0, line.indexOf(" ")),
                    line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" ")),
                    Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1))));
        }
        Score currentScore = scoreList.get(scoreList.size() - 1);

        // Sorts all scores in the current level
        for (Score score : scoreList) {
            if (score.getLevelName().equals(level.levelName())) {
                currentLevelScores.add(score);
            }
        }
        // Orders the score accordingly to the amount of moves made
        currentLevelScores.sort(Comparator.comparingInt(Score::getMoves));

        // makes a topScore list with max three scores
        if (currentLevelScores.size() < 4) {
            topScores = currentLevelScores;
        } else {
            topScores = currentLevelScores.subList(0, 3);
        }

        //Writes in a textArea the high scores
        textArea.appendText("High Scores: " + level.levelName() + "\n");
        for (Score topScore : topScores) {
            textArea.appendText(topScore.toString());
            if (currentScore.equals(topScore)) {
                textArea.appendText("-> TOP");
            }
            textArea.appendText("\n");
        }
    }

    /**
     * adds the current gameState to undoGameState and redoGameState
     *
     * @param keeperPos the current position of the keeper
     */
    public void makeGameState(Position keeperPos) {
        List<Position> currentState = new ArrayList<>();
        currentState.add(keeperPos);
        for (Box box : boxes) {
            currentState.add(box.getPosition());
        }
        this.undoGameStates.add(currentState);
        this.redoGameStates.add(currentState);
    }

    public List<List<Position>> getUndoGameStates() {
        return this.undoGameStates;
    }

    public void setUndoGameStates(List<List<Position>> undoGameStates) {
        this.undoGameStates = undoGameStates;
    }

    public List<List<Position>> getRedoGameStates() {
        return this.redoGameStates;
    }

    public void setRedoGameStates(List<List<Position>> redoGameStates) {
        this.redoGameStates = redoGameStates;
    }

    public int getRedoCounter() {
        return redoCounter;
    }

    /**
     * increments redoCounter
     */
    public void incRedoCounter() {
        this.redoCounter++;
    }

    /**
     * decrements redo counter
     */
    public void decRedoCounter() {
        this.redoCounter--;
    }

    /**
     * version of moveBoxAt that updates the movement need for undo
     *
     * @param start initial box position
     * @param end   final box position
     */
    public void moveBoxTo(Position start, Position end) {
        List<Position> positions = new ArrayList<>();
        positions.add(start);
        positions.add(end);
        String messageToGUI = "move box " + start + " to " + end;
        this.boxes.remove(new Box(start));
        this.boxes.add(new Box(end));
        this.view.update(new MessageToUI(positions, messageToGUI));
    }

    /**
     * reads the keys pressed by the player and moves the keeper
     *
     * @param pane the pane that extends the Sokoban view
     */
    public void setOnKeyPressedMovement(Pane pane) {
        pane.setOnKeyPressed(event -> {
            final Map<KeyCode, Direction> keyToDir = Map.of(KeyCode.UP, Direction.UP, KeyCode.DOWN, Direction.DOWN, KeyCode.LEFT, Direction.LEFT, KeyCode.RIGHT, Direction.RIGHT);
            Direction direction = keyToDir.get(event.getCode());
            if (direction != null && !this.moveKeeper(direction)) {
                this.couldNotMove();
            }
        });
    }

    /**
     * Signal that keeper could not move
     */
    private void couldNotMove() {
        Toolkit.getDefaultToolkit().beep(); // did not move
    }

    /**
     * function that gets the labels of a gridPane
     *
     * @param line line of label in board
     * @param col  column of label in board
     * @param pane GridPane where the labels are in
     * @return the label at line, col
     */
    public javafx.scene.control.Label getLabel(int line, int col, GridPane pane) {
        ObservableList<Node> children = pane.getChildren();
        for (Node node : children) {
            if (GridPane.getRowIndex(node) == line && GridPane.getColumnIndex(node) == col) {
                assert (node.getClass() == javafx.scene.control.Label.class);
                return (Label) node;
            }
        }
        assert (false); // must not happen
        return null;
    }

}


