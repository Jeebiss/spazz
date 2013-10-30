package net.jeebiss.spazz.github.meta.objects;

import java.util.ArrayList;
import java.util.HashMap;

public interface Meta {
    
    public static enum Types { COMMAND, EVENT, REQUIREMENT, TAG, EXAMPLE, LANGUAGE, ACTION, MECHANISM }

    public String getObjectType();
    
    public HashMap<String, ArrayList<String>> getMeta();
    
}
