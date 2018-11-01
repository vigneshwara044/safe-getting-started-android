package net.maidsafe.sample.actions;

public class Result<T> implements IResult<T> {

    private T result;
    private Exception e;

    public Result(T result) {
        this.result = result;
    }

    public Result(Exception e) {
        this.e = e;
    }

    @Override
    public boolean isError() {
        return this.e != null;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public Exception getError() {
        return e;
    }
}
