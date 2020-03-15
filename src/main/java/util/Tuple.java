package util;

public class Tuple<T, U> {
    private T t;
    private U u;
    public Tuple(T t, U u) {
        this.t = t;
        this.u = u;
    }
    public T getFirst() {
        return t;
    }
    public U getSecond() {
        return u;
    }

    public boolean equals(Object other) {
        if (getClass() != other.getClass())
            return false;
        Tuple<T,U> tp = (Tuple<T,U>) other;
        return t.equals(tp.getFirst()) && u.equals(tp.getSecond());
    }
}