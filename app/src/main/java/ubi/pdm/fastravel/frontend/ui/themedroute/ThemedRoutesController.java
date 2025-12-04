package ubi.pdm.fastravel.frontend.ui.themedroute;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

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
        View front, back;
        TextView titleFront, descriptionFront;

        boolean flipped = false;

        RouteViewHolder(View itemView) {
            super(itemView);

            front = itemView.findViewById(R.id.frontCard);
            titleFront = itemView.findViewById(R.id.titleFront);

            itemView.setOnClickListener(v -> flipCard());
        }

        void bind(ThemedRoute r) {
            titleFront.setText(r.getTitle());
            descriptionFront.setText(r.getDescription());
        }

        void flipCard() {
            Animation out = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.card_flip_out);
            Animation in = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.card_flip_in);

            front.startAnimation(out);
        }
    }
}
