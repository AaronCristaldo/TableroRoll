package org.demoforge.tableroroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.demoforge.tableroroll.R;

import java.util.ArrayList;
import java.util.List;

public class FichaListFragment extends Fragment {

    private RecyclerView rvFichas;

    private List<Ficha> listaFichas = new ArrayList<>();

    private DatabaseReference fichasRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ficha_list, container, false);
        rvFichas = view.findViewById(R.id.rvFichas);
        rvFichas.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        fichasRef = FirebaseDatabase
                .getInstance()
                .getReference("fichas")
                .child(user.getUid());

        // Leer todas las fichas
        fichasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaFichas.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Ficha f = ds.getValue(Ficha.class);
                    if (f != null) listaFichas.add(f);
                }

            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Error al cargar fichas: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
