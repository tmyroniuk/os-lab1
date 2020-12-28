package lab1.manager;

import spos.lab1.demo.DoubleOps;

import java.io.PipedInputStream;

public class DoubleManager extends Manager<Double> {
    public DoubleManager(int arg, PipedInputStream inputStream) {
        super(DoubleOps::funcF, DoubleOps::funcG, arg, 0.0, inputStream);
    }

    @Override
    protected Double operation(Double arg1, Double arg2) {
        return arg1 * arg2;
    }
}
