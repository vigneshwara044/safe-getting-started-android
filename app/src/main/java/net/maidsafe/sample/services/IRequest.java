package net.maidsafe.sample.services;


public interface IRequest<T> {
    IResult execute();
}