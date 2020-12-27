package lab1.manager;

import lab1.ThreadFunction;
import lab1.Worker;

import java.io.*;
import java.util.concurrent.Semaphore;

public abstract class Manager<R extends Serializable> implements Runnable{

    private final Semaphore semaphore = new Semaphore(0);
    private final R zeroVal;

    protected Thread gThread, fThread;
    protected PipedInputStream gInput = new PipedInputStream();
    protected PipedInputStream fInput = new PipedInputStream();
    protected PipedOutputStream output = new PipedOutputStream();

    private R getRes() throws IOException {
        R res = null;
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            gThread.interrupt();
            fThread.interrupt();
            System.out.println("Canceled;");
        }
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
        gThread = new Thread(new Worker<R>(funcG, arg, gInput, semaphore));
        fThread = new Thread(new Worker<R>(funcF, arg, fInput, semaphore));
        try {
            this.output = new PipedOutputStream(inputStream);
        } catch (IOException ignored) {}
    }

    public void cancel() {
        gThread.interrupt();
        fThread.interrupt();
        Thread.currentThread().interrupt();
    }

    @Override
    public void run(){
        R res = null, res1 = null, res2 = null;
        fThread.start();
        gThread.start();
        System.out.println("Started...");

        try {
            res1 = getRes();
            if(res1.equals(zeroVal)) {
                gThread.interrupt();
                fThread.interrupt();
                res = zeroVal;
            } else {
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
