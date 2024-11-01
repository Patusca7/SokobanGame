package pt.ipbeja.po2.sokoban2023.guiimages;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pt.ipbeja.po2.sokoban2023.images.ImageType;
import pt.ipbeja.po2.sokoban2023.model.*;

import java.util.Map;
import java.util.Optional;


/**
 * Game interface. A BorderPane with a Menu and a TextArea to display the movements made
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on <a href="https://en.wikipedia.org/wiki/Sokoban">...</a>
 */
public class SokobanBoardImages extends BorderPane implements SokobanView {
    private static final int SQUARE_SIZE = 60;
    private static final int MOVES_LIMIT = 250;
    final Map<ImageType, Image> imageTypeToImage;
    private final SokobanGameModel sokoban;
    private final Stage primaryStage;
    private final GridPane gridPane;
    private final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final TextArea textArea;
    private final String playerName;
    private final HBox hBox;
    private final Level level;
    private final String environment;
    private SokobanMenuBar menu;


    /**
     * Create a sokoban board with labels and handle keystrokes and to coordinates of the lines and columns,
     * creates a text Area to display the movements
     * and creates a menuBar on top of the latter
     */
    public SokobanBoardImages(SokobanGameModel sokoban,
                              Stage primaryStage,
                              Level level,
                              String environment,
                              String keeperImageFilename,
                              String boxImageFilename,
                              String boxEndImageFilename,
                              String wallImageFilename,
                              String freeImageFilename,
                              String endImageFilename) {
        this.setOnMouseClicked(event -> this.requestFocus());
        this.environment = environment;
        this.imageTypeToImage = Map.of(ImageType.KEEPER, new Image("images/" + this.environment + keeperImageFilename),
                ImageType.BOX, new Image("images/" + this.environment + boxImageFilename),
                ImageType.BOXEND, new Image("images/" + this.environment + boxEndImageFilename),
                ImageType.WALL, new Image("images/" + this.environment + wallImageFilename),
                ImageType.END, new Image("images/" + this.environment + endImageFilename),
                ImageType.FREE, new Image("images/" + this.environment + freeImageFilename));
        this.primaryStage = primaryStage;
        this.level = level;
        this.sokoban = sokoban;
        this.playerName = this.askPlayerName();

        this.textArea = new TextArea("Moves " + level.levelName() + "\n");
        this.textArea.setMaxWidth(250);
        this.textArea.setEditable(false);
        //this.textArea.setOnMouseClicked(event -> this.requestFocus());

        this.textArea.setFont(Font.font("Calibri", FontWeight.BOLD, 15));

        this.gridPane = new GridPane();
        this.hBox = new HBox();
        this.buildGUI();

        this.sokoban.setOnKeyPressedMovement(this);
    }

    /**
     * Build the interface with the coordinates, the textArea and the menuBar
     */
    private void buildGUI() {
        assert (this.sokoban != null);

        // create one label for each position
        for (int line = 0; line < this.sokoban.getNLines() + 1; line++) {
            for (int col = 0; col < this.sokoban.getNCols() + 1; col++) {
                Label label = new Label();
                if (col == this.sokoban.getNCols() && line != this.sokoban.getNLines()) {
                    this.formattedLabelText(label, String.valueOf(line));
                } else if (line == this.sokoban.getNLines() && col != this.sokoban.getNCols()) {
                    this.formattedLabelText(label, String.valueOf(LETTERS.charAt(col)));
                } else if (line < this.sokoban.getNLines() && col < this.sokoban.getNCols()) {
                    label.setMinWidth(SQUARE_SIZE);
                    label.setMinHeight(SQUARE_SIZE);
                    ImageType imgType = this.sokoban.imageForPosition(new Position(line, col));
                    label.setGraphic(this.createImageView(this.imageTypeToImage.get(imgType)));
                }
                this.gridPane.add(label, col, line); // add label to GridPane
            }
        }
        this.hBox.getChildren().addAll(textArea, gridPane);
        this.setCenter(hBox);
        this.setupUI();
        this.requestFocus();
    }

    /**
     * creates an image from an image
     *
     * @param image image needed to create the image view
     * @return imageView
     */
    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(SQUARE_SIZE);
        imageView.setFitWidth(SQUARE_SIZE);
        return imageView;
    }


    /**
     * Formats the text in a Label for the coordinate labels and add the coordinate
     *
     * @param label label that has his text formatted
     * @param text  text of the label
     */
    private void formattedLabelText(Label label, String text) {
        label.setText(text);
        label.setMinHeight(SQUARE_SIZE);
        label.setMinWidth(SQUARE_SIZE);
        label.setStyle("-fx-text-fill: black; " +
                "-fx-background-color: #BFBFBF;" +
                " -fx-border-color: #B5B5B5;" +
                "-fx-alignment: center; " +
                "-fx-font-size: 25px;");
    }

    /**
     * Writes in this.textArea the current movement
     *
     * @param messageToUI the sokoban model
     */
    private void writeMovementLog(MessageToUI messageToUI) {
        if (!messageToUI.getMessage().contains("box")) {
            Position prevPos = messageToUI.positions().get(1);
            Position currPos = messageToUI.positions().get(0);
            String textLog = String.format("(%d, %s)->(%d, %s)\n",
                    prevPos.line(),
                    this.LETTERS.charAt(prevPos.col()),
                    currPos.line(),
                    LETTERS.charAt(currPos.col()));
            this.textArea.appendText(textLog);
        }
    }

    /**
     * Updates the Labels accordingly to the movements the player made
     * Updates the textArea accordingly to the movements the player made
     * See if the game as ended and proceeds accordingly showing an alert and the high scores on the level
     *
     * @param messageToUI the sokoban model
     */
    @Override
    public void update(MessageToUI messageToUI) {
        for (Position p : messageToUI.positions()) {
            ImageType imageType = this.sokoban.imageForPosition(p);
            this.sokoban.getLabel(p.line(), p.col(), this.gridPane).setGraphic(this.createImageView(this.imageTypeToImage.get(imageType)));
        }
        this.writeMovementLog(messageToUI);

        if (this.sokoban.getMovesList().size() == 1) {
            this.menu.setLoadKeeperCourseDisable();
            this.menu.startTimer();
        }

        if (this.sokoban.allBoxesAreStored() && this.sokoban.getMovesList().size() <= MOVES_LIMIT) {
            this.menu.stopTimer();

            TextArea highScoreArea = new TextArea();
            highScoreArea.setMaxWidth(200);
            highScoreArea.setEditable(false);
            highScoreArea.setFont(Font.font("Calibri", FontWeight.BOLD, 15));
            HBox endHBox = new HBox(this, highScoreArea);

            this.sokoban.createScoreFile();
            this.sokoban.writeScoreFile(this.level, this.playerName);
            this.sokoban.writeHighScores(highScoreArea, this.level);

            Scene scene = new Scene(endHBox);
            this.primaryStage.setScene(scene);

            this.showEndGameAlert(true);

        } else if (this.sokoban.getMovesList().size() > MOVES_LIMIT) {
            this.showEndGameAlert(false);
        }
    }

    /**
     * Shows an alert with text (that differs if the player lost or won) with set on action button to restart or exit the game
     *
     * @param victory true if game was won, false if not
     */
    private void showEndGameAlert(boolean victory) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (victory) {
            alert.setTitle("Game Won");
            alert.setHeaderText("Level Completed!");
            alert.setContentText("Would You Like To Play Again?");
        } else {
            alert.setTitle("Game Lost");
            alert.setHeaderText("Level Uncompleted!");
            alert.setContentText("Would You Like To Play Again?");
        }

        // Add buttons to the alert
        ButtonType buttonTypeRestart = new ButtonType("Play Again");
        ButtonType buttonTypeExit = new ButtonType("Exit");


        alert.getButtonTypes().setAll(buttonTypeRestart, buttonTypeExit);

        Platform.runLater(() -> alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeExit) {
                System.exit(0);
            } else if (response == buttonTypeRestart) {
                this.restart(this.primaryStage);
            }
        }));
    }

    /**
     * Ask for the player name with max 3 letters
     *
     * @return the player name
     */
    public String askPlayerName() {
        TextInputDialog dialog = new TextInputDialog("AAA");
        dialog.setTitle("Player Name");
        dialog.setHeaderText("Enter your name (3 letters max):");
        dialog.setContentText("Name:");
        Optional<String> name = dialog.showAndWait();
        if (name.isPresent()) {
            String playerName = name.get();
            if (playerName.length() > 3) {
                playerName = playerName.substring(0, 3);
            }
            return playerName.toUpperCase();
        } else System.exit(0);
        assert (false); // must not happen
        return null;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * Restarts the game
     *
     * @param primaryStage current stage
     */
    public void restart(Stage primaryStage) {
        StartJavaFXGUIImages newGame = new StartJavaFXGUIImages();
        newGame.setEnvironment(this.environment);
        newGame.setLevel(this.level);
        newGame.start(primaryStage);
    }

    /**
     * Sets on top of this BoarderPane a menuBar
     */
    public void setupUI() {
        this.menu = new SokobanMenuBar(this.level, this.primaryStage,
                this.textArea, this.sokoban,
                this, this.environment);
        MenuBar menuBar = this.menu.createMenuBar();
        this.setTop(menuBar);
    }
}