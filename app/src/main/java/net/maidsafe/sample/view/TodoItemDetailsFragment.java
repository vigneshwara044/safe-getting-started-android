package net.maidsafe.sample.view;

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

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodoItemDetailsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public TodoItemDetailsFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_item_details, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TextView taskDescription = getActivity().findViewById(R.id.task_description);
        final TextView taskStatus = getActivity().findViewById(R.id.taskStatus);
        final TextView taskDueDateTime = getActivity().findViewById(R.id.dueDateTime);

        final Task task = getArguments().getParcelable(getString(R.string.task_argument_string));
        taskDescription.setText(task.getDescription());
        taskStatus.setText(task.getComplete() ? getString(R.string.complete) : getString(R.string.incomplete));
        final SimpleDateFormat format = new SimpleDateFormat("d MMM y", Locale.getDefault());
        taskDueDateTime.setText(format.format(task.getDate()));
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
        mListener.setActionBarTitle("Task details");
        mListener.showActionBarBack(true);
    }

    public interface OnFragmentInteractionListener {
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }
}
