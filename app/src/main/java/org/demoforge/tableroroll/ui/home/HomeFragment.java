package org.demoforge.tableroroll.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.demoforge.tableroroll.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el ViewModel, si lo necesitas
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Inflar el layout y asignar el ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Aquí puedes acceder a los elementos del layout y añadir lógica, por ejemplo:
        // binding.textView.setText("Bienvenido!");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Liberar el binding para evitar posibles fugas de memoria
        binding = null;
    }
}
