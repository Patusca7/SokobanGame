package pt.ipbeja.po2.sokoban2023.guiimages;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import pt.ipbeja.po2.sokoban2023.model.Direction;
import pt.ipbeja.po2.sokoban2023.model.Level;
import pt.ipbeja.po2.sokoban2023.model.Position;
import pt.ipbeja.po2.sokoban2023.model.SokobanGameModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * All MenuBar functions
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 */
public class SokobanMenuBar {
    private final SokobanGameModel sokoban;
    private final SokobanBoardImages boardImages;
    private final Level level;
    private final Stage primaryStage;
    private final TextArea textArea;
    private final String environment;
    private Label timerLabel;
    private Timeline timer;
    private int seconds;
    private int minutes;
    private MenuItem loadKeeperCourse;

    /**
     * Initializes the SokobanMenuBar
     *
     * @param level        current game level
     * @param primaryStage stage that shows the game
     * @param textArea     area with the player movements
     * @param sokoban      Game model
     * @param boardImages  SokobanBoardImages
     * @param environment  the current environment of the game
     */
    public SokobanMenuBar(Level level, Stage primaryStage,
                          TextArea textArea, SokobanGameModel sokoban,
                          SokobanBoardImages boardImages, String environment) {
        this.level = level;
        this.primaryStage = primaryStage;
        this.textArea = textArea;
        this.sokoban = sokoban;
        this.boardImages = boardImages;
        this.environment = environment;
    }

    /**
     * Creates the menu bar with all the menus and items necessary and there set on action
     *
     * @return menuBar with file menu and timer
     */
    public MenuBar createMenuBar() {
        MenuItem writeMoves = new MenuItem("Write Moves");
        writeMoves.setOnAction(actionEvent -> this.writeCourseFile());

        MenuItem swapLevel = new MenuItem("Levels");
        swapLevel.setOnAction(event -> this.changeLevel());

        MenuItem undo = new MenuItem("Undo");
        undo.setOnAction(event -> this.undoMove());

        MenuItem redo = new MenuItem("Redo");
        redo.setOnAction(event -> this.redoMove());

        MenuItem changeEnvironment = new MenuItem("Change Environment");
        changeEnvironment.setOnAction(event -> changeEnvironment());

        this.loadKeeperCourse = new MenuItem("Load Keeper's Course");
        loadKeeperCourse.setOnAction(event -> loadCourse());
        Menu generateMenu = new Menu("File", null, writeMoves, swapLevel, loadKeeperCourse, changeEnvironment, undo, redo);
        this.timerLabel = new Label("00:00");
        Menu time = new Menu("", this.timerLabel);

        return new MenuBar(generateMenu, time);
    }

    /**
     * function use to undo movements
     */
    private void undoMove() {
        List<List<Position>> gameStates = this.sokoban.getUndoGameStates();
        // Mechanism would only work if size of gameStates > 1
        if (gameStates.size() > 1) {
            this.textArea.appendText("UNDO ");

            List<Position> currState = gameStates.get(gameStates.size() - 1);
            List<Position> prevState = gameStates.get(gameStates.size() - 2);
            Position currKeeperPos = currState.get(0);
            Position prevKeeperPos = prevState.get(0);

            // get current and previous boxes positions
            List<Position> currBoxPositions = new ArrayList<>();
            List<Position> prevBoxPositions = new ArrayList<>();
            for (Position currBoxPos : currState) {
                if (!currBoxPos.equals(currKeeperPos)) {
                    currBoxPositions.add(currBoxPos);
                }
            }
            for (Position prevBoxPos : prevState) {
                if (!prevBoxPos.equals(prevKeeperPos)) {
                    prevBoxPositions.add(prevBoxPos);
                }
            }

            //asserts that the size of the list of boxes positions
            assert (currBoxPositions.size() == prevBoxPositions.size());

            currBoxPositions.sort(Comparator.comparingInt(Position::line).thenComparing(Position::col));
            prevBoxPositions.sort(Comparator.comparingInt(Position::line).thenComparing(Position::col));
            // if there is a different box position in the list that box as to move to the previous position
            if (!currBoxPositions.equals(prevBoxPositions)) {
                List<Position> differentBoxes = this.getDifferentBoxes(currBoxPositions, prevBoxPositions);
                this.sokoban.moveBoxTo(differentBoxes.get(0), differentBoxes.get(1));
            }

            this.sokoban.moveKeeperTo(prevKeeperPos);

            // delete the most recent game state from the undoGameStates list
            this.sokoban.setUndoGameStates(gameStates.subList(0, gameStates.size() - 2));
            this.sokoban.incRedoCounter(); //increments redo counter so it's possible to redo
        }
    }

    /**
     * function use to redo movements
     */
    private void redoMove() {
        this.boardImages.setDisable(true);
        // Mechanism would only work if size of redoCounter > 0
        if (this.sokoban.getRedoCounter() > 0) {
            this.textArea.appendText("REDO ");
            List<List<Position>> redoGameStates = this.sokoban.getRedoGameStates();
            List<Position> redoPrevState = redoGameStates.get(redoGameStates.size() - 2);
            Position redoPrevKeeperPos = redoPrevState.get(0);
            this.sokoban.moveKeeperTo(redoPrevKeeperPos);

            // delete the most recent game state on RedoGameStates list
            this.sokoban.setRedoGameStates(redoGameStates.subList(0, redoGameStates.size() - 2));
            this.sokoban.decRedoCounter();
        }
        this.boardImages.setDisable(false);
    }

    /**
     * Chooses the file to change level to
     */
    private void changeLevel() {
        FileChooser fileChooser = new FileChooser();

        // Set the initial directory
        File initialDirectory = new File("levelFiles");
        fileChooser.setInitialDirectory(initialDirectory);

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        try {
            this.readLevel(Files.readAllLines(selectedFile.toPath()), selectedFile.getName());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load");
            alert.show();
        }

    }

    /**
     * creates the level from the levelFile and starts a new game with that level
     *
     * @param levelFile     List of strings with the lines of the file chosen in changeLevel
     * @param levelFileName Level file name
     */
    private void readLevel(List<String> levelFile, String levelFileName) {
        //gets keeper position and number of boxes
        int keeperLine = Integer.parseInt(levelFile.get(1).substring(0, levelFile.get(1).indexOf(" ")));
        int keeperCol = Integer.parseInt(levelFile.get(1).substring(levelFile.get(1).indexOf(" ") + 1));
        int boxNum = Integer.parseInt(levelFile.get(2));

        //gets every box to position
        Set<Position> boxesPos = new HashSet<>();
        for (int i = 3; i < boxNum + 3; i++) {
            int boxLine = Integer.parseInt(levelFile.get(i).substring(0, levelFile.get(i).indexOf(" ")));
            int boxCol = Integer.parseInt(levelFile.get(i).substring(levelFile.get(i).indexOf(" ") + 1));
            boxesPos.add(new Position(boxLine, boxCol));
        }

        //creates the board
        StringBuilder board = new StringBuilder();
        for (int j = boxNum + 3; j < levelFile.size(); j++) {
            if (j < levelFile.size() - 1) {
                board.append(levelFile.get(j)).append("\n");
            } else board.append(levelFile.get(j));
        }

        //creates the new level
        Level newLevel = new Level(levelFileName.substring(0, levelFileName.length() - 4), new Position(keeperLine, keeperCol), boxesPos, String.valueOf(board));

        //creates a new game with the new level and shows it on the current stage
        StartJavaFXGUIImages newGame = new StartJavaFXGUIImages();
        newGame.setLevel(newLevel);
        newGame.setEnvironment(this.environment);
        newGame.start(this.primaryStage);
    }

    /**
     * function use to write the course taken by the player in a text file
     */
    public void writeCourseFile() {
        List<String> toWrite = this.getCourse();

        // Set the initial directory
        FileChooser fileChooser = new FileChooser();
        File initialDirectory = new File("Courses");
        fileChooser.setInitialDirectory(initialDirectory);

        // Set initial file name
        fileChooser.setInitialFileName(boardImages.getPlayerName()+"_"+this.level.levelName()+"Solution.txt");
        File file = fileChooser.showSaveDialog(this.primaryStage);
        Path path = file.toPath();

        //writes file
        try {
            Files.write(path, toWrite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * function that gets the text in text area as a list of Strings
     *
     * @return a List of Strings with the movements taken by the player
     */
    private List<String> getCourse() {
        List<String> course = new ArrayList<>();
        String text = this.textArea.getText().strip();
        course.add(text);
        return course;
    }

    /**
     * Starts the timer in this.timerLabel
     */
    public void startTimer() {
        this.timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            this.seconds++;
            if (seconds == 60) {
                this.seconds = 0;
                this.minutes++;
            }
            String formattedTime = String.format("%02d:%02d", this.minutes, this.seconds);
            this.timerLabel.setText(formattedTime);
        }));
        this.timer.setCycleCount(Animation.INDEFINITE);
        this.timer.play();
    }

    /**
     * Stops the timer in this.TimerLabel
     */
    public void stopTimer() {
        this.timer.stop();
    }

    /**
     * Gets the positions that don't have a match in the other List
     *
     * @param currentBoxesPos first list of positions
     * @param prevBoxesPos    second list of positions
     * @return a list with the position that didn't match
     */
    private List<Position> getDifferentBoxes(List<Position> currentBoxesPos, List<Position> prevBoxesPos) {
        List<Position> differentPositions = new ArrayList<>();
        // for circle gets current box that doesn't match the previous box list
        for (Position currPos : currentBoxesPos) {
            boolean foundMatch = false;
            for (Position prevPos : prevBoxesPos) {
                if (currPos.equals(prevPos)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                differentPositions.add(currPos);
            }
        }
        // for circle gets previous box that doesn't match the current box list
        for (Position prevPos : prevBoxesPos) {
            boolean foundMatch = false;
            for (Position currPos : currentBoxesPos) {
                if (currPos.equals(prevPos)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                differentPositions.add(prevPos);
            }
        }

        // assert used to make sure that the lists have at least a different position
        assert (differentPositions.size() > 2);

        // assert used to make that the number of position is even
        assert (differentPositions.size() % 2 == 0);

        return differentPositions;
    }

    /**
     * Sorts a string in alphabetical order
     *
     * @param string original string
     * @return sorted original string
     */
    private String sortAlphabetical(String string) {
        char[] charArray = string.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }

    /**
     * Chooses the course to take ask if the player wants an animation or final position
     * and show what was chosen
     */
    private void loadCourse() {
        FileChooser fileChooser = new FileChooser();

        // Set the initial directory
        File initialDirectory = new File("Courses");
        fileChooser.setInitialDirectory(initialDirectory);

        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);
        try {
            List<String> courseFile = Files.readAllLines(selectedFile.toPath());

            // Shows an alert so the player can choose how does he want to see the course
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("How do you want to load the Course?");
            alert.setContentText("Final Position or animated?");

            // Add buttons to the alert
            ButtonType buttonType1 = new ButtonType("Final Position");
            ButtonType buttonType2 = new ButtonType("Animation");
            alert.getButtonTypes().setAll(buttonType1, buttonType2);

            // Button pressed action
            alert.showAndWait().ifPresent(response -> {
                if (response == buttonType1) {
                    this.loadCourseFinalPos(courseFile);
                } else if (response == buttonType2) {
                    this.loadCourseAnimated(courseFile);
                }
            });

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load");
            alert.show();
        }
    }

    /**
     * Function that shows the final position of a course
     *
     * @param courseFile List of strings with all the movements made in the course
     */
    private void loadCourseFinalPos(List<String> courseFile) {
        this.boardImages.setDisable(true);
        for (String textLine : courseFile) {
            //Checks if the level of the course matches the current level and acts accordingly
            if (textLine.contains("Level")) {
                if (this.compareLevelName(textLine)) {
                    this.boardImages.setDisable(false);
                    break;
                }
            } else {
                // mechanism for the keeper movement
                if (textLine.contains("UNDO")) {
                    undoMove();
                } else if (textLine.contains("REDO")) {
                    redoMove();
                } else {
                    //gets current and next position of the keeper
                    int currLine = Integer.parseInt(textLine.substring(1, textLine.indexOf(",")));
                    String currCol = String.valueOf(textLine.charAt(textLine.indexOf(" ") + 1));
                    int nextLine = Integer.parseInt(textLine.substring(textLine.lastIndexOf("(") + 1, textLine.lastIndexOf(",")));
                    String nextCol = String.valueOf(textLine.charAt(textLine.lastIndexOf(" ") + 1));

                    // if col are equal the keeper moved up or down depending on the value of currLine - nextLine
                    // if line are equal the keeper moved Right or Left depending on how if currCol + nextCol are in alphabetical order
                    if (currCol.equals(nextCol)) {
                        if (currLine - nextLine > 0) {
                            this.sokoban.moveKeeper(Direction.UP);

                        } else this.sokoban.moveKeeper(Direction.DOWN);
                    } else if ((currCol + nextCol).equals(sortAlphabetical(currCol + nextCol))) {
                        this.sokoban.moveKeeper(Direction.RIGHT);
                    } else this.sokoban.moveKeeper(Direction.LEFT);
                }
            }
        }
        this.boardImages.setDisable(false);
    }

    /**
     * Function that shows the animation of the chosen  course
     *
     * @param courseFile List of strings with all the movements made in the course
     */
    private void loadCourseAnimated(List<String> courseFile) {
        this.boardImages.setDisable(true);
        Duration duration = Duration.millis(500); //duration of every keyframe 0.5s
        Timeline timeline = new Timeline();
        int i = 0; // will serve has a counter to multiply the duration so every keyframe starts at the wright time

        //Checks if the level of the course matches the current level and acts accordingly
        for (String textLine : courseFile) {
            i++;
            if (textLine.contains("Level")) {
                if (this.compareLevelName(textLine)) {
                    this.boardImages.setDisable(false);
                    break;
                }
            } else {
                KeyFrame keyFrame;
                // mechanism for the keeper movement
                if (textLine.contains("UNDO")) {
                    keyFrame = new KeyFrame(duration.multiply(i), event -> undoMove());
                } else if (textLine.contains("REDO")) {
                    keyFrame = new KeyFrame(duration.multiply(i), event -> redoMove());
                } else {
                    //gets current and next position of the keeper
                    int currLine = Integer.parseInt(textLine.substring(1, textLine.indexOf(",")));
                    String currCol = String.valueOf(textLine.charAt(textLine.indexOf(" ") + 1));
                    int nextLine = Integer.parseInt(textLine.substring(textLine.lastIndexOf("(") + 1, textLine.lastIndexOf(",")));
                    String nextCol = String.valueOf(textLine.charAt(textLine.lastIndexOf(" ") + 1));

                    // if col are equal the keeper moved up or down depending on the value of currLine - nextLine
                    // if line are equal the keeper moved Right or Left depending on how if currCol + nextCol are in alphabetical order
                    if (currCol.equals(nextCol)) {
                        if (currLine - nextLine > 0) {
                            keyFrame = new KeyFrame(duration.multiply(i), event -> this.sokoban.moveKeeper(Direction.UP));
                        } else {
                            keyFrame = new KeyFrame(duration.multiply(i), event -> this.sokoban.moveKeeper(Direction.DOWN));
                        }
                    } else if ((currCol + nextCol).equals(sortAlphabetical(currCol + nextCol))) {
                        keyFrame = new KeyFrame(duration.multiply(i), event -> this.sokoban.moveKeeper(Direction.RIGHT));
                    } else {
                        keyFrame = new KeyFrame(duration.multiply(i), event -> this.sokoban.moveKeeper(Direction.LEFT));
                    }
                }
                timeline.getKeyFrames().add(keyFrame); //adds all keyFrames to timeline
            }
        }
        timeline.play();
        timeline.setOnFinished(event -> this.boardImages.setDisable(false));
    }

    /**
     * compares a string of the first line of the course file to see if it is the current level
     * show alert if levels are different
     *
     * @param levelTextLine text line with the level name
     * @return a boolean true if levels are equal else false
     */
    private boolean compareLevelName(String levelTextLine) {
        if (!levelTextLine.substring(levelTextLine.indexOf(" ") + 1).equals(this.level.levelName())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Wrong Level");
            alert.setHeaderText("Try again with " + this.level.levelName());
            alert.showAndWait();
            return true;
        }
        return false;
    }

    /**
     * Changes the game environment
     */
    public void changeEnvironment() {
        List<String> choices = new ArrayList<>();
        choices.add("Default");
        choices.add("Super Mario");
        choices.add("Police Station");
        choices.add("Pokemon");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Default", choices);
        dialog.setTitle("Change Environment");
        dialog.setHeaderText("Warning: Changing the environment will restart the Level");
        dialog.setContentText("Environment:");

        StartJavaFXGUIImages newGame = new StartJavaFXGUIImages();
        newGame.setLevel(this.level);
        String result = dialog.showAndWait().orElse(choices.get(0));
        if (result.equals(choices.get(0))) newGame.setEnvironment("Default/");
        else if (result.equals(choices.get(1))) newGame.setEnvironment("Super_Mario/");
        else if (result.equals(choices.get(2))) newGame.setEnvironment("Police_Station/");
        else if (result.equals(choices.get(3))) newGame.setEnvironment("Pokemon/");


        newGame.start(this.primaryStage);
    }

    public void setLoadKeeperCourseDisable() {
        this.loadKeeperCourse.setDisable(true);
    }
}