package pt.ipbeja.po2.sokoban2023.guitext;


import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import pt.ipbeja.po2.sokoban2023.model.MessageToUI;
import pt.ipbeja.po2.sokoban2023.model.Position;
import pt.ipbeja.po2.sokoban2023.model.SokobanGameModel;
import pt.ipbeja.po2.sokoban2023.model.SokobanView;


/**
 * Game interface. Just a GridPane of buttons. No images. No menu.
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public class SokobanBoardText extends GridPane implements SokobanView {
    private final SokobanGameModel sokoban;
    private static final int SQUARE_SIZE = 80;

    /**
     * Create a sokoban board with labels and handle keystrokes
     */
    public SokobanBoardText(SokobanGameModel sokoban) {
        this.sokoban = sokoban;
        this.buildGUI();
        this.sokoban.setOnKeyPressedMovement(this);
    }

    /**
     * Build the interface
     */
    private void buildGUI() {
        assert (this.sokoban != null);

        // create one label for each position
        for (int line = 0; line < this.sokoban.getNLines(); line++) {
            for (int col = 0; col < this.sokoban.getNCols(); col++) {
                String textForButton = this.sokoban.textForPosition(new Position(line, col));
                Label label = new Label(textForButton);
                label.setMinWidth(SQUARE_SIZE);
                label.setMinHeight(SQUARE_SIZE);
                this.add(label, col, line); // add label to GridPane
            }
        }
        this.requestFocus();
    }


    /**
     * Simply updates the text for the buttons in the received positions
     *
     * @param messageToUI the sokoban model
     */
    @Override
    public void update(MessageToUI messageToUI) {
        for (Position p : messageToUI.positions()) {
            String s = this.sokoban.textForPosition(p);
            this.sokoban.getLabel(p.line(), p.col(), this).setText(s);
        }
        if (this.sokoban.allBoxesAreStored()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText("");
            alert.setContentText("Level completed!");
            alert.showAndWait();
            System.exit(0);
        }
    }
}
