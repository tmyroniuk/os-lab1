package lab1;

import java.io.*;
import java.util.concurrent.Semaphore;

public class Worker<R extends Serializable> implements Runnable {
    private PipedOutputStream output;
    private ThreadFunction<Integer, R> func;
    private final Semaphore semaphore;
    private final int arg;

    public Worker(ThreadFunction<Integer, R> func , int arg, PipedInputStream inputStream, Semaphore semaphore) {
        this.func = func;
        this.arg = arg;
        this.semaphore = semaphore;
        try {
            output = new PipedOutputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            R res = func.apply(arg);
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(output);
                out.writeObject(res);
                out.flush();
                semaphore.release();
            } finally {
                try {
                    output.close();
                } catch (IOException ignored) {}
            }
        } catch (InterruptedException e) {
            System.out.println("One thread interrupted\n");
        } catch (IOException e) {
            System.out.println("IO Exception\n");
        }
    }
}
