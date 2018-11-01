package net.maidsafe.sample.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.maidsafe.api.Client;
import net.maidsafe.api.model.App;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.actions.IFailureHandler;
import net.maidsafe.sample.actions.IProgressHandler;
import net.maidsafe.sample.actions.NetworkOperation;
import net.maidsafe.sample.actions.Result;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.view.TodoActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class TodoViewModel extends ViewModel implements IFailureHandler, IProgressHandler {

    private Client client;
    private MDataInfo mDataInfo;
    private MutableLiveData<List<Task>> liveTaskList;
    private MutableLiveData<Boolean> loading;
    private List<Task> taskList;
    private int index;
    private App app;

    public TodoViewModel() {
        taskList = new ArrayList<>();
        app = new App("net.maidsafe.sample", "Safe TODO Java",
                "Maidsafe.net", "0.1.0");
        index = 0;
        loading = new MutableLiveData<>();
        loading.setValue(false);
    }

    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<List<Task>> getTaskList() {
        if (liveTaskList == null) {
            liveTaskList = new MutableLiveData<>();
            liveTaskList.setValue(taskList);
        }
        return liveTaskList;
    }

    public void addNewTask(String description) {
        Task task = new Task(description, index);
        new NetworkOperation(this).execute(() -> {
            try {
                Services.addTask(mDataInfo, client, index, task);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }

        }).onResult((result) -> {
            taskList.add(task);
            index++;
            liveTaskList.setValue(taskList);
        }).onException(this);

    }

    public void deleteTask(Task t) {
        new NetworkOperation(this).execute(() -> {
            try {
                Services.deleteTask(mDataInfo, client, t);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            Log.d("LIST SIZES", "Task list: " + taskList.size() + "\nLiveTaskList: " + liveTaskList.getValue().size());
            taskList.remove(t);
            liveTaskList.setValue(taskList);
            Log.d("LIST SIZES", "Task list: " + taskList.size() + "\nLiveTaskList: " + liveTaskList.getValue().size());
        }).onException(this);
    }

    public void updateTask(Task task) {
        task.setComplete(!task.getComplete());
        task.setVersion(task.getVersion() + 1);
        new NetworkOperation(this).execute(() -> {
            try {
                Services.updateTask(mDataInfo, client, task);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {

        }).onException(this);

    }


    public void authenticateApplication(Context context) {
        new NetworkOperation(this).execute(() -> {
            try {
                String uri = Services.authenticateApplication(app);
                return new Result(uri);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if(BuildConfig.FLAVOR.equals("mock")) {
                mockAuthenticate((String) result);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String)result)));
            }
        });
    }

    public void handleUriActivation(Uri data) {
        new NetworkOperation(this).execute(() -> {
            try {
                Client c = Services.handleUriActivation(data, app);
                return new Result(c);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            client = (Client) result;
            prepareMutableData();
        }).onException(this);
    }

    private void prepareMutableData() {
        new NetworkOperation(this).execute(() -> {
            try {
                MDataInfo mdInfo = Services.prepareMutableData(client, app);
                return new Result(mdInfo);

            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            mDataInfo = (MDataInfo) result;
            listEntries(mDataInfo);
        }).onException(this);
    }

    private void listEntries(MDataInfo mdInfo) {
        new NetworkOperation(this).execute(() -> {
            try {
                List<Task> list = Services.listEntries(client, mdInfo);
                return new Result<List>(list);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if (result != null) {
                taskList = (List<Task>) result;
                liveTaskList.setValue(taskList);
                index = taskList.size();
            }
        });
    }

    public void mockAuthenticate(final String uri) {

        new NetworkOperation<Uri>(this).execute(() -> {
            try {
                Uri response = AuthService.mockAuthenticate(uri);
                return new Result(response);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            handleUriActivation((Uri) result);
        }).onException(this);

    }

    @Override
    public void updateStatus(boolean isLoading) {
        loading.setValue(isLoading);
    }
}
