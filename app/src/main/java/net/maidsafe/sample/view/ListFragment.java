package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.viewmodel.TodoViewModel;
import net.maidsafe.sample.model.Task;

import java.util.List;

public class ListFragment extends Fragment {

    FloatingActionButton floatingActionButton;
    ListAdapter taskListAdapter;
    List<Task> taskList;
    private TodoViewModel viewModel;
    private OnFragmentInteractionListener mListener;
    private RecyclerView taskListView;


    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(TodoViewModel.class);
        taskList = viewModel.getTaskList().getValue();

        final Observer<List<Task>> taskObserver = tasks -> taskListAdapter.updateList(tasks);

        viewModel.getTaskList().observe(this, taskObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        taskListView = view.findViewById(R.id.taskListView);
        floatingActionButton = view.findViewById(R.id.addTaskButton);
        floatingActionButton.setOnClickListener(v -> showAddTaskDialog());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        taskListView.setLayoutManager(layoutManager);

        taskListAdapter = new ListAdapter(taskList);
        taskListView.setAdapter(taskListAdapter);


        return view;
    }

    public void showAddTaskDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getActivity()).create();
        View dialogView = View.inflate(getActivity(), R.layout.new_task_dialog, null);

        final EditText newTaskText = dialogView.findViewById(R.id.newTaskText);
        Button addNewTask = dialogView.findViewById(R.id.addNewTask);
        Button cancelAddTask = dialogView.findViewById(R.id.cancelAddTask);

        cancelAddTask.setOnClickListener(view -> dialogBuilder.dismiss());
        addNewTask.setOnClickListener(view -> {
            String taskText = newTaskText.getText().toString();
            onButtonPressed(view, taskText);
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onButtonPressed(View v, Object object) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, object);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object object);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private List<Task> taskList;

        ListAdapter(List<Task> taskList) {
            this.taskList = taskList;

        }

        public void updateList(List<Task> tasks) {
            this.taskList = tasks;
            notifyDataSetChanged();
        }

        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.checkBox.setChecked(taskList.get(position).getComplete());
            holder.taskDescription.setText(taskList.get(position).getDescription());

        }

        @Override
        public int getItemCount() {
            if (taskList != null)
                return taskList.size();
            else
                return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            CheckBox checkBox;
            TextView taskDescription;
            Button deleteButton;
            TodoViewModel viewModel;

            public ViewHolder(View view) {
                super(view);
                viewModel = ViewModelProviders.of(getActivity()).get(TodoViewModel.class);
                this.checkBox = view.findViewById(R.id.taskCheckbox);
                checkBox.setOnClickListener(v -> onButtonPressed(v, taskList.get(getAdapterPosition())));
                this.taskDescription = view.findViewById(R.id.taskDescription);
                this.deleteButton = view.findViewById(R.id.taskDeleteButton);
                this.deleteButton.setOnClickListener(v -> onButtonPressed(v, taskList.get(getAdapterPosition())));
            }

        }

    }
}
