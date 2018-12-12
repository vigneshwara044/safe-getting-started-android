package net.maidsafe.sample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;
import android.util.Log;

import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.IFailureHandler;
import net.maidsafe.sample.services.IProgressHandler;
import net.maidsafe.sample.services.ITodoService;
import net.maidsafe.sample.services.OnDisconnected;
import net.maidsafe.sample.services.Result;
import net.maidsafe.sample.services.SafeTodoService;
import net.maidsafe.sample.services.AsyncOperation.Status;

public class CommonViewModel extends AndroidViewModel implements IFailureHandler, IProgressHandler {

    private static final MutableLiveData<Boolean> CONNECTED = new MutableLiveData<>();
    private static final MutableLiveData<Integer> STATUS = new MutableLiveData<>();;
    private static String errorMessage;
    protected final ITodoService todoService;


    public CommonViewModel(final Application application) {
        super(application);
        STATUS.setValue(Status.DONE.getValue());
        todoService = new SafeTodoService(application.getApplicationContext());
    }

    public void reconnect() {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.reconnect();
                return new Result();
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            CONNECTED.setValue(true);
        }).onException(this);
    }

    public void disconnect() {
        MockServices.simulateDisconnect();
        CONNECTED.setValue(false);
    }

    private void fetchAppData() {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.getAppData();
                return new Result();
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            STATUS.setValue(Status.CONNECTED.getValue());
        }).onException(this);
    }

    public void connect(final Uri data, final OnDisconnected disconnected) {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.connect(data, disconnected);
                return new Result();
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult((result) -> {
            CONNECTED.setValue(true);
            fetchAppData();
        }).onException(this);
    }

    public void mockAuthentication(final String uri, final OnDisconnected disconnected) {
        new AsyncOperation<Uri>(this).execute(() -> {
            try {
                final Uri response = MockServices.mockAuthenticate(uri);
                return new Result(response);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            connect((Uri) result, disconnected);
        }).onException(this);
    }

    public MutableLiveData<Boolean> getConnected() {
        return CONNECTED;
    }

    public MutableLiveData<Integer> getStatus() {
        return STATUS;
    }



    @Override
    public void onFailure(final Exception e) {
        errorMessage = e.getMessage();
        Log.e("INFO:", errorMessage);
        STATUS.setValue(Status.ERROR.getValue());
    }

    @Override
    public void updateStatus(final int s) {
        this.STATUS.setValue(s);
    }

    public String getErrorMessage() {
        return errorMessage;
    }


}
