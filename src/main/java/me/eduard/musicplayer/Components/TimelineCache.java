package me.eduard.musicplayer.Components;

import javafx.animation.Timeline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TimelineCache {
    private static HashMap<Timeline, String> TIMELINES = new HashMap<>();
    public static void add(Timeline timeline, String id){
        TIMELINES.put(timeline, id);
    }
    public static void remove(Timeline timeline, String id){
        TIMELINES.remove(timeline, id);
    }
    public static void stopAll(){
        for(Map.Entry<Timeline, String> thisMap : TIMELINES.entrySet()){
            thisMap.getKey().stop();
        }
        TIMELINES.clear();
    }
    public static void say(){
        for(Map.Entry<Timeline, String> thisMap : TIMELINES.entrySet()){
            System.out.println(thisMap.getKey()+" : "+thisMap.getValue());
        }
    }
    public static void cleanup(){
        HashMap<Timeline, String> newMap = new HashMap<>();
        int encounter = 1;
        Iterator<Map.Entry<Timeline, String>> iterator = TIMELINES.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Timeline, String> entry = iterator.next();
            Timeline key = entry.getKey();
            String value = entry.getValue();
            if(!newMap.containsValue(value)){
                newMap.put(key, value);
            }else{
                if(encounter == 1){
                    encounter ++;
                    continue;
                }
                key.stop();
                iterator.remove();
                encounter = 1;
            }
        }
        TIMELINES.clear();
        TIMELINES.putAll(newMap);
        for(Map.Entry<Timeline, String> thisMap : TIMELINES.entrySet()){
            thisMap.getKey().play();
        }

    }

}
