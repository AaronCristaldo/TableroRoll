package org.demoforge.tableroroll.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

    // Vistas de información básica
    private EditText etNombre, etXP;
    private Spinner spinnerClases, spinnerRazas, spinnerAlineamientos, spinnerAntecedentes;
    private TextView tvTituloFicha, tvAtributos, tvSalvaciones;

    // NUEVO: Vistas para nivel y bonificador de competencia
    private EditText etNivel;
    private TextView tvBonificadorCompetencia;

    // Vistas de atributos
    private EditText etFuerza, etDestreza, etConstitucion, etInteligencia, etSabiduria, etCarisma;

    // Vistas de salvaciones (6)
    private EditText etSalvacionFue, etSalvacionDes, etSalvacionCon, etSalvacionInt, etSalvacionSab, etSalvacionCar;

    // Vistas de habilidades
    // Habilidades basadas en Fuerza
    private EditText etAtletismo;
    // Habilidades basadas en Destreza
    private EditText etAcrobacias, etJuegoManos, etSigilo;
    // Habilidades basadas en Inteligencia
    private EditText etArcanos, etHistoria, etInvestigacion, etNaturaleza, etReligion;
    // Habilidades basadas en Sabiduría
    private EditText etMedicina, etPercepcion, etPerspicacia, etSupervivencia, etTratoAnimales;
    // Habilidades basadas en Carisma
    private EditText etEngano, etInterpretacion, etIntimidacion, etPersuasion;

    // ToggleButtons para las habilidades
    private ToggleButton tbAtletismo, tbAcrobacias, tbJuegoManos, tbSigilo;
    private ToggleButton tbArcanos, tbHistoria, tbInvestigacion, tbNaturaleza, tbReligion;
    private ToggleButton tbMedicina, tbPercepcion, tbPerspicacia, tbSupervivencia, tbTratoAnimales;
    private ToggleButton tbEngano, tbInterpretacion, tbIntimidacion, tbPersuasion;

    // Binding (opcional, se declara pero seguimos usando findViewById)
    private FragmentFichaBinding binding;

    // URLs de la API
    private static final String URL_CLASSES = "https://www.dnd5eapi.co/api/classes";
    private static final String URL_RACES = "https://www.dnd5eapi.co/api/races";
    private static final String URL_ALIGNMENTS = "https://www.dnd5eapi.co/api/alignments";
    private static final String URL_BACKGROUNDS = "https://www.dnd5eapi.co/api/backgrounds";

    // RequestQueue para Volley
    private RequestQueue requestQueue;

    // Variables para almacenar el valor base de cada habilidad (sin competencia)
    private int baseAtletismo, baseAcrobacias, baseJuegoManos, baseSigilo;
    private int baseArcanos, baseHistoria, baseInvestigacion, baseNaturaleza, baseReligion;
    private int baseMedicina, basePercepcion, basePerspicacia, baseSupervivencia, baseTratoAnimales;
    private int baseEngano, baseInterpretacion, baseIntimidacion, basePersuasion;

    public CharacterSheetFragment() {
        // Constructor público vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el layout del fragment (asegúrate de tener en fragment_ficha.xml los nuevos IDs)
        View view = inflater.inflate(R.layout.fragment_ficha, container, false);

        // Inicializamos las vistas de información básica
        tvTituloFicha = view.findViewById(R.id.tvTituloFicha);
        etNombre = view.findViewById(R.id.etNombre);
        spinnerClases = view.findViewById(R.id.spinnerClases);
        spinnerRazas = view.findViewById(R.id.spinnerRazas);
        spinnerAlineamientos = view.findViewById(R.id.spinnerAlineamientos);
        spinnerAntecedentes = view.findViewById(R.id.spinnerAntecedentes);
        etXP = view.findViewById(R.id.etXP);
        tvAtributos = view.findViewById(R.id.tvAtributos);
        tvSalvaciones = view.findViewById(R.id.tvSalvaciones);

        // Inicializamos la vista de nivel y el TextView del bonificador
        etNivel = view.findViewById(R.id.etNivel);
        tvBonificadorCompetencia = view.findViewById(R.id.tvBonificadorCompetencia);

        // Inicializamos las vistas de atributos
        etFuerza = view.findViewById(R.id.etFuerza);
        etDestreza = view.findViewById(R.id.etDestreza);
        etConstitucion = view.findViewById(R.id.etConstitucion);
        etInteligencia = view.findViewById(R.id.etInteligencia);
        etSabiduria = view.findViewById(R.id.etSabiduria);
        etCarisma = view.findViewById(R.id.etCarisma);

        // Inicializamos las vistas de salvaciones (6)
        etSalvacionFue = view.findViewById(R.id.etSalvacionFue);
        etSalvacionDes = view.findViewById(R.id.etSalvacionDes);
        etSalvacionCon = view.findViewById(R.id.etSalvacionCon);
        etSalvacionInt = view.findViewById(R.id.etSalvacionInt);
        etSalvacionSab = view.findViewById(R.id.etSalvacionSab);
        etSalvacionCar = view.findViewById(R.id.etSalvacionCar);

        // Inicializamos las vistas de habilidades
        // Fuerza
        etAtletismo = view.findViewById(R.id.etAtletismo);
        // Destreza
        etAcrobacias = view.findViewById(R.id.etAcrobacias);
        etJuegoManos = view.findViewById(R.id.etJuegoManos);
        etSigilo = view.findViewById(R.id.etSigilo);
        // Inteligencia
        etArcanos = view.findViewById(R.id.etArcanos);
        etHistoria = view.findViewById(R.id.etHistoria);
        etInvestigacion = view.findViewById(R.id.etInvestigacion);
        etNaturaleza = view.findViewById(R.id.etNaturaleza);
        etReligion = view.findViewById(R.id.etReligion);
        // Sabiduría
        etMedicina = view.findViewById(R.id.etMedicina);
        etPercepcion = view.findViewById(R.id.etPercepcion);
        etPerspicacia = view.findViewById(R.id.etPerspicacia);
        etSupervivencia = view.findViewById(R.id.etSupervivencia);
        etTratoAnimales = view.findViewById(R.id.etTratoAnimales);
        // Carisma
        etEngano = view.findViewById(R.id.etEngano);
        etInterpretacion = view.findViewById(R.id.etInterpretacion);
        etIntimidacion = view.findViewById(R.id.etIntimidacion);
        etPersuasion = view.findViewById(R.id.etPersuasion);

        // Inicializamos los ToggleButtons para las habilidades
        tbAtletismo = view.findViewById(R.id.tbAtletismo);
        tbAcrobacias = view.findViewById(R.id.tbAcrobacias);
        tbJuegoManos = view.findViewById(R.id.tbJuegoManos);
        tbSigilo = view.findViewById(R.id.tbSigilo);
        tbArcanos = view.findViewById(R.id.tbArcanos);
        tbHistoria = view.findViewById(R.id.tbHistoria);
        tbInvestigacion = view.findViewById(R.id.tbInvestigacion);
        tbNaturaleza = view.findViewById(R.id.tbNaturaleza);
        tbReligion = view.findViewById(R.id.tbReligion);
        tbMedicina = view.findViewById(R.id.tbMedicina);
        tbPercepcion = view.findViewById(R.id.tbPercepcion);
        tbPerspicacia = view.findViewById(R.id.tbPerspicacia);
        tbSupervivencia = view.findViewById(R.id.tbSupervivencia);
        tbTratoAnimales = view.findViewById(R.id.tbTratoAnimales);
        tbEngano = view.findViewById(R.id.tbEngano);
        tbInterpretacion = view.findViewById(R.id.tbInterpretacion);
        tbIntimidacion = view.findViewById(R.id.tbIntimidacion);
        tbPersuasion = view.findViewById(R.id.tbPersuasion);

        // Inicializamos la RequestQueue de Volley
        requestQueue = Volley.newRequestQueue(getContext());

        // Cargamos los datos desde la API para los spinners
        fetchSpinnerData(URL_CLASSES, spinnerClases);
        fetchSpinnerData(URL_RACES, spinnerRazas);
        fetchSpinnerData(URL_ALIGNMENTS, spinnerAlineamientos);
        fetchSpinnerData(URL_BACKGROUNDS, spinnerAntecedentes);

        // Agregamos listener a spinnerRazas para obtener detalles de la raza seleccionada
        spinnerRazas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Suponiendo que el item seleccionado es el nombre de la raza,
                // se convierte a índice en minúsculas (o bien, si guardas una estructura con índice, lo usas directamente)
                String raceName = parent.getItemAtPosition(position).toString();
                String raceIndex = raceName.toLowerCase(); // Ajusta esto según tu implementación
                fetchRaceDetails(raceIndex);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Agregamos TextWatchers para que los atributos actualicen sus salvaciones y habilidades.
        // Además, se actualizan las variables base para cada habilidad.
        agregarTextWatcher(etFuerza, bonus -> {
            etSalvacionFue.setText(String.valueOf(bonus));
            baseAtletismo = bonus;
            updateAbility(etAtletismo, baseAtletismo, tbAtletismo.isChecked());
        });

        agregarTextWatcher(etDestreza, bonus -> {
            etSalvacionDes.setText(String.valueOf(bonus));
            baseAcrobacias = bonus;
            baseJuegoManos = bonus;
            baseSigilo = bonus;
            updateAbility(etAcrobacias, baseAcrobacias, tbAcrobacias.isChecked());
            updateAbility(etJuegoManos, baseJuegoManos, tbJuegoManos.isChecked());
            updateAbility(etSigilo, baseSigilo, tbSigilo.isChecked());
        });

        agregarTextWatcher(etInteligencia, bonus -> {
            etSalvacionInt.setText(String.valueOf(bonus));
            baseArcanos = bonus;
            baseHistoria = bonus;
            baseInvestigacion = bonus;
            baseNaturaleza = bonus;
            baseReligion = bonus;
            updateAbility(etArcanos, baseArcanos, tbArcanos.isChecked());
            updateAbility(etHistoria, baseHistoria, tbHistoria.isChecked());
            updateAbility(etInvestigacion, baseInvestigacion, tbInvestigacion.isChecked());
            updateAbility(etNaturaleza, baseNaturaleza, tbNaturaleza.isChecked());
            updateAbility(etReligion, baseReligion, tbReligion.isChecked());
        });

        agregarTextWatcher(etSabiduria, bonus -> {
            etSalvacionSab.setText(String.valueOf(bonus));
            baseMedicina = bonus;
            basePercepcion = bonus;
            basePerspicacia = bonus;
            baseSupervivencia = bonus;
            baseTratoAnimales = bonus;
            updateAbility(etMedicina, baseMedicina, tbMedicina.isChecked());
            updateAbility(etPercepcion, basePercepcion, tbPercepcion.isChecked());
            updateAbility(etPerspicacia, basePerspicacia, tbPerspicacia.isChecked());
            updateAbility(etSupervivencia, baseSupervivencia, tbSupervivencia.isChecked());
            updateAbility(etTratoAnimales, baseTratoAnimales, tbTratoAnimales.isChecked());
        });

        agregarTextWatcher(etCarisma, bonus -> {
            etSalvacionCar.setText(String.valueOf(bonus));
            baseEngano = bonus;
            baseInterpretacion = bonus;
            baseIntimidacion = bonus;
            basePersuasion = bonus;
            updateAbility(etEngano, baseEngano, tbEngano.isChecked());
            updateAbility(etInterpretacion, baseInterpretacion, tbInterpretacion.isChecked());
            updateAbility(etIntimidacion, baseIntimidacion, tbIntimidacion.isChecked());
            updateAbility(etPersuasion, basePersuasion, tbPersuasion.isChecked());
        });

        // Agregamos TextWatcher para el nivel y así actualizar el bonificador de competencia
        etNivel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                actualizarBonificadorCompetencia();
                // Al cambiar el bonificador, actualizamos todas las habilidades marcadas
                updateAbility(etAtletismo, baseAtletismo, tbAtletismo.isChecked());
                updateAbility(etAcrobacias, baseAcrobacias, tbAcrobacias.isChecked());
                updateAbility(etJuegoManos, baseJuegoManos, tbJuegoManos.isChecked());
                updateAbility(etSigilo, baseSigilo, tbSigilo.isChecked());
                updateAbility(etArcanos, baseArcanos, tbArcanos.isChecked());
                updateAbility(etHistoria, baseHistoria, tbHistoria.isChecked());
                updateAbility(etInvestigacion, baseInvestigacion, tbInvestigacion.isChecked());
                updateAbility(etNaturaleza, baseNaturaleza, tbNaturaleza.isChecked());
                updateAbility(etReligion, baseReligion, tbReligion.isChecked());
                updateAbility(etMedicina, baseMedicina, tbMedicina.isChecked());
                updateAbility(etPercepcion, basePercepcion, tbPercepcion.isChecked());
                updateAbility(etPerspicacia, basePerspicacia, tbPerspicacia.isChecked());
                updateAbility(etSupervivencia, baseSupervivencia, tbSupervivencia.isChecked());
                updateAbility(etTratoAnimales, baseTratoAnimales, tbTratoAnimales.isChecked());
                updateAbility(etEngano, baseEngano, tbEngano.isChecked());
                updateAbility(etInterpretacion, baseInterpretacion, tbInterpretacion.isChecked());
                updateAbility(etIntimidacion, baseIntimidacion, tbIntimidacion.isChecked());
                updateAbility(etPersuasion, basePersuasion, tbPersuasion.isChecked());
            }
        });

        // Configuramos los ToggleButtons para que, al cambiar, actualicen su habilidad correspondiente
        tbAtletismo.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etAtletismo, baseAtletismo, isChecked));
        tbAcrobacias.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etAcrobacias, baseAcrobacias, isChecked));
        tbJuegoManos.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etJuegoManos, baseJuegoManos, isChecked));
        tbSigilo.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etSigilo, baseSigilo, isChecked));
        tbArcanos.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etArcanos, baseArcanos, isChecked));
        tbHistoria.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etHistoria, baseHistoria, isChecked));
        tbInvestigacion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etInvestigacion, baseInvestigacion, isChecked));
        tbNaturaleza.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etNaturaleza, baseNaturaleza, isChecked));
        tbReligion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etReligion, baseReligion, isChecked));
        tbMedicina.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etMedicina, baseMedicina, isChecked));
        tbPercepcion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etPercepcion, basePercepcion, isChecked));
        tbPerspicacia.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etPerspicacia, basePerspicacia, isChecked));
        tbSupervivencia.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etSupervivencia, baseSupervivencia, isChecked));
        tbTratoAnimales.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etTratoAnimales, baseTratoAnimales, isChecked));
        tbEngano.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etEngano, baseEngano, isChecked));
        tbInterpretacion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etInterpretacion, baseInterpretacion, isChecked));
        tbIntimidacion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etIntimidacion, baseIntimidacion, isChecked));
        tbPersuasion.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateAbility(etPersuasion, basePersuasion, isChecked));

        return view;
    }

    /**
     * Realiza una petición a la API y actualiza el spinner pasado con los nombres extraídos.
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

    /**
     * Agrega un TextWatcher a un EditText para que, cuando se modifique su valor,
     * se invoque el callback con el bonus (valor numérico) o 0 si no se puede parsear.
     */
    private void agregarTextWatcher(EditText et, OnAttributeChanged callback) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                int bonus = 0;
                try {
                    bonus = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) { }
                callback.onChanged(bonus);
            }
        });
    }

    /**
     * Actualiza el TextView del bonificador de competencia según el nivel ingresado.
     * La tabla es:
     * 1-4   = +2
     * 5-8   = +3
     * 9-12  = +4
     * 13-16 = +5
     * 17-20 = +6
     */
    private void actualizarBonificadorCompetencia() {
        int nivel = 1;
        try {
            nivel = Integer.parseInt(etNivel.getText().toString());
        } catch (NumberFormatException e) { }
        int bonificador;
        if (nivel >= 1 && nivel <= 4) {
            bonificador = 2;
        } else if (nivel >= 5 && nivel <= 8) {
            bonificador = 3;
        } else if (nivel >= 9 && nivel <= 12) {
            bonificador = 4;
        } else if (nivel >= 13 && nivel <= 16) {
            bonificador = 5;
        } else if (nivel >= 17 && nivel <= 20) {
            bonificador = 6;
        } else {
            bonificador = 2;
        }
        tvBonificadorCompetencia.setText("+" + bonificador);
    }

    /**
     * Actualiza el valor mostrado en un EditText de habilidad.
     * Si isCompetence es true, se suma el bonificador de competencia (extraído de tvBonificadorCompetencia) al valor base;
     * si no, se muestra el valor base.
     */
    private void updateAbility(EditText abilityEditText, int baseValue, boolean isCompetence) {
        int compBonus = getCompetenceBonus();
        int newValue = isCompetence ? baseValue + compBonus : baseValue;
        abilityEditText.setText(String.valueOf(newValue));
    }

    /**
     * Extrae el bonificador de competencia a partir del TextView tvBonificadorCompetencia.
     */
    private int getCompetenceBonus() {
        String text = tvBonificadorCompetencia.getText().toString().replace("+", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    /**
     * Realiza una petición a la API para obtener los detalles de la raza seleccionada y, si tiene starting_proficiencies,
     * marca los ToggleButtons correspondientes (por ejemplo, si tiene "Skill: Perception", se marca tbPercepcion).
     *
     * @param raceIndex El índice de la raza, por ejemplo "elf".
     */
    private void fetchRaceDetails(String raceIndex) {
        String url = "https://www.dnd5eapi.co/api/races/" + raceIndex;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("starting_proficiencies")) {
                                JSONArray profArray = response.getJSONArray("starting_proficiencies");
                                // Primero, desmarcamos todos los ToggleButtons de habilidades
                                tbPercepcion.setChecked(false);
                                // Puedes agregar aquí otros botones si se espera que algunas razas tengan otras especializaciones.
                                for (int i = 0; i < profArray.length(); i++) {
                                    JSONObject prof = profArray.getJSONObject(i);
                                    String profIndex = prof.getString("index"); // Por ejemplo, "skill-perception"
                                    // Si el starting_proficiency es "skill-perception", marcamos tbPercepcion
                                    if (profIndex.equals("skill-perception")) {
                                        tbPercepcion.setChecked(true);
                                        updateAbility(etPercepcion, basePercepcion, true);
                                    }
                                    // Aquí podrías agregar más if/else para otros proficiencies de interés
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parseando detalles de la raza", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error al obtener detalles de la raza", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(request);
    }

    /**
     * Interfaz para notificar el cambio de un atributo.
     */
    private interface OnAttributeChanged {
        void onChanged(int bonus);
    }
}
