package com.github.hzhang.routerdemo.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.hzhang.routerdemo.R;
import com.github.hzhang.router.RouterMgr;

public class MainFragment extends Fragment {
    // private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        view.findViewById(R.id.message).setOnClickListener(
                v -> RouterMgr.route(getActivity(), "51",
                        null, null));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

}