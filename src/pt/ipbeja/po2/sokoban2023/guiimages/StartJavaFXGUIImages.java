package pt.ipbeja.po2.sokoban2023.guiimages;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pt.ipbeja.po2.sokoban2023.model.Level;
import pt.ipbeja.po2.sokoban2023.model.SokobanGameModel;


/**
 * Start a game with a hardcoded board and images
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public class StartJavaFXGUIImages extends Application {
    private Level level = new Level();
    private String environment = "Default/";

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        SokobanGameModel sokoban = new SokobanGameModel(this.level);

        SokobanBoardImages sokobanBoardImages =
                new SokobanBoardImages(sokoban,
                        primaryStage,
                        this.level,
                        this.environment,
                        "keeper.png",
                        "box.png",
                        "boxend.png",
                        "wall.png",
                        "free.png",
                        "end.png"
                );

        Scene scene = new Scene(sokobanBoardImages);
        primaryStage.setTitle("Sokoban");
        primaryStage.getIcons().add(new Image("Default/box.png"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        sokoban.registerView(sokobanBoardImages);
        sokobanBoardImages.requestFocus(); // to remove focus from first button
        primaryStage.show();

    }

    /**
     * Sets the this.level as level
     *
     * @param level the level of the game
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Sets this.environment as environment
     *
     * @param environment the environment of the game
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

}


