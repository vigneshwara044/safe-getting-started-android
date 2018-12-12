package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.services.ItemClickedListener;
import net.maidsafe.sample.viewmodel.ListViewModel;
import net.maidsafe.sample.model.Task;

import java.util.List;

public class ListFragment extends Fragment {

    FloatingActionButton addTaskButton;
    ListAdapter taskListAdapter;
    List<Task> taskList;
    private ListViewModel viewModel;
    private OnFragmentInteractionListener mListener;
    private RecyclerView taskListView;
    private TextView noDataText;


    public ListFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(ListViewModel.class);
        viewModel.setListDetails(getArguments().getParcelable("listInfo"));

        final Observer<List<Task>> taskObserver = tasks -> taskListAdapter.updateList(tasks);
        final Observer<Boolean> connectedObserver = connected -> {
                if (connected) {
                    viewModel.prepareList();
                }
                taskListAdapter.setConnected(connected);
        };

        viewModel.getTaskList().observe(this, taskObserver);
        viewModel.getConnected().observe(this, connectedObserver);

    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_list, container, false);

        taskListView = view.findViewById(R.id.taskListView);
        noDataText = view.findViewById(R.id.no_data_list);
        addTaskButton = view.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> {
            onButtonPressed(v, null);
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        taskListView.setLayoutManager(layoutManager);
        taskList = viewModel.getTaskList().getValue();
        taskListAdapter = new ListAdapter(taskList);
        taskListView.setAdapter(taskListAdapter);
        taskListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                taskList = taskListAdapter.getTaskList();
                updateView();
            }
        });
        updateView();
        return view;
    }

    public void updateView() {
        if (taskList.isEmpty()) {
            taskListView.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
        } else {
            taskListView.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.GONE);
        }
    }

    public void onButtonPressed(final View v, final Object object) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, object);
        }
    }

    public void onItemClicked(final View v, final Object object) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, object);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new java.lang.RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.getConnected().getValue()) {
            viewModel.prepareList();
        } else {
            onButtonPressed(new View(getContext()), null);
        }
        mListener.setActionBarTitle(viewModel.getListInfo().getListTitle());
        mListener.showActionBarBack(true);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object object);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private List<Task> taskList;
        private boolean connected = true;

        ListAdapter(final List<Task> taskList) {
            super();
            this.taskList = taskList;
        }

        private List<Task> getTaskList() {
            return taskList;
        }

        public void updateList(final List<Task> tasks) {
            this.taskList = tasks;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            final ItemClickedListener listener = (itemView, task) -> {
                onItemClicked(itemView, task);
            };
            return new ViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            holder.checkBox.setEnabled(connected);
            holder.checkBox.setChecked(taskList.get(position).getComplete());
            holder.taskDescription.setText(taskList.get(position).getDescription());

        }

        @Override
        public int getItemCount() {
            int size;
            if (taskList != null) {
                size = taskList.size();
            } else {
                size = 0;
            }
            return size;
        }

        public void setConnected(final Boolean isConnected) {
            this.connected = isConnected;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ItemClickedListener itemClickListener;
            CheckBox checkBox;
            TextView taskDescription;
            Button deleteButton;

            public ViewHolder(final View view, final ItemClickedListener listener) {
                super(view);
                itemClickListener = listener;
                view.setOnClickListener(this);
                this.checkBox = view.findViewById(R.id.taskCheckbox);
                checkBox.setOnClickListener(v -> {
                        onButtonPressed(v, taskList.get(getAdapterPosition()));
                });
                this.taskDescription = view.findViewById(R.id.taskDescription);
                this.deleteButton = view.findViewById(R.id.taskDeleteButton);
                this.deleteButton.setOnClickListener(v -> onButtonPressed(v, taskList.get(getAdapterPosition())));
            }

            @Override
            public void onClick(final View view) {
                itemClickListener.onClick(view, taskList.get(getAdapterPosition()));
            }
        }

    }

}
