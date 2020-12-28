package lab1;

import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Worker<R extends Serializable> implements Runnable {
    private PipedOutputStream output;
    private ThreadFunction<Integer, R> func;
    private final Condition gotResult;
    private Lock lock;
    private final int arg;

    public Worker(ThreadFunction<Integer, R> func , int arg, PipedInputStream inputStream, Lock lock, Condition gotResult) {
        this.func = func;
        this.arg = arg;
        this.lock = lock;
        this.gotResult = gotResult;
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
                lock.lock();
                gotResult.signal();
            } finally {
                lock.unlock();
                output.close();
            }
        } catch (InterruptedException e) {
            System.out.println("One thread interrupted");
        } catch (IOException e) {
            System.out.println("IO Exception\n");
        }
    }
}
