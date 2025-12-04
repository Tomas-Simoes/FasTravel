package ubi.pdm.fastravel.frontend.ui.themedroute;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.routes.ThemedRoute;

public class ThemedRoutesController extends RecyclerView.Adapter<ThemedRoutesController.RouteViewHolder> {

    private List<ThemedRoute> routes;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onSelect(ThemedRoute r);
        void onLearnMore(ThemedRoute r);
    }

    public ThemedRoutesController(List<ThemedRoute> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card_front, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        holder.bind(routes.get(position));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView routeImage;
        LinearLayout coloredSection;
        TextView titleFront, descriptionFront, durationFront;

        RouteViewHolder(View itemView) {
            super(itemView);
            routeImage = itemView.findViewById(R.id.routeImage);
            coloredSection = itemView.findViewById(R.id.coloredSection);
            titleFront = itemView.findViewById(R.id.titleFront);
            descriptionFront = itemView.findViewById(R.id.descriptionFront);
            durationFront = itemView.findViewById(R.id.durationFront);

            itemView.setOnClickListener(v -> {
                // Aqui vais implementar o flip para mostrar o verso
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // TODO: Implementar flip animation
                }
            });
        }

        void bind(ThemedRoute route) {
            titleFront.setText(route.getTitle());
            descriptionFront.setText(route.getDescription());
            durationFront.setText(route.getDuration());

            coloredSection.setBackgroundColor(Color.parseColor(route.getColor()));

            if (route.getImageResId() != 0) {
                routeImage.setImageResource(route.getImageResId());
            }
        }
    }
}