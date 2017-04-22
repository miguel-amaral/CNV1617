package tool;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by miguel on 22/04/17.
 */

public class ContainerManager  {
    private static Map<Long, DataContainer> data = new HashMap<Long, DataContainer>();

    private ContainerManager() {}

    public static DataContainer getInstance(long threadID) {
        if(!data.containsKey(threadID)) {
            data.put(threadID, new DataContainer());
        }
        return data.get(threadID);
    }

    public static void clearInstance(long threadID) {
        if(data.containsKey(threadID)) {
            data.remove(threadID);
        }
    }
}