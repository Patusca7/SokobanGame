package pt.ipbeja.po2.sokoban2023.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Game board contents
 *
 * @author Diogo Patusca 23925, João Costa 22890
 * @version 2023/06/11
 * Based on https://en.wikipedia.org/wiki/Sokoban
 */
public class BoardModel {

    private final List<List<PositionContent>> board;

    public BoardModel(String boardContent) {
        Map<Character, PositionContent> pc =
                Map.of('F', PositionContent.FREE,
                        'W', PositionContent.WALL,
                        'E', PositionContent.END);
        List<List<PositionContent>> boardPos = new ArrayList<>();
        boardPos.add(new ArrayList<>());
        for (int i = 0; i < boardContent.length(); i++) {
            char c = boardContent.charAt(i);
            if (c == '\n') boardPos.add(new ArrayList<>());
            else boardPos.get(boardPos.size() - 1).add(pc.get(c));
        }
        this.board = Collections.unmodifiableList(boardPos);
    }

    public int nLines() {
        return this.board.size();
    }

    public int nCols() {
        return this.board.get(0).size();
    }

    public PositionContent getPosContent(Position pos) {
        return this.board.get(pos.line()).get(pos.col());
    }
}
