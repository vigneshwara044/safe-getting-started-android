package net.maidsafe.sample.actions;

public interface ISuccessHandler<T> {
    void onSuccess(T result);
}