package net.techtrends.general.exception;

public class ServerExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Uncaught exception in thread " + t.getName() + ":");
        e.printStackTrace();
    }
}
