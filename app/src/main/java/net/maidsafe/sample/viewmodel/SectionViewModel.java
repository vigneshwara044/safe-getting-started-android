package net.maidsafe.sample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.OnDisconnected;
import net.maidsafe.sample.services.Result;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;


public class SectionViewModel extends CommonViewModel {

    private static final String MOCK = "mock";
    private final MutableLiveData<List<TodoList>> liveSectionsList;
    private List<TodoList> sectionsList;

    public SectionViewModel(final Application application) {
        super(application);
        sectionsList = new ArrayList<>();
        liveSectionsList = new MutableLiveData<>();
        liveSectionsList.setValue(sectionsList);
    }

    public MutableLiveData<List<TodoList>> getSections() {
        liveSectionsList.setValue(sectionsList);
        return liveSectionsList;
    }

    public void authenticateApplication(final Context context, final OnDisconnected disconnected) {
        new AsyncOperation(this).execute(() -> {
            try {
                final String uri = todoService.generateAuthURL();
                return new Result(uri);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if (BuildConfig.FLAVOR.equals(MOCK)) {
                mockAuthentication((String) result, disconnected);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String) result)));
            }
        });
    }

    public void addSection(final String sectionTitle) {
        new AsyncOperation(this).execute(() -> {
            try {
                final TodoList sectionList = todoService.addSection(sectionTitle);
                return new Result(sectionList);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            sectionsList.add((TodoList) result);
            liveSectionsList.setValue(sectionsList);
        }).onException(this);

    }

    public void fetchSections() {
        new AsyncOperation(this).execute(() -> {
            try {
                final List<TodoList> list = todoService.fetchSections();
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
}
