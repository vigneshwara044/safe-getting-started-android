package net.maidsafe.sample.actions;


public interface IRequest<T> {
    IResult execute();
}