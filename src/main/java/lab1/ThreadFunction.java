package lab1;

@FunctionalInterface
public interface ThreadFunction<T, R> {
    R apply(T t) throws InterruptedException;
}
