package me.eduard.musicplayer.Utils;

import javafx.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
public class DataStructures {
    public static final List<Timeline> TEXT_ANIMATIONS = new ArrayList<>();
    public static final List<Process> PROCESSES = new ArrayList<>();
    public static void cleanupTimelines(List<Timeline> list, boolean clear){
        if(clear){
            for(Timeline timeline : list){
                if(timeline == null)
                    continue;
                timeline.stop();
            }
            list.clear();
        }else{
            if(list.size() > 1){
                for(int i = 0; i < list.size(); i++){
                    list.get(i).stop();
                    list.remove(list.get(i));
                    if(list.size() <= 1){
                        break;
                    }
                }
            }
        }
    }
}
