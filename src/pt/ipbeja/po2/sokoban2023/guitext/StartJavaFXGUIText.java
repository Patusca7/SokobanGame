package pt.ipbeja.po2.sokoban2023.guitext;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.ipbeja.po2.sokoban2023.model.Level;
import pt.ipbeja.po2.sokoban2023.model.SokobanGameModel;

/**
 * Start a game with a hardcoded board and labels
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public class StartJavaFXGUIText extends Application {

    @Override
    public void start(Stage primaryStage) {
        Level level = new Level();

        SokobanGameModel sokoban = new SokobanGameModel(level);

        SokobanBoardText sokobanBoard = new SokobanBoardText(sokoban);
        primaryStage.setScene(new Scene(sokobanBoard));

        sokoban.registerView(sokobanBoard);
        sokobanBoard.requestFocus(); // to remove focus from first button
        primaryStage.show();
    }

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
