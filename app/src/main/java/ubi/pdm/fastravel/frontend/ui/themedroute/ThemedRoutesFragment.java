package ubi.pdm.fastravel.frontend.ui.themedroute;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;
import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.routes.ThemedRoute;

public class ThemedRoutesFragment extends Fragment {

    List<ThemedRoute> themedRoutes = Arrays.asList(
            new ThemedRoute(
                    1,
                    "City of Arts Walk",
                    "ic_culture",
                    "#E91E63", // Rosa/Magenta para artes
                    "Explore the city's artistic soul through museums, murals and creative hubs.",
                    "This route takes you through the Museum of Contemporary Art, street-art alleys, cultural cafés and the local theater district.",
                    "2h 15m",
                    6,
                    R.drawable.route_arts // Substitui pela tua imagem
            ),
            new ThemedRoute(
                    2,
                    "Old Town Heritage Trail",
                    "ic_history",
                    "#FF6F00", // Laranja para história
                    "Discover centuries of history through ancient streets and iconic landmarks.",
                    "Walk through medieval gates, visit the old fortress, pass restored marketplaces and learn about the foundation of the city.",
                    "3h 00m",
                    8,
                    R.drawable.route_heritage // Substitui pela tua imagem
            ),
            new ThemedRoute(
                    3,
                    "Riverside Eco Path",
                    "ic_green",
                    "#2E7D32", // Verde para natureza
                    "A peaceful ecological trail filled with parks and riverside views.",
                    "Enjoy natural stretches along the river, cross eco-bridges, visit a botanical garden and rest in shaded viewpoints.",
                    "1h 40m",
                    5,
                    R.drawable.route_eco // Substitui pela tua imagem
            ),
            new ThemedRoute(
                    4,
                    "Modern City Landmarks",
                    "ic_modern",
                    "#1565C0", // Azul para moderno
                    "A tour of the city's iconic contemporary buildings.",
                    "Discover futuristic viewpoints, innovative skyscrapers and smart-city installations.",
                    "2h 50m",
                    7,
                    R.drawable.route_modern // Substitui pela tua imagem
            )
    );

    private ThemedRoutesController.OnRouteClickListener listener = new ThemedRoutesController.OnRouteClickListener() {
        @Override
        public void onSelect(ThemedRoute r) {
            // Implementar navegação
        }

        @Override
        public void onLearnMore(ThemedRoute r) {
            // Mostrar detalhes
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_themedroutes, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.routesRecycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        ThemedRoutesController adapter = new ThemedRoutesController(themedRoutes, listener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}
