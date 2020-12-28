package lab1.manager;

import spos.lab1.demo.IntOps;

import java.io.PipedInputStream;

public class IntManager extends Manager<Integer> {
    public IntManager(int arg, PipedInputStream inputStream) {
        super(IntOps::funcF, IntOps::funcG, arg, 0, inputStream);
    }

    @Override
    protected Integer operation(Integer arg1, Integer arg2) {
        return arg1 * arg2;
    }
}
