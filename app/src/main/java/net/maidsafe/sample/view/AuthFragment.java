package net.maidsafe.sample.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.R;

public class AuthFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final String NON_MOCK = "nonMock";

    public AuthFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Button button = getActivity().findViewById(R.id.authButton);
        final TextView welcomeMessage = getActivity().findViewById(R.id.auth_welcome_message);
        if (NON_MOCK.equals(BuildConfig.FLAVOR)) {
            welcomeMessage.setText(String.format(getString(R.string.welcome_msg),
                    getString(R.string.welcome_msg_live)));
            button.setText(getString(R.string.auth_button));
        } else {
            welcomeMessage.setText(String.format(getString(R.string.welcome_msg), getString(R.string.welcome_msg_dev)));
        }
        button.setOnClickListener(this::authAction);
    }

    public void authAction(final View v) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, null);
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
        mListener.setActionBarTitle("SAFE Todo");
        mListener.showActionBarBack(false);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object object);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }
}
