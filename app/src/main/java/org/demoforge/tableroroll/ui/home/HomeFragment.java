package org.demoforge.tableroroll.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

        // Inicializa el ViewModel, si lo necesitas
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Botón para abrir la cuadrícula
        binding.btnOpenGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GridActivity.class);
                startActivity(intent);
            }
        });

        binding.btnCrearFitxa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Usando NavController para navegar a CharacterSheetFragment
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_homeFragment_to_characterSheetFragment);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar fugas de memoria
    }
}
