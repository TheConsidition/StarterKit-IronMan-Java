package considition.ironman.api;

import com.google.gson.*;
import considition.ironman.api.helpers.ApiResponseDeserializer;
import considition.ironman.api.models.ironman.GameState;
import considition.ironman.api.models.response.ApiResponse;
import considition.ironman.api.models.response.ErrorApiResponse;
import considition.ironman.api.models.response.GameIdApiResponse;
import considition.ironman.api.models.response.GameStateApiResponse;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class Api {

    private static final String BASE_PATH = "http://theconsidition.se/considition/ironman";
    private static String _apiKey;
    private static boolean _silenced;
    private static Gson _gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ApiResponse.class, new ApiResponseDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
            }
        });
        _gson = gsonBuilder.create();
    }

    private static void log(String message) {
        if (!_silenced) {
            System.out.println("API: " + message);
        }
    }

    private static ApiResponse readApiResponse(HttpURLConnection con) throws IOException {
        InputStream stream;
        if (con.getResponseCode() < 400) {
            stream = con.getInputStream();
        }
        else {
            stream = con.getErrorStream();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        StringBuilder received = new StringBuilder();
        String chunk;
        while ((chunk = br.readLine()) != null) {
            received.append(chunk);
        }

        ApiResponse response = _gson.fromJson(received.toString(), ApiResponse.class);
        if (!handleApiResponse(response)) {
            return null;
        }

        if (con.getResponseCode() >= 400) {
            log(con.getResponseCode() + ": " + con.getResponseMessage());
            return null;
        }

        return response;
    }

    private static ApiResponse get(String path) {
        try {
            URL url = new URL(BASE_PATH + "/" + path);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            ApiResponse response = readApiResponse(con);

            con.disconnect();
            return response;
        } catch (IOException ex) {
            log("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private static ApiResponse post(String path, Object data) {
        try {
            URL url = new URL(BASE_PATH + "/" + path);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("x-api-key", _apiKey);

            Gson gson = new Gson();
            String jsonData = gson.toJson(data);

            OutputStream os = con.getOutputStream();
            os.write(jsonData.getBytes());
            os.flush();
            os.close();

            ApiResponse response = readApiResponse(con);

            con.disconnect();
            return response;
        }
        catch (IOException ex) {
            log("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    
    private static ApiResponse delete(String path) {
        try {
            URL url = new URL(BASE_PATH + "/" + path);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("x-api-key", _apiKey);

            OutputStream os = con.getOutputStream();
            os.flush();
            os.close();

            ApiResponse response = readApiResponse(con);

            con.disconnect();
            return response;
        }
        catch (IOException ex) {
            log("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private static boolean handleApiResponse(ApiResponse response) {
        if (response instanceof ErrorApiResponse) {
            ErrorApiResponse error = (ErrorApiResponse)response;
            String message = "An error occured: " + error.message;
            log(message);
            return false;
        }
        return true;
    }

    public static void setApiKey(String apiKey) {
        _apiKey = apiKey;
    }

    public static void silence() {
        _silenced = true;
    }

    public static void unsilence() {
        _silenced = false;
    }

    public static String initGame(int maxPlayers, String map, int numberOfStreams, int numberOfElevations, int numberOfPowerups) {
        JsonObject data = new JsonObject();
        data.addProperty("maxPlayers", maxPlayers);
        data.addProperty("map", map);
        data.addProperty("numberOfStreams", numberOfStreams);
        data.addProperty("numberOfElevations", numberOfElevations);
        data.addProperty("numberOfPowerups", numberOfPowerups);
        ApiResponse response = post("games", data);
        if (response == null) {
            System.exit(1);
        }
        GameIdApiResponse gameResponse = (GameIdApiResponse)response;
        log("Created new game: " + gameResponse.gameId);
        return gameResponse.gameId;
    }

    public static GameState getGame(String gameId) {
    	log("Getting game: " + gameId);
        ApiResponse response = get("games/" + gameId + "/" + _apiKey);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState joinGame(String gameId) {
        JsonObject data = new JsonObject();
        ApiResponse response = post("games/" + gameId + "/join", data);
        if (response == null) {
            System.exit(1);
        }
        log("Joined game: " + gameId);
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState readyUp(String gameId) {
        log("Readying up!");
        JsonObject data = new JsonObject();
        ApiResponse response = post("games/" + gameId + "/ready", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState tryReadyUp(String gameId) {
        log("Readying up!");
        while (true) {
        	log("Trying to ready up!");
            JsonObject data = new JsonObject();
            ApiResponse response = post("games/" + gameId + "/ready", data);
            if (response == null) {
                try {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ex) {

                }
                continue;
            }
            return ((GameStateApiResponse)response).gameState;
        }
    }

    public static GameState makeMove(String gameId, String direction, String speed) {
    	log("Attempting to makeMove with speed: " + speed + " and " + " direction: " + direction);
        JsonObject data = new JsonObject();
        data.addProperty("speed", speed);
        data.addProperty("direction", direction);
        ApiResponse response = post("games/" + gameId + "/action/move", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState step(String gameId, String direction) {
    	log("Attempting to step in direction: " + direction);
        JsonObject data = new JsonObject();
        data.addProperty("direction", direction);
        ApiResponse response = post("games/" + gameId + "/action/step", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState rest(String gameId) {
    	log("Attempting to rest!");
        JsonObject data = new JsonObject();
        ApiResponse response = post("games/" + gameId + "/action/rest", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }

    public static GameState usePowerup(String gameId, String powerupName) {
    	log("Attempting to use powerup: " + powerupName);
        JsonObject data = new JsonObject();
        data.addProperty("name", powerupName);
        ApiResponse response = post("games/" + gameId + "/action/usepowerup", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }
    
    public static GameState dropPowerup(String gameId, String powerupName) {
    	log("Attempting to drop powerup: " + powerupName);
        JsonObject data = new JsonObject();
        data.addProperty("name", powerupName);
        ApiResponse response = post("games/" + gameId + "/action/droppowerup", data);
        if (response == null) {
            System.exit(1);
        }
        return ((GameStateApiResponse)response).gameState;
    }
    
    public static void endPreviousGamesIfAny() {
    	log("Attempting to remove previous games if any.");
        delete("games");
    }
}
