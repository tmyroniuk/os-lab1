package lab1.manager;

import lab1.ThreadFunction;
import lab1.Worker;

import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Manager<R extends Serializable> implements Runnable{

    final Lock lock = new ReentrantLock();
    Condition gotResult = lock.newCondition();
    final R zeroVal;

    protected Thread gThread, fThread;
    protected PipedInputStream gInput = new PipedInputStream();
    protected PipedInputStream fInput = new PipedInputStream();
    protected PipedOutputStream output = new PipedOutputStream();

    private void waitForRes() throws IOException {
        try {
            lock.lockInterruptibly();
            while (fInput.available() == 0 && gInput.available() == 0) {
                gotResult.await();
            }
        } catch(InterruptedException e) {
            fThread.interrupt();
            gThread.interrupt();
            System.out.println("Canceled;");
        } finally {
            lock.unlock();
        }
    }

    private R getRes() throws IOException {
        R res = null;
        if(fInput.available() == 0) {
            try (ObjectInput in = new ObjectInputStream(gInput)) {
                res = (R) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try (ObjectInput in = new ObjectInputStream(fInput)) {
                res = (R) in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    protected abstract R operation(R arg1, R arg2);

    public Manager(ThreadFunction<Integer, R> funcF, ThreadFunction<Integer, R> funcG, int arg, R zeroVal, PipedInputStream inputStream) {
        this.zeroVal = zeroVal;
        gThread = new Thread(new Worker<R>(funcG, arg, gInput, lock, gotResult));
        fThread = new Thread(new Worker<R>(funcF, arg, fInput, lock, gotResult));
        try {
            this.output = new PipedOutputStream(inputStream);
        } catch (IOException ignored) {}
    }

    @Override
    public void run(){
        R res = null, res1 = null, res2 = null;
        fThread.start();
        gThread.start();
        System.out.println("Started...");

        try {
            waitForRes();
            res1 = getRes();
            if(res1.equals(zeroVal)) {
                gThread.interrupt();
                fThread.interrupt();
                res = zeroVal;
            } else {
                waitForRes();
                res2 = getRes();
                res = operation(res1, res2);
            }
            ObjectOutput out = null;
            System.out.println("calc: " + res.toString());
            try {
                out = new ObjectOutputStream(output);
                out.writeObject(res);
                out.flush();
            } finally {
                try {
                    output.close();
                } catch (IOException ignored) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fThread.interrupt();
            gThread.interrupt();
        }
    }
}
