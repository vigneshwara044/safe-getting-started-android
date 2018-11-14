package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.R;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.OnDisconnected;
import net.maidsafe.sample.viewmodel.ListViewModel;
import net.maidsafe.sample.viewmodel.SectionViewModel;
import net.maidsafe.sample.model.Task;


import androidx.navigation.Navigation;

public class TodoActivity extends AppCompatActivity implements AuthFragment.OnFragmentInteractionListener,
        ListFragment.OnFragmentInteractionListener, TodoItemDetailsFragment.OnFragmentInteractionListener,
        ListHomeFragment.OnFragmentInteractionListener {

    SectionViewModel sectionViewModel;
    ListViewModel listViewModel;
    ProgressBar progressBar;
    boolean connected;
    View host;
    OnDisconnected onDisconnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        onDisconnected = () -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.disconnected_message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.reconnect), view -> {
                            sectionViewModel.reconnect();
                        })
                        .show();
            });
        };
        sectionViewModel = ViewModelProviders.of(this).get(SectionViewModel.class);
        listViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        progressBar = findViewById(R.id.progressBar);
        host = findViewById(R.id.my_nav_host_fragment);

        final Observer<Integer> statusObserver = status -> {
            if (status == 1) {
                progressBar.setVisibility(View.VISIBLE);
            } else if(status == 0){
                progressBar.setVisibility(View.INVISIBLE);
            } else if(status == -1) {
                showErrorPopUp(sectionViewModel.getErrorMessage());
            } else if(status == -2) {
                showErrorPopUp(listViewModel.getErrorMessage());
            }
        };
        final Observer<Boolean> clientObserver = isConnected -> {
            connected = isConnected;
        };


        listViewModel.getStatus().observe(this, statusObserver);
        sectionViewModel.getStatus().observe(this, statusObserver);
        sectionViewModel.getConnected().observe(this, clientObserver);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            sectionViewModel.connect(data, onDisconnected);
            Navigation.findNavController(host).navigate(R.id.listHomeFragment);
        }
    }

    private void showErrorPopUp(String errorMessage) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        View dialogView = View.inflate(this, R.layout.error_layout, null);
        dialogBuilder.setView(dialogView);

        TextView errorLog = dialogView.findViewById(R.id.errorLog);
        errorLog.setText(errorMessage);
        Button close = dialogView.findViewById(R.id.closeErrorLog);
        close.setOnClickListener(v -> {
            dialogBuilder.hide();
        });
        Navigation.findNavController(host).navigateUp();
        dialogBuilder.show();
    }

    public void onFragmentInteraction(View v, Object object) {
        if(!connected && v.getId() != R.id.authButton) {
            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(v.getId() + "", "");
        switch (v.getId()) {
            case R.id.authButton:
                sectionViewModel.authenticateApplication(getApplicationContext(), onDisconnected);
                if(BuildConfig.FLAVOR.equals("mock")) {
                    Navigation.findNavController(v).navigate(R.id.listHomeFragment);
                }
                break;
            case R.id.new_section_add_section:
                String sectionTitle = (String) object;
                sectionViewModel.addSection(sectionTitle);
                break;
            case R.id.taskDeleteButton:
                listViewModel.deleteTask((Task) object);
                break;
            case R.id.addNewTask:
                String taskDesc = (String) object;
                listViewModel.addTask(taskDesc);
                break;
            case R.id.taskCheckbox:
                listViewModel.updateTask((Task) object);
                break;
            case R.id.todoListItem:
                Bundle task = new Bundle();
                task.putParcelable("task", (Task) object);
                Navigation.findNavController(v).navigate(R.id.todoItemDetailsFragment, task);
                break;
            case R.id.list_home_card_layout:
                Bundle listDetails = new Bundle();
                listDetails.putParcelable("listInfo", (TodoList) object);
                Navigation.findNavController(v).navigate(R.id.listFragment, listDetails);
                break;
            case R.id.addSectionButton:
                showAddSectionDialog();
                break;
            case R.id.addTaskButton:
                showAddTaskDialog();
                break;
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(BuildConfig.FLAVOR.equals("mock")) {
            menu.add(Menu.NONE, 3241, Menu.NONE, getString(R.string.simulate_disconnect));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(BuildConfig.FLAVOR.equals("mock")) {
            if (!connected) {
                menu.getItem(0).setEnabled(false);
            } else {
                menu.getItem(0).setEnabled(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 3241) {
            if(connected) {
                sectionViewModel.disconnect();
            }
        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.my_nav_host_fragment).navigateUp();
    }

    @Override
    public void onBackPressed() {
        listViewModel.clearList();
        super.onBackPressed();
    }

    public void setActionBarText(String text) {
        getActionBar().setTitle(text);
    }

    public void showActionBarBack(boolean displayed) {
        getSupportActionBar().setDisplayShowHomeEnabled(displayed);
    }

    public void showAddSectionDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        View dialogView = View.inflate(this, R.layout.new_section_dialog, null);

        final EditText newSectionText = dialogView.findViewById(R.id.new_section_edit_text);
        newSectionText.setOnFocusChangeListener((v, hasFocus) -> newSectionText.post(() -> {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newSectionText, InputMethodManager.SHOW_IMPLICIT);
        }));
        newSectionText.requestFocus();
        Button addNewSection = dialogView.findViewById(R.id.new_section_add_section);
        Button cancelAddSection = dialogView.findViewById(R.id.new_section_cancel);
        newSectionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewSection.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cancelAddSection.setOnClickListener(view -> dialogBuilder.dismiss());
        addNewSection.setOnClickListener(view -> {
            String taskText = newSectionText.getText().toString();
            onFragmentInteraction(view, taskText);
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void showAddTaskDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        View dialogView = View.inflate(this, R.layout.new_task_dialog, null);

        final EditText newTaskText = dialogView.findViewById(R.id.newTaskText);
        newTaskText.setOnFocusChangeListener((v, hasFocus) -> newTaskText.post(() -> {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newTaskText, InputMethodManager.SHOW_IMPLICIT);
        }));
        newTaskText.requestFocus();
        Button addNewTask = dialogView.findViewById(R.id.addNewTask);
        Button cancelAddTask = dialogView.findViewById(R.id.cancelAddTask);

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewTask.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cancelAddTask.setOnClickListener(view -> dialogBuilder.dismiss());
        addNewTask.setOnClickListener(view -> {
            String taskText = newTaskText.getText().toString();
            onFragmentInteraction(view, taskText);
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }


}
