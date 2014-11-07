package net.jeebiss.spazz.urban;

import java.util.List;

public class Response {

    private List<String> tags;
    private String result_type;
    private List<Definition> list;
    private List<String> sounds;

    public List<String> getTags() { return tags; }
    public Result getResultType() { return Result.getByValue(result_type); }
    public List<Definition> getDefinitions() { return list; }
    public List<String> getSounds() { return sounds; }

    public enum Result {
        EXACT("exact"),
        NONE("no_results");

        final String string;

        Result(String string) {
            this.string = string;
        }

        public static Result getByValue(String name) {
            for (Result result : values())
                if (name.equals(result.string))
                    return result;
            return null;
        }
    }

}
