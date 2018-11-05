package net.maidsafe.sample.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.viewmodel.SectionViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodoItemDetailsFragment extends Fragment {

    private SectionViewModel viewModel;
    private OnFragmentInteractionListener mListener;

    public TodoItemDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SectionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_item_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TextView taskDescription = getActivity().findViewById(R.id.task_description);
        final TextView taskStatus = getActivity().findViewById(R.id.taskStatus);
        final TextView taskDueDateTime = getActivity().findViewById(R.id.dueDateTime);

        Task task = getArguments().getParcelable(getString(R.string.task_argument_string));
        taskDescription.setText(task.getDescription());
        taskStatus.setText(task.getComplete()? getString(R.string.complete):getString(R.string.incomplete));
        SimpleDateFormat format = new SimpleDateFormat("d MMM y", Locale.getDefault());
        taskDueDateTime.setText(format.format(task.getDate()));
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

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("Task details");
        mListener.showActionBarBack(true);
    }

    public interface OnFragmentInteractionListener {
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }
}
