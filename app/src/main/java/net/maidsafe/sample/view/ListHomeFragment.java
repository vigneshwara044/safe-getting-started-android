package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.ItemClickedListener;
import net.maidsafe.sample.viewmodel.SectionViewModel;

import java.util.List;

public class ListHomeFragment extends Fragment {

    private RecyclerView listHomeView;
    private TextView noDataText;
    FloatingActionButton addSectionButton;
    private ListsHomeAdapter adapter;
    private List<TodoList> sectionsList;
    private SectionViewModel viewModel;

    private OnFragmentInteractionListener mListener;

    public ListHomeFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SectionViewModel.class);

        final Observer<List<TodoList>> sectionsObserver = sections -> adapter.updateList(sections);

        viewModel.getSections().observe(this, sectionsObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("My Lists");
        mListener.showActionBarBack(false);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_list_home, container, false);
        listHomeView  = view.findViewById(R.id.list_home_view);
        noDataText = view.findViewById(R.id.no_data_section);
        addSectionButton = view.findViewById(R.id.addSectionButton);
        addSectionButton.setOnClickListener(v -> {
            onButtonPressed(v, null);
        });

        sectionsList = viewModel.getSections().getValue();
        adapter = new ListsHomeAdapter(sectionsList);
        final RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        listHomeView.setLayoutManager(mLayoutManager);
        listHomeView.setItemAnimator(new DefaultItemAnimator());
        listHomeView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                sectionsList = adapter.getSectionsList();
                updateView();
            }
        });
        updateView();
        return view;
    }

    private void updateView() {
        if (sectionsList.isEmpty()) {
            listHomeView.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
        } else {
            listHomeView.setVisibility(View.VISIBLE);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object o);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }

    class ListsHomeAdapter extends RecyclerView.Adapter<ListsHomeAdapter.MyViewHolder> {

        private List<TodoList> sectionsList;

        ListsHomeAdapter(final List<TodoList> sectionsList) {
            super();
            this.sectionsList = sectionsList;
        }

        public void updateList(final List<TodoList> sections) {
            this.sectionsList = sections;
            notifyDataSetChanged();
        }

        private List<TodoList> getSectionsList() {
            return sectionsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_home_card, parent, false);

            final ItemClickedListener listener = (itemView, task) -> {
                onItemClicked(itemView, task);
            };

            return new MyViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.title.setText(sectionsList.get(position).getListTitle());
        }

        @Override
        public int getItemCount() {
            return sectionsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ItemClickedListener itemClickListener;
            private final TextView title;

            MyViewHolder(final View view, final ItemClickedListener listener) {
                super(view);
                itemClickListener = listener;
                view.setOnClickListener(this);
                title = view.findViewById(R.id.list_card_title);
            }

            @Override
            public void onClick(final View view) {
                itemClickListener.onClick(view, sectionsList.get(getAdapterPosition()));
            }
        }

    }

}


