package net.maidsafe.sample.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.IFailureHandler;
import net.maidsafe.sample.services.IProgressHandler;
import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.ITodoService;
import net.maidsafe.sample.services.Result;
import net.maidsafe.sample.services.SafeTodoService;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;


public class SectionViewModel extends ViewModel implements IFailureHandler, IProgressHandler {

    private MutableLiveData<List<TodoList>> liveSectionsList;
    private MutableLiveData<Boolean> loading;
    private List<TodoList> sectionsList;
    private ITodoService todoService;

    public SectionViewModel() {
        sectionsList = new ArrayList<>();
        loading = new MutableLiveData<>();
        loading.setValue(false);
        todoService = new SafeTodoService();
    }

    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<List<TodoList>> getSections() {
        if (liveSectionsList == null) {
            liveSectionsList = new MutableLiveData<>();
            liveSectionsList.setValue(sectionsList);
        }
        return liveSectionsList;
    }

    public void authenticateApplication(Context context) {
        new AsyncOperation(this).execute(() -> {
            try {
                String uri = todoService.generateAuthURL();
                return new Result(uri);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if(BuildConfig.FLAVOR.equals("mock")) {
                mockAuthentication((String) result);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String)result)));
            }
        });
    }

    public void addSection(String sectionTitle) {

        new AsyncOperation(this).execute(() -> {
            try {
                TodoList sectionList = todoService.addSection(sectionTitle);
                return new Result(sectionList);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            sectionsList.add((TodoList)result);
            liveSectionsList.setValue(sectionsList);
        }).onException(this);

    }


    public void connect(Uri data) {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.connect(data);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult((result) -> {
            prepareSections();
        }).onException(this);
    }

    private void prepareSections() {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.getAppData();
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            fetchSections();
        }).onException(this);
    }

    private void fetchSections() {
        new AsyncOperation(this).execute(() -> {
            try {
                List<TodoList> list = todoService.fetchSections();
                return new Result<List>(list);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if (result != null) {
                sectionsList = (List<TodoList>) result;
                liveSectionsList.setValue(sectionsList);
            }
        });
    }

    public void mockAuthentication(final String uri) {
        new AsyncOperation<Uri>(this).execute(() -> {
            try {
                Uri response = AuthService.mockAuthenticate(uri);
                return new Result(response);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            connect((Uri)result);
        }).onException(this);
    }

    @Override
    public void updateStatus(boolean isLoading) {
        loading.setValue(isLoading);
    }


}
