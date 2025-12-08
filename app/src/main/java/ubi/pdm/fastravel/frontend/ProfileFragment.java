package ubi.pdm.fastravel.frontend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import ubi.pdm.fastravel.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        /*
        TextView btnEdit = view.findViewById(R.id.btn_edit);
        TextView btnPreferences = view.findViewById(R.id.btn_preferences);
        TextView btnLogout = view.findViewById(R.id.btn_logout);

        btnEdit.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile", Toast.LENGTH_SHORT).show());

        btnPreferences.setOnClickListener(v ->
                Toast.makeText(getContext(), "Preferences", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v ->
                Toast.makeText(getContext(), "Logout", Toast.LENGTH_SHORT).show());
*/
        return view;
    }
}
