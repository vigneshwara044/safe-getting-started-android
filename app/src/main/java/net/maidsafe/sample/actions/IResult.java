package net.maidsafe.sample.actions;

public interface IResult<T> {
    boolean isError();

    T getResult();

    Exception getError();
}
