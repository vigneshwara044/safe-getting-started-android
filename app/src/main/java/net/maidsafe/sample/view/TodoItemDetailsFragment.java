package net.maidsafe.sample.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.viewmodel.CommonViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodoItemDetailsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Task task;
    CommonViewModel viewModel;

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
        viewModel = ViewModelProviders.of(getActivity()).get(CommonViewModel.class);
        final TextView taskDescription = getActivity().findViewById(R.id.task_description);
        final TextView taskStatus = getActivity().findViewById(R.id.taskStatus);
        final TextView taskDueDateTime = getActivity().findViewById(R.id.dueDateTime);
        final Button deleteButton = getActivity().findViewById(R.id.taskDeleteButton);
        task = getArguments().getParcelable(getString(R.string.task_argument_string));
        taskDescription.setText(task.getDescription());
        if (task.getComplete()) {
            taskStatus.setText(getString(R.string.complete));
            taskStatus.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getActivity().getResources().getDrawable(R.drawable.ic_round_check, null), null);
        } else {
            taskStatus.setText(getString(R.string.incomplete));
            taskStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    getActivity().getResources().getDrawable(R.drawable.ic_round_cross, null), null);
        }
        deleteButton.setOnClickListener(v -> {
            if (viewModel.getConnected().getValue()) {
            final AlertDialog dialogBuilder = new AlertDialog.Builder(getActivity()).create();
            final View dialogView = View.inflate(getActivity(), R.layout.delete_alert, null);
            final Button confirmDelete = dialogView.findViewById(R.id.delete_task);
            final Button cancelDelete = dialogView.findViewById(R.id.delete_cancel);
            confirmDelete.setOnClickListener(v1 -> {
                onButtonPressed(v1, task);
                dialogBuilder.dismiss();
                getActivity().onBackPressed();
            });
            cancelDelete.setOnClickListener(v1 -> {
                dialogBuilder.dismiss();
            });
            dialogBuilder.setView(dialogView);
            dialogBuilder.show();
            } else {
                onButtonPressed(v, null);
            }
        });
        final SimpleDateFormat format = new SimpleDateFormat("d MMM y", Locale.getDefault());
        taskDueDateTime.setText(format.format(task.getDate()));
    }

    public void onButtonPressed(final View v, final Object object) {
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
        mListener.setActionBarTitle(task.getTitle());
        mListener.showActionBarBack(true);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object o);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }
}
