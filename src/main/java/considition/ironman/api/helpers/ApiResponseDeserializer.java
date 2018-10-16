package considition.ironman.api.helpers;

import com.google.gson.*;
import considition.ironman.api.models.response.ApiResponse;
import considition.ironman.api.models.response.ErrorApiResponse;
import considition.ironman.api.models.response.GameIdApiResponse;
import considition.ironman.api.models.response.GameStateApiResponse;

import java.lang.reflect.Type;

public class ApiResponseDeserializer implements JsonDeserializer<ApiResponse> {

    public ApiResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonSuccess = jsonObject.get("success");
        boolean success = jsonSuccess.getAsBoolean();

        ApiResponse model = null;

        if (!success) {
            model = new ErrorApiResponse();
        }
        else if (jsonObject.get("gameId") != null) {
            model = new GameIdApiResponse();
        }
        else if (jsonObject.get("gameState") != null) {
            model = new GameStateApiResponse();
        }

        if (model != null) {
            model = context.deserialize(json, model.getClass());
        }

        return model;
    }

}
