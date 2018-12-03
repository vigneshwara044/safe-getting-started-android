package net.maidsafe.sample.services;

import android.os.AsyncTask;

public class AsyncOperation<T> extends AsyncTask<IRequest, Void, IResult<T>> {

    private ISuccessHandler<T> successHandler;
    private IFailureHandler failureHandler;
    private final IProgressHandler progressHandler;

    public AsyncOperation(final IProgressHandler handler) {
        super();
        this.progressHandler = handler;
    }

    @Override
    protected void onPreExecute() {
        this.progressHandler.updateStatus(1);
    }

    @Override
    protected void onPostExecute(final IResult<T> result) {
        if (result.isError() && this.failureHandler != null) {
            this.failureHandler.onFailure(result.getError());
        } else {
            this.successHandler.onSuccess(result.getResult());
        }
        this.progressHandler.updateStatus(0);
    }

    public AsyncOperation<T> onResult(final ISuccessHandler iSuccessHandler) {
        this.successHandler = iSuccessHandler;
        return this;
    }


    public AsyncOperation<T> onException(final IFailureHandler iFailureHandler) {
        this.failureHandler = iFailureHandler;
        return this;
    }

    public AsyncOperation<T> execute(final IRequest<T>... args) {
        super.execute(args);
        return this;
    }

    @Override
    public IResult doInBackground(final IRequest... iRequests) {
        return iRequests[0].execute();
    }
}
