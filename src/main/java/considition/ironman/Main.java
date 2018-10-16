package considition.ironman;

import considition.ironman.api.Api;
import considition.ironman.api.models.ironman.GameState;

import java.util.Random;

public class Main {

    // TODO: Enter your API key
    private static final String API_KEY = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";

    // Game options
    private static final int maxPlayers = 1;
    private static final String map = "standardmap";
    private static final int numberOfStreams = 10;
    private static final int numberOfElevations = 10;
    private static final int numberOfPowerups = 10;

    private static void play(GameState gameState) {
        // TODO: Implement your ironman
    	        // Example
        Random random = new Random();
        String[] directions = new String[] { "e", "w", "n", "s" };
        for (int i = 0; i < 50; i++) {
            String direction = directions[random.nextInt(directions.length)];
            System.out.println("Starting turn " + gameState.turn);
            gameState = Api.makeMove(gameState.gameId, direction, "slow");
        }
    }

    public static void main(String[] args) {
        Api.setApiKey(API_KEY);
        Api.endPreviousGamesIfAny(); //Can only have 2 active games at once. This will end any previous ones.
        String gameId = Api.initGame(maxPlayers, map, numberOfStreams, numberOfElevations, numberOfPowerups);
        GameState gameState;
        gameState = Api.joinGame(gameId);
        gameState = Api.tryReadyUp(gameId);
        play(gameState);
    }
}
