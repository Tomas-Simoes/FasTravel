package ubi.pdm.fastravel.frontend.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;

import ubi.pdm.fastravel.R;

public class FavoritesManager {

    public enum FavoriteType {
        HOME,
        WORK,
        OTHER
    }

    public interface Listener {
        void onFavoriteSelected(LatLng latLng, String label);

        void onRequestPlaceForFavorite(FavoriteType type);
    }

    private final Context context;
    private final SharedPreferences prefs;

    private final LinearLayout containerFavoritos;
    private final LinearLayout favoritoCasa;
    private final LinearLayout favoritoTrabalho;
    private final LinearLayout btnAdicionarFavorito;
    private final TextView textSubtituloCasa;
    private final TextView textSubtituloTrabalho;

    private final Listener listener;

    private FavoriteType pendingFavoriteType = null;

    public FavoritesManager(
            Context context,
            SharedPreferences prefs,
            LinearLayout containerFavoritos,
            LinearLayout favoritoCasa,
            LinearLayout favoritoTrabalho,
            LinearLayout btnAdicionarFavorito,
            TextView textSubtituloCasa,
            TextView textSubtituloTrabalho,
            Listener listener
    ) {
        this.context = context.getApplicationContext();
        this.prefs = prefs;
        this.containerFavoritos = containerFavoritos;
        this.favoritoCasa = favoritoCasa;
        this.favoritoTrabalho = favoritoTrabalho;
        this.btnAdicionarFavorito = btnAdicionarFavorito;
        this.textSubtituloCasa = textSubtituloCasa;
        this.textSubtituloTrabalho = textSubtituloTrabalho;
        this.listener = listener;
    }

    public void init() {
        setupFavouriteClicks();
        loadFavourites();
        loadExtraFavourites();
    }

    public void onStartFavoriteSelection(FavoriteType type) {
        pendingFavoriteType = type;
    }

    public boolean handlePlaceSelected(Place place) {
        if (pendingFavoriteType == null) {
            return false;
        }

        saveFavouriteInternal(pendingFavoriteType, place);
        pendingFavoriteType = null;
        return true;
    }


    public boolean existsFavourite(FavoriteType type) {
        String prefix = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";
        return prefs.getString(prefix + "name", null) != null;
    }

    public void useFavourite(FavoriteType type) {
        String prefixo = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

        String name = prefs.getString(prefixo + "name", null);
        String address = prefs.getString(prefixo + "address", null);
        float lat = prefs.getFloat(prefixo + "lat", 0f);
        float lng = prefs.getFloat(prefixo + "lng", 0f);

        if (name == null || address == null) {
            Toast.makeText(context, "Favorito não definido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            listener.onFavoriteSelected(new LatLng(lat, lng), name);
        }
    }

    public void clearFavourite(FavoriteType type) {
        String prefix = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(prefix + "id");
        editor.remove(prefix + "name");
        editor.remove(prefix + "address");
        editor.remove(prefix + "lat");
        editor.remove(prefix + "lng");
        editor.apply();

        loadFavourites();
    }


    private void setupFavouriteClicks() {

        favoritoCasa.setOnClickListener(v -> {
            if (existsFavourite(FavoriteType.HOME)) {
                useFavourite(FavoriteType.HOME);
            } else {
                if (listener != null) {
                    listener.onRequestPlaceForFavorite(FavoriteType.HOME);
                }
            }
        });

        favoritoCasa.setOnLongClickListener(v -> {
            if (!existsFavourite(FavoriteType.HOME)) return true;

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Remover Casa?")
                    .setMessage("Queres apagar o favorito Casa?")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        clearFavourite(FavoriteType.HOME);
                        Toast.makeText(context, "Casa removida dos favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        favoritoTrabalho.setOnClickListener(v -> {
            if (existsFavourite(FavoriteType.WORK)) {
                useFavourite(FavoriteType.WORK);
            } else {
                if (listener != null) {
                    listener.onRequestPlaceForFavorite(FavoriteType.WORK);
                }
            }
        });

        favoritoTrabalho.setOnLongClickListener(v -> {
            if (!existsFavourite(FavoriteType.WORK)) return true;

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Remover Trabalho?")
                    .setMessage("Queres apagar o favorito Trabalho?")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        clearFavourite(FavoriteType.WORK);
                        Toast.makeText(context, "Trabalho removido dos favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        btnAdicionarFavorito.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestPlaceForFavorite(FavoriteType.OTHER);
            }
        });
    }

    private void saveFavouriteInternal(FavoriteType type, Place place) {
        if (type == FavoriteType.OTHER) {
            addExtraFavourite(place);
        } else {
            String prefixo = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(prefixo + "id", place.getId());
            editor.putString(prefixo + "name", place.getDisplayName());
            editor.putString(prefixo + "address", place.getFormattedAddress());

            if (place.getLocation() != null) {
                editor.putFloat(prefixo + "lat", (float) place.getLocation().latitude);
                editor.putFloat(prefixo + "lng", (float) place.getLocation().longitude);
            }

            editor.apply();
        }

        loadFavourites();
        loadExtraFavourites();
        Toast.makeText(context, "Favorito guardado: " + place.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    private void addExtraFavourite(Place place) {
        if (place.getLocation() == null) return;

        int count = prefs.getInt("extra_fav_count", 0);
        String baseKey = "extra_fav_" + count + "_";

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(baseKey + "id", place.getId());
        editor.putString(baseKey + "name", place.getDisplayName());
        editor.putString(baseKey + "address", place.getShortFormattedAddress());
        editor.putFloat(baseKey + "lat", (float) place.getLocation().latitude);
        editor.putFloat(baseKey + "lng", (float) place.getLocation().longitude);
        editor.putInt("extra_fav_count", count + 1);
        editor.apply();
    }

    private void loadFavourites() {
        String homeName = prefs.getString("fav_home_name", null);

        if (homeName != null) {
            textSubtituloCasa.setText("");
            textSubtituloCasa.setTextColor(Color.parseColor("#475569"));
        } else {
            textSubtituloCasa.setText("Adicionar");
            textSubtituloCasa.setTextColor(Color.parseColor("#3B82F6"));
        }

        String workName = prefs.getString("fav_work_name", null);

        if (workName != null) {
            textSubtituloTrabalho.setText("");
            textSubtituloTrabalho.setTextColor(Color.parseColor("#475569"));
        } else {
            textSubtituloTrabalho.setText("Adicionar");
            textSubtituloTrabalho.setTextColor(Color.parseColor("#3B82F6"));
        }
    }

    private void loadExtraFavourites() {
        int count = prefs.getInt("extra_fav_count", 0);

        int indexPlus = containerFavoritos.indexOfChild(btnAdicionarFavorito);

        // limpar antigos extras
        for (int i = indexPlus - 1; i >= 0; i--) {
            View child = containerFavoritos.getChildAt(i);
            if (child != favoritoCasa && child != favoritoTrabalho) {
                containerFavoritos.removeViewAt(i);
            } else {
                break;
            }
        }

        if (count == 0) {
            return;
        }

        indexPlus = containerFavoritos.indexOfChild(btnAdicionarFavorito);

        for (int i = 0; i < count; i++) {
            String baseKey = "extra_fav_" + i + "_";
            String name = prefs.getString(baseKey + "name", null);
            String address = prefs.getString(baseKey + "address", null);
            float lat = prefs.getFloat(baseKey + "lat", 0f);
            float lng = prefs.getFloat(baseKey + "lng", 0f);

            if (name == null) continue;

            View favView = createViewExtraFavourite(i, name, address, lat, lng);
            containerFavoritos.addView(favView, indexPlus);
            indexPlus++;
        }
    }

    private void deleteExtraFavourite(int index) {
        int count = prefs.getInt("extra_fav_count", 0);
        if (index < 0 || index >= count) return;

        SharedPreferences.Editor editor = prefs.edit();

        for (int i = index; i < count - 1; i++) {
            String src = "extra_fav_" + (i + 1) + "_";
            String dst = "extra_fav_" + i + "_";

            editor.putString(dst + "id", prefs.getString(src + "id", null));
            editor.putString(dst + "name", prefs.getString(src + "name", null));
            editor.putString(dst + "address", prefs.getString(src + "address", null));
            editor.putFloat(dst + "lat", prefs.getFloat(src + "lat", 0f));
            editor.putFloat(dst + "lng", prefs.getFloat(src + "lng", 0f));
        }

        String last = "extra_fav_" + (count - 1) + "_";
        editor.remove(last + "id");
        editor.remove(last + "name");
        editor.remove(last + "address");
        editor.remove(last + "lat");
        editor.remove(last + "lng");

        editor.putInt("extra_fav_count", count - 1);
        editor.apply();
    }

    private View createViewExtraFavourite(int index, String name, String address, float lat, float lng) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 0, dpToPx(20), dpToPx(20));

        MaterialCardView card = new MaterialCardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(64), dpToPx(64));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(36));
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#E2E8F0"));
        card.setCardBackgroundColor(Color.parseColor("#F8FAFC"));

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        iconParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_star);
        icon.setColorFilter(Color.parseColor("#F97316"));

        card.addView(icon);

        TextView titulo = new TextView(context);
        titulo.setText(name);
        titulo.setTextSize(12);
        titulo.setTextColor(Color.parseColor("#475569"));
        titulo.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
        titulo.setMaxLines(1);
        titulo.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        tituloParams.topMargin = dpToPx(8);
        titulo.setLayoutParams(tituloParams);

        TextView subtitulo = new TextView(context);
        subtitulo.setText(address != null ? address : "Favorito");
        subtitulo.setTextSize(10);
        subtitulo.setTextColor(Color.parseColor("#64748B"));
        subtitulo.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitulo.setMaxLines(1);
        subtitulo.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams subtituloParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        subtituloParams.topMargin = dpToPx(2);
        subtitulo.setLayoutParams(subtituloParams);

        layout.addView(card);
        layout.addView(titulo);
        layout.addView(subtitulo);

        LatLng latLng = new LatLng(lat, lng);

        layout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteSelected(latLng, name);
            }
        });

        layout.setTag(index);

        layout.setOnLongClickListener(v -> {
            Object tag = v.getTag();
            if (!(tag instanceof Integer)) return true;

            int idx = (Integer) tag;

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Remover favorito?")
                    .setMessage(name)
                    .setPositiveButton("Remover", (dialog, which) -> {
                        deleteExtraFavourite(idx);
                        loadExtraFavourites();
                        Toast.makeText(context, "Favorito removido", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        return layout;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
