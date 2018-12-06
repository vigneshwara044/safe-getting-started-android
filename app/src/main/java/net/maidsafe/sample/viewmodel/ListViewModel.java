package net.maidsafe.sample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListViewModel extends CommonViewModel {

    private List<Task> taskList;
    private TodoList listInfo;
    private final MutableLiveData<List<Task>> liveTaskList;

    public ListViewModel(final Application application) {
        super(application);
        taskList = new ArrayList<>();
        liveTaskList = new MutableLiveData<>();
        liveTaskList.setValue(taskList);
    }

    public TodoList getListInfo() {
        return listInfo;
    }

    public MutableLiveData<List<Task>> getTaskList() {
        return liveTaskList;
    }

    public void addTask(final String description) {
        final Task task = new Task(description, new Date());
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.addTask(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult((result) -> {
            taskList.add(task);
            liveTaskList.setValue(taskList);
        }).onException(this);
    }

    public void deleteTask(final Task task) {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.deleteTask(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            taskList.remove(task);
            liveTaskList.setValue(taskList);
        }).onException(this);
    }

    public void updateTask(final Task task) {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.updateTaskStatus(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
        }).onException(this);

    }

    private void fetchListItems() {
        new AsyncOperation(this).execute(() -> {
            try {
                final List<Task> list = todoService.fetchListItems(listInfo);
                return new Result<List>(list);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if (result != null) {
                taskList = (List<Task>) result;
                liveTaskList.setValue(taskList);
            }
        });
    }

    public void prepareList() {
        try {
            fetchListItems();
        } catch (Exception e) {
            Log.e("INFO:", "Unable to fetch data");
        }
    }


    public void setListDetails(final TodoList todoList) {
        this.listInfo = todoList;
    }

    public void clearList() {
        taskList.clear();
        if (liveTaskList != null) {
            liveTaskList.setValue(taskList);
        }
    }
}
