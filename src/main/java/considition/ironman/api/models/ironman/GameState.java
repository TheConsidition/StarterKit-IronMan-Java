package considition.ironman.api.models.ironman;

import java.util.List;

public class GameState {
    public String gameId;
    public String gameStatus;
    public int turn;
    public Player yourPlayer;
    public List<Player> otherPlayers;
    public TileInfo[][] tileInfo;
}
