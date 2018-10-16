package considition.ironman.api.models.ironman;

import java.time.LocalDateTime;
import java.util.List;

public class Player {
    public int xPos;
    public int yPos;
    public String status;
    public int statusDuration;
    public int stamina;
    public List<String> powerupInventory;
    public String name;
    public int playedTurns;
    public List<ActivePowerup> activePowerups;
}
