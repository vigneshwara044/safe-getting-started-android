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

    private static final String MOCK = "mock";
    SectionViewModel sectionViewModel;
    ListViewModel listViewModel;
    ProgressBar progressBar;
    boolean connected;
    View host;
    OnDisconnected onDisconnected;
    public enum Status {

        LOADING(1),
        DONE(0),
        CONNECTED(2),
        ERROR(-1);

        private int val;

        Status(final int val) {
            this.val = val;
        }

        public int getValue() {
            return this.val;
        }
    }
    private static final int MOCK_DISCONNECT_ID = 3421;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        onDisconnected = () -> {
            connected = false;
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.disconnected_message),
                        Snackbar.LENGTH_INDEFINITE)
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
            if (status == Status.LOADING.getValue()) {
                progressBar.setVisibility(View.VISIBLE);
            } else if (status == Status.DONE.getValue()) {
                progressBar.setVisibility(View.INVISIBLE);
            } else if (status == Status.ERROR.getValue()) {
                showErrorPopUp(sectionViewModel.getErrorMessage());
            } else if (status == Status.CONNECTED.getValue()) {
                sectionViewModel.fetchSections();
            }
        };
        final Observer<Boolean> clientObserver = isConnected -> {
            connected = isConnected;
        };

        sectionViewModel.getStatus().observe(this, statusObserver);
        sectionViewModel.getConnected().observe(this, clientObserver);

        final Intent intent = getIntent();
        final Uri data = intent.getData();
        if (data != null) {
            sectionViewModel.connect(data, onDisconnected);
            Navigation.findNavController(host).navigate(R.id.listHomeFragment);
        }
    }

    private void showErrorPopUp(final String errorMessage) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        final View dialogView = View.inflate(this, R.layout.error_layout, null);
        dialogBuilder.setView(dialogView);

        final TextView errorLog = dialogView.findViewById(R.id.errorLog);
        errorLog.setText(errorMessage);
        final Button close = dialogView.findViewById(R.id.closeErrorLog);
        close.setOnClickListener(v -> {
            dialogBuilder.hide();
        });
        Navigation.findNavController(host).navigateUp();
        dialogBuilder.show();
    }

    public void onFragmentInteraction(final View v, final Object object) {
        if (!connected && v.getId() != R.id.authButton) {
            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.authButton:
                sectionViewModel.authenticateApplication(getApplicationContext(), onDisconnected);
                if (BuildConfig.FLAVOR.equals(MOCK)) {
                    Navigation.findNavController(v).navigate(R.id.listHomeFragment);
                }
                break;
            case R.id.new_section_add_section:
                final String sectionTitle = (String) object;
                sectionViewModel.addSection(sectionTitle);
                break;
            case R.id.taskDeleteButton:
                listViewModel.deleteTask((Task) object);
                break;
            case R.id.new_task_add_task:
                final String taskDesc = (String) object;
                listViewModel.addTask(taskDesc);
                break;
            case R.id.taskCheckbox:
                listViewModel.updateTask((Task) object);
                break;
            case R.id.todoListItem:
                final Bundle task = new Bundle();
                task.putParcelable("task", (Task) object);
                Navigation.findNavController(v).navigate(R.id.todoItemDetailsFragment, task);
                break;
            case R.id.list_home_card_layout:
                final Bundle listDetails = new Bundle();
                listDetails.putParcelable("listInfo", (TodoList) object);
                Navigation.findNavController(v).navigate(R.id.listFragment, listDetails);
                break;
            case R.id.addSectionButton:
                showPopUp(R.layout.new_section_dialog);
                break;
            case R.id.addTaskButton:
                showPopUp(R.layout.new_task_dialog);
                break;
            default:
                Log.i(getString(R.string.info), getString(R.string.no_action));
                break;
        }
    }

    @Override
    public void setActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (BuildConfig.FLAVOR.equals(MOCK)) {
            menu.add(Menu.NONE, MOCK_DISCONNECT_ID, Menu.NONE, getString(R.string.simulate_disconnect));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (BuildConfig.FLAVOR.equals(MOCK)) {
            if (!connected) {
                menu.getItem(0).setEnabled(false);
            } else {
                menu.getItem(0).setEnabled(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == MOCK_DISCONNECT_ID && connected) {
            sectionViewModel.disconnect();
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

    public void setActionBarText(final String text) {
        getActionBar().setTitle(text);
    }

    public void showActionBarBack(final boolean displayed) {
        getSupportActionBar().setDisplayShowHomeEnabled(displayed);
    }

    public void showPopUp(final int id) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        final View dialogView = View.inflate(this, id, null);

        EditText editText;
        Button addButton;
        Button cancelButton;

        if (id == R.layout.new_section_dialog) {
            editText = dialogView.findViewById(R.id.new_section_edit_text);
            addButton = dialogView.findViewById(R.id.new_section_add_section);
            cancelButton = dialogView.findViewById(R.id.new_section_cancel);
        } else {
            editText = dialogView.findViewById(R.id.new_task_edit_text);
            addButton = dialogView.findViewById(R.id.new_task_add_task);
            cancelButton = dialogView.findViewById(R.id.new_task_cancel);
        }
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.post(() -> {
            final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }));
        editText.requestFocus();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                // no action
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                addButton.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(final Editable s) {
                // no action
            }
        });
        cancelButton.setOnClickListener(view -> dialogBuilder.dismiss());
        addButton.setOnClickListener(view -> {
            final String taskText = editText.getText().toString();
            onFragmentInteraction(view, taskText);
            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }
}
