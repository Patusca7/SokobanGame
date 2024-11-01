package pt.ipbeja.po2.sokoban2023.model;

import java.util.List;

/**
 * Message to be sent from the model so that the interface updates the positions in the list
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public record MessageToUI(List<Position> positions, String message) {
    public String getMessage() {
        return message();
    }
}
