package net.maidsafe.sample.services;

public interface ISuccessHandler<T> {
    void onSuccess(T result);
}