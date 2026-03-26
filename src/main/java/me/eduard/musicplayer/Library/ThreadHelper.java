package me.eduard.musicplayer.Library;

import java.awt.*;
import java.util.concurrent.*;

public class ThreadHelper {

    public void runAsNewThread(Runnable task) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Future<?> future = service.submit(task);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(future.isDone()){
                future.cancel(true);
                service.shutdownNow();
                System.out.println("Finished.");
                scheduledExecutorService.shutdownNow();
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

}
