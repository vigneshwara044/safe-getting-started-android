package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.R;
import net.maidsafe.sample.viewmodel.TodoViewModel;
import net.maidsafe.sample.model.Task;

import androidx.navigation.Navigation;

public class TodoActivity extends AppCompatActivity implements AuthFragment.OnFragmentInteractionListener, ListFragment.OnFragmentInteractionListener {

    TodoViewModel viewModel;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Os.setenv("SAFE_MOCK_VAULT_PATH", getApplicationContext().getFilesDir().getPath(), true);
            Log.d("SAFE_MOCK_VAULT_PATH", getApplicationContext().getFilesDir().getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TodoViewModel.class);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            viewModel.handleUriActivation(data);
        }
        setContentView(R.layout.activity_auth);
        progressBar = findViewById(R.id.progressBar);

        final Observer<Boolean> loadingObserver = loading -> {
            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        viewModel.getLoading().observe(this, loadingObserver);
    }

    public void onFragmentInteraction(View v, Object object) {
        switch (v.getId()) {
            case R.id.authButton:
                    viewModel.authenticateApplication(getApplicationContext());
                    Navigation.findNavController(v).navigate(R.id.listFragment);
                break;
            case R.id.taskDeleteButton:
                viewModel.deleteTask((Task) object);
                break;
            case R.id.addNewTask:
                String taskDesc = (String) object;
                viewModel.addNewTask(taskDesc);
                break;
            case R.id.taskCheckbox:
                viewModel.updateTask((Task) object);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.my_nav_host_fragment).navigateUp();
    }

}
