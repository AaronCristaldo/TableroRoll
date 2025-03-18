package org.demoforge.tableroroll.ui.home;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.demoforge.tableroroll.R;
import org.demoforge.tableroroll.databinding.FragmentFichaBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CharacterSheetFragment extends Fragment {

    // Declaración de vistas
    private EditText etNombre, etClaseNivel, etXP;
    private Spinner spinnerClases, spinnerRazas, spinnerAlineamientos, spinnerAntecedentes;
    private EditText etFuerza, etDestreza, etConstitucion, etInteligencia, etSabiduria, etCarisma;
    private EditText etSalvacionFue, etSalvacionDes, etSalvacionCon;
    private TextView tvTituloFicha, tvAtributos, tvSalvaciones;
    private FragmentFichaBinding binding; // Usa ViewBinding para el fragmento

    // URLs de la API
    private static final String URL_CLASSES = "https://www.dnd5eapi.co/api/classes";
    private static final String URL_RACES = "https://www.dnd5eapi.co/api/races";
    private static final String URL_ALIGNMENTS = "https://www.dnd5eapi.co/api/alignments";
    private static final String URL_BACKGROUNDS = "https://www.dnd5eapi.co/api/backgrounds";

    // RequestQueue para Volley
    private RequestQueue requestQueue;

    public CharacterSheetFragment() {
        // Constructor público vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el layout del fragment
        View view = inflater.inflate(R.layout.fragment_ficha, container, false);

        // Inicializamos las vistas
        tvTituloFicha = view.findViewById(R.id.tvTituloFicha);
        etNombre = view.findViewById(R.id.etNombre);
        etClaseNivel = view.findViewById(R.id.etClaseNivel);
        spinnerClases = view.findViewById(R.id.spinnerClases);
        spinnerRazas = view.findViewById(R.id.spinnerRazas);
        spinnerAlineamientos = view.findViewById(R.id.spinnerAlineamientos);
        spinnerAntecedentes = view.findViewById(R.id.spinnerAntecedentes);
        etXP = view.findViewById(R.id.etXP);

        etFuerza = view.findViewById(R.id.etFuerza);
        etDestreza = view.findViewById(R.id.etDestreza);
        etConstitucion = view.findViewById(R.id.etConstitucion);
        etInteligencia = view.findViewById(R.id.etInteligencia);
        etSabiduria = view.findViewById(R.id.etSabiduria);
        etCarisma = view.findViewById(R.id.etCarisma);

        etSalvacionFue = view.findViewById(R.id.etSalvacionFue);
        etSalvacionDes = view.findViewById(R.id.etSalvacionDes);
        etSalvacionCon = view.findViewById(R.id.etSalvacionCon);

        tvAtributos = view.findViewById(R.id.tvAtributos);
        tvSalvaciones = view.findViewById(R.id.tvSalvaciones);

        // Inicializamos la RequestQueue de Volley
        requestQueue = Volley.newRequestQueue(getContext());

        // Cargamos los datos desde la API
        fetchSpinnerData(URL_CLASSES, spinnerClases);
        fetchSpinnerData(URL_RACES, spinnerRazas);
        fetchSpinnerData(URL_ALIGNMENTS, spinnerAlineamientos);
        fetchSpinnerData(URL_BACKGROUNDS, spinnerAntecedentes);

        return view;
    }

    /**
     * Realiza una petición a la API y actualiza el spinner pasado con los nombres extraídos.
     * @param url URL del endpoint de la API.
     * @param spinner Spinner a actualizar.
     */
    private void fetchSpinnerData(String url, final Spinner spinner) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            ArrayList<String> namesList = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                String name = item.getString("name");
                                namesList.add(name);
                            }
                            // Creamos el adaptador y lo asignamos al spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, namesList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parseando JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
}
