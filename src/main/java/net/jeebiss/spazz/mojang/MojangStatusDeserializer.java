package net.jeebiss.spazz.mojang;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MojangStatusDeserializer implements JsonDeserializer<MojangStatus.Status> {

    @Override
    public MojangStatus.Status deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        return MojangStatus.Status.valueOf(jsonElement.getAsString().toUpperCase());
    }
}
