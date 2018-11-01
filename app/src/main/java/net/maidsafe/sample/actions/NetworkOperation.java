package net.maidsafe.sample.actions;

import android.os.AsyncTask;

public class NetworkOperation<T> extends AsyncTask<IRequest, Void, IResult<T>> {

    private ISuccessHandler<T> successHandler;
    private IFailureHandler failureHandler;
    private IProgressHandler progressHandler;

    public NetworkOperation(IProgressHandler handler) {
        this.progressHandler = handler;
    }

    @Override
    protected void onPreExecute() {
        this.progressHandler.updateStatus(true);
    }

    @Override
    protected void onPostExecute(IResult<T> result) {
        if (result.isError() && this.failureHandler != null) {
            this.failureHandler.onFailure(result.getError());
        } else {
            this.successHandler.onSuccess(result.getResult());
        }
        this.progressHandler.updateStatus(false);
    }

    public NetworkOperation<T> onResult(ISuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }


    public NetworkOperation<T> onException(IFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    public NetworkOperation<T> execute(IRequest<T>... args) {
        super.execute(args);
        return this;
    }

    @Override
    public IResult doInBackground(IRequest... iRequests) {
        return iRequests[0].execute();
    }
}
