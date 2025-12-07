package ubi.pdm.fastravel.frontend.ThemedRoutesModule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.routes.ThemedRoute;

public class ThemedRoutesController extends RecyclerView.Adapter<ThemedRoutesController.RouteViewHolder> {

    private List<ThemedRoute> routes;
    private OnRouteClickListener listener;
    // Guarda o estado (Virado ou Não) de cada posição para evitar bugs no scroll
    private SparseBooleanArray flippedStates = new SparseBooleanArray();

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
        // Agora inflamos o container que tem Frente e Verso
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        holder.bind(routes.get(position), position);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {
        // Front Components
        CardView cardFront;
        ImageView routeImage;
        LinearLayout coloredSection;
        TextView titleFront, descriptionFront, durationFront;

        // Back Components
        CardView cardBack;
        LinearLayout backHeader;
        TextView titleBack, statsBack, fullRouteText;
        Button btnStartRoute;
        NestedScrollView routeScrollView;

        RouteViewHolder(View itemView) {
            super(itemView);

            cardFront = itemView.findViewById(R.id.frontCardRoot);

            routeImage = itemView.findViewById(R.id.routeImage);
            coloredSection = itemView.findViewById(R.id.coloredSection);
            titleFront = itemView.findViewById(R.id.titleFront);
            descriptionFront = itemView.findViewById(R.id.descriptionFront);
            durationFront = itemView.findViewById(R.id.durationFront);

            cardBack = itemView.findViewById(R.id.cardBack);
            backHeader = itemView.findViewById(R.id.backHeader);
            titleBack = itemView.findViewById(R.id.titleBack);
            statsBack = itemView.findViewById(R.id.statsBack);
            fullRouteText = itemView.findViewById(R.id.fullRouteText);
            btnStartRoute = itemView.findViewById(R.id.btnStartRoute);
            routeScrollView = itemView.findViewById(R.id.routeScrollView);

            float scale = itemView.getResources().getDisplayMetrics().density;
            if(cardFront != null) cardFront.setCameraDistance(8000 * scale);
            if(cardBack != null) cardBack.setCameraDistance(8000 * scale);


            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    boolean isFlipped = flippedStates.get(pos);
                    flipCard(pos, !isFlipped);
                }
            });

            // Clique no botão iniciar
            btnStartRoute.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSelect(routes.get(pos));
                }
            });

            if (routeScrollView != null) {
                routeScrollView.setOnTouchListener((v, event) -> {
                    v.getParent().requestDisallowInterceptTouchEvent(true);

                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);

                        v.performClick();
                    }

                    return false;
                });
            }
        }

        void bind(ThemedRoute route, int position) {
            // --- BIND FRONT ---
            titleFront.setText(route.getTitle());
            descriptionFront.setText(route.getPathText());
            durationFront.setText(route.getTimeText() + " • " + route.getDistanceText());
            coloredSection.setBackgroundColor(Color.parseColor(route.getColor()));

            if (route.getPhotoUrl() != null) {
                Glide.with(itemView.getContext()).load(route.getPhotoUrl()).centerCrop().into(routeImage);
            } else {
                routeImage.setImageResource(route.getImageResId());
            }

            // --- BIND BACK ---
            titleBack.setText(route.getTitle());
            statsBack.setText(route.getTimeText() + " • " + route.getDistanceText());
            backHeader.setBackgroundColor(Color.parseColor(route.getColor()));

            // Formatar a rota para ficar vertical e bonita no verso
            String formattedRoute = route.getPathText().replace(" → ", "\n  ↓  \n");
            fullRouteText.setText(formattedRoute);

            // --- ESTADO (FLIP) ---
            // Verifica se este item deve estar virado ou não
            boolean isFlipped = flippedStates.get(position);
            setCardStateInstant(isFlipped);
        }

        // Define o estado visual sem animação (para quando fazes scroll)
        private void setCardStateInstant(boolean isFlipped) {
            if (isFlipped) {
                cardFront.setAlpha(0f);
                cardFront.setVisibility(View.GONE);
                cardFront.setRotationY(180f); // Frente rodada

                cardBack.setAlpha(1f);
                cardBack.setVisibility(View.VISIBLE);
                cardBack.setRotationY(0f);    // Verso visivel
            } else {
                cardFront.setAlpha(1f);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.setRotationY(0f);   // Frente visivel

                cardBack.setAlpha(0f);
                cardBack.setVisibility(View.GONE);
                cardBack.setRotationY(180f);  // Verso rodado
            }
        }

        // Executa a animação de flip
        private void flipCard(int position, boolean toBack) {
            flippedStates.put(position, toBack);

            final View visibleView = toBack ? cardFront : cardBack;
            final View invisibleView = toBack ? cardBack : cardFront;

            // 1. Roda a view visível até 90 graus (fica invisível no 3D)
            visibleView.animate()
                    .rotationY(toBack ? 90f : -90f)
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            visibleView.setVisibility(View.GONE);

                            // 2. Prepara a view invisível
                            invisibleView.setVisibility(View.VISIBLE);
                            invisibleView.setRotationY(toBack ? -90f : 90f);
                            invisibleView.setAlpha(0f);

                            // 3. Roda a view invisível de 90 até 0
                            invisibleView.animate()
                                    .rotationY(0f)
                                    .alpha(1f)
                                    .setDuration(300)
                                    .setListener(null)
                                    .start();
                        }
                    })
                    .start();
        }
    }
}