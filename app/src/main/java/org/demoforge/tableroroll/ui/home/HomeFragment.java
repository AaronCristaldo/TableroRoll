package org.demoforge.tableroroll.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.demoforge.tableroroll.GridActivity;
import org.demoforge.tableroroll.R;
import org.demoforge.tableroroll.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout y asignar el ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializa el ViewModel (si lo necesitas)
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Botón para abrir la cuadrícula
        binding.btnOpenGrid.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GridActivity.class);
            startActivity(intent);
        });

        // Botón para acceder al listado de fichas

        // Botón para crear ficha directamente
        binding.btnCrearFitxa.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_homeFragment_to_characterSheetFragment);
        });

        // Botón para crear tablero (NUEVO)
        binding.btnCrearTablero.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_homeFragment_to_crearTableroFragment);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar fugas de memoria
    }
}
