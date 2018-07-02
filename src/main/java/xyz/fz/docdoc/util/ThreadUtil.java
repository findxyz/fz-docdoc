package xyz.fz.docdoc.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
