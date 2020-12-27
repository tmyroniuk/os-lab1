package lab1.manager;

import spos.lab1.demo.Conjunction;

import java.io.PipedInputStream;

public class ConjunctionManager extends Manager<Boolean> {
    public ConjunctionManager(int arg, PipedInputStream inputStream) {
        super(Conjunction::funcF, Conjunction::funcG, arg, false, inputStream);
    }

    @Override
    protected Boolean operation(Boolean arg1, Boolean arg2) {
        return arg1 && arg2;
    }
}
