package net.maidsafe.sample.services;

public class Result<T> implements IResult<T> {

    private T result;
    private Exception e;

    public Result(final T result) {
        this.result = result;
    }

    public Result(final Exception e) {
        this.e = e;
    }

    public Result() {
        result = null;
        this.e = null;
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
