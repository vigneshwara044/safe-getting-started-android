package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
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
        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        listHomeView.setLayoutManager(mLayoutManager);
        listHomeView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(1), true));
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

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(final int spanCount, final int spacing, final boolean includeEdge) {
            super();
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(final Rect rect, final View view, final RecyclerView parent,
                                   final RecyclerView.State state) {
            final int position = parent.getChildAdapterPosition(view);
            final int column = position % spanCount;

            if (includeEdge) {
                rect.left = spacing - column * spacing / spanCount;
                rect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    rect.top = spacing;
                }
                rect.bottom = spacing;
            } else {
                rect.left = column * spacing / spanCount;
                rect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    rect.top = spacing;
                }
            }
        }
    }

    private int dpToPx(final int dp) {
        final Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
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
            SectionViewModel viewModel;

            MyViewHolder(final View view, final ItemClickedListener listener) {
                super(view);
                itemClickListener = listener;
                view.setOnClickListener(this);
                viewModel = ViewModelProviders.of(getActivity()).get(SectionViewModel.class);
                title = view.findViewById(R.id.list_card_title);
            }

            @Override
            public void onClick(final View view) {
                itemClickListener.onClick(view, sectionsList.get(getAdapterPosition()));
            }
        }

    }

}


