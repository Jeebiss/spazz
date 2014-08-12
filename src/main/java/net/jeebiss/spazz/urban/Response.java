package net.jeebiss.spazz.urban;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    private List<String> tags;
    private Result result_type;
    private List<Definition> list;
    private List<String> sounds;

    public List<String> getTags() { return tags; }
    public Result getResultType() { return result_type; }
    public List<Definition> getDefinitions() { return list; }
    public List<String> getSounds() { return sounds; }

    public enum Result { @SerializedName("exact") EXACT, @SerializedName("no_results") NONE }

}
