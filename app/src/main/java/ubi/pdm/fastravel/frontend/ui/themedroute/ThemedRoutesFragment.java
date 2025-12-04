package ubi.pdm.fastravel.frontend.ui.themedroute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.routes.ThemedRoute;

public class ThemedRoutesFragment extends Fragment {

    private List<ThemedRoute> routes;
    private ThemedRoutesController.OnRouteClickListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_themedroutes, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.routesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ThemedRoutesController adapter = new ThemedRoutesController(routes, listener);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
