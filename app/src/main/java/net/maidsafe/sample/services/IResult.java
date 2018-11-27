package net.maidsafe.sample.services;

public interface IResult<T> {
    boolean isError();

    T getResult();

    Exception getError();
}
