package org.demoforge.tableroroll.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.demoforge.tableroroll.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharacterSheetFragment extends Fragment {
    // --- Firebase ---
    private FirebaseAuth auth;
    private DatabaseReference fichasRef;
    private String nombreOriginal;

    // --- UI Básica ---
    private TextView tvTituloFicha, tvAtributos, tvSalvaciones, tvBonificadorCompetencia;
    private EditText etNombre, etXP, etNivel;
    private Spinner spinnerClases, spinnerRazas, spinnerAlineamientos, spinnerAntecedentes;
    private Button buttonGuardarFicha;

    // --- Atributos ---
    private EditText etFuerza, etDestreza, etConstitucion, etInteligencia, etSabiduria, etCarisma;

    // --- Salvaciones ---
    private EditText etSalvacionFue, etSalvacionDes, etSalvacionCon, etSalvacionInt, etSalvacionSab, etSalvacionCar;
    private ToggleButton tbSalvacionFue, tbSalvacionDes, tbSalvacionCon, tbSalvacionInt, tbSalvacionSab, tbSalvacionCar;

    // --- Habilidades ---
    private EditText etAtletismo, etAcrobacias, etJuegoManos, etSigilo;
    private EditText etArcanos, etHistoria, etInvestigacion, etNaturaleza, etReligion;
    private EditText etMedicina, etPercepcion, etPerspicacia, etSupervivencia, etTratoAnimales;
    private EditText etEngano, etInterpretacion, etIntimidacion, etPersuasion;
    private ToggleButton tbAtletismo, tbAcrobacias, tbJuegoManos, tbSigilo;
    private ToggleButton tbArcanos, tbHistoria, tbInvestigacion, tbNaturaleza, tbReligion;
    private ToggleButton tbMedicina, tbPercepcion, tbPerspicacia, tbSupervivencia, tbTratoAnimales;
    private ToggleButton tbEngano, tbInterpretacion, tbIntimidacion, tbPersuasion;

    // --- Volley ---
    private RequestQueue requestQueue;
    private static final String URL_CLASSES    = "https://www.dnd5eapi.co/api/classes";
    private static final String URL_RACES      = "https://www.dnd5eapi.co/api/races";
    private static final String URL_ALIGNMENTS = "https://www.dnd5eapi.co/api/alignments";

    // --- Base values ---
    private int baseAtletismo, baseAcrobacias, baseJuegoManos, baseSigilo;
    private int baseArcanos, baseHistoria, baseInvestigacion, baseNaturaleza, baseReligion;
    private int baseMedicina, basePercepcion, basePerspicacia, baseSupervivencia, baseTratoAnimales;
    private int baseEngano, baseInterpretacion, baseIntimidacion, basePersuasion;

    // --- Traducciones API → Español ---
    private static final Map<String,String> traducciones = new HashMap<String,String>() {{
        put("Barbarian","Bárbaro"); put("Bard","Bardo"); put("Cleric","Clérigo");
        put("Druid","Druida"); put("Fighter","Guerrero"); put("Monk","Monje");
        put("Paladin","Paladín"); put("Ranger","Explorador"); put("Rogue","Pícaro");
        put("Sorcerer","Hechicero"); put("Warlock","Brujo"); put("Wizard","Mago");
        put("Dwarf","Enano"); put("Elf","Elfo"); put("Halfling","Mediano");
        put("Human","Humano"); put("Gnome","Gnomo"); put("Half-Elf","Medio elfo");
        put("Half-Orc","Medio orco"); put("Tiefling","Tiflin"); put("Dragonborn","Dracónido");
        put("Lawful Good","Legal bueno"); put("Neutral Good","Neutral bueno");
        put("Chaotic Good","Caótico bueno"); put("Lawful Neutral","Legal neutral");
        put("True Neutral","Neutral puro"); put("Chaotic Neutral","Caótico neutral");
        put("Lawful Evil","Legal maligno"); put("Neutral Evil","Neutral maligno");
        put("Chaotic Evil","Caótico maligno");
    }};

    public CharacterSheetFragment() {
        // Constructor vacío
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ficha, container, false);

        // 1) Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        } else {
            fichasRef = FirebaseDatabase
                    .getInstance()
                    .getReference("fichas")
                    .child(user.getUid());
        }

        // 2) Indexar vistas básicas
        etNombre          = view.findViewById(R.id.etNombre);
        etXP              = view.findViewById(R.id.etXP);
        etNivel           = view.findViewById(R.id.etNivel);
        tvTituloFicha     = view.findViewById(R.id.tvTituloFicha);
        tvAtributos       = view.findViewById(R.id.tvAtributos);
        tvSalvaciones     = view.findViewById(R.id.tvSalvaciones);
        tvBonificadorCompetencia = view.findViewById(R.id.tvBonificadorCompetencia);
        spinnerClases     = view.findViewById(R.id.spinnerClases);
        spinnerRazas      = view.findViewById(R.id.spinnerRazas);
        spinnerAlineamientos = view.findViewById(R.id.spinnerAlineamientos);
        spinnerAntecedentes  = view.findViewById(R.id.spinnerAntecedentes);
        buttonGuardarFicha = view.findViewById(R.id.btnGuardarFicha);

        // 3) Indexar atributos
        etFuerza       = view.findViewById(R.id.etFuerza);
        etDestreza     = view.findViewById(R.id.etDestreza);
        etConstitucion = view.findViewById(R.id.etConstitucion);
        etInteligencia = view.findViewById(R.id.etInteligencia);
        etSabiduria    = view.findViewById(R.id.etSabiduria);
        etCarisma      = view.findViewById(R.id.etCarisma);

        // 4) Indexar salvaciones
        etSalvacionFue = view.findViewById(R.id.etSalvacionFue);
        etSalvacionDes = view.findViewById(R.id.etSalvacionDes);
        etSalvacionCon = view.findViewById(R.id.etSalvacionCon);
        etSalvacionInt = view.findViewById(R.id.etSalvacionInt);
        etSalvacionSab = view.findViewById(R.id.etSalvacionSab);
        etSalvacionCar = view.findViewById(R.id.etSalvacionCar);

        tbSalvacionFue = view.findViewById(R.id.tbSalvacionFue);
        tbSalvacionDes = view.findViewById(R.id.tbSalvacionDes);
        tbSalvacionCon = view.findViewById(R.id.tbSalvacionCon);
        tbSalvacionInt = view.findViewById(R.id.tbSalvacionInt);
        tbSalvacionSab = view.findViewById(R.id.tbSalvacionSab);
        tbSalvacionCar = view.findViewById(R.id.tbSalvacionCar);

        // 5) Indexar habilidades
        etAtletismo     = view.findViewById(R.id.etAtletismo);
        etAcrobacias    = view.findViewById(R.id.etAcrobacias);
        etJuegoManos    = view.findViewById(R.id.etJuegoManos);
        etSigilo        = view.findViewById(R.id.etSigilo);
        etArcanos       = view.findViewById(R.id.etArcanos);
        etHistoria      = view.findViewById(R.id.etHistoria);
        etInvestigacion = view.findViewById(R.id.etInvestigacion);
        etNaturaleza    = view.findViewById(R.id.etNaturaleza);
        etReligion      = view.findViewById(R.id.etReligion);
        etMedicina      = view.findViewById(R.id.etMedicina);
        etPercepcion    = view.findViewById(R.id.etPercepcion);
        etPerspicacia   = view.findViewById(R.id.etPerspicacia);
        etSupervivencia = view.findViewById(R.id.etSupervivencia);
        etTratoAnimales = view.findViewById(R.id.etTratoAnimales);
        etEngano        = view.findViewById(R.id.etEngano);
        etInterpretacion= view.findViewById(R.id.etInterpretacion);
        etIntimidacion  = view.findViewById(R.id.etIntimidacion);
        etPersuasion    = view.findViewById(R.id.etPersuasion);

        tbAtletismo     = view.findViewById(R.id.tbAtletismo);
        tbAcrobacias    = view.findViewById(R.id.tbAcrobacias);
        tbJuegoManos    = view.findViewById(R.id.tbJuegoManos);
        tbSigilo        = view.findViewById(R.id.tbSigilo);
        tbArcanos       = view.findViewById(R.id.tbArcanos);
        tbHistoria      = view.findViewById(R.id.tbHistoria);
        tbInvestigacion = view.findViewById(R.id.tbInvestigacion);
        tbNaturaleza    = view.findViewById(R.id.tbNaturaleza);
        tbReligion      = view.findViewById(R.id.tbReligion);
        tbMedicina      = view.findViewById(R.id.tbMedicina);
        tbPercepcion    = view.findViewById(R.id.tbPercepcion);
        tbPerspicacia   = view.findViewById(R.id.tbPerspicacia);
        tbSupervivencia = view.findViewById(R.id.tbSupervivencia);
        tbTratoAnimales = view.findViewById(R.id.tbTratoAnimales);
        tbEngano        = view.findViewById(R.id.tbEngano);
        tbInterpretacion= view.findViewById(R.id.tbInterpretacion);
        tbIntimidacion  = view.findViewById(R.id.tbIntimidacion);
        tbPersuasion    = view.findViewById(R.id.tbPersuasion);

        // 6) RequestQueue de Volley
        requestQueue = Volley.newRequestQueue(requireContext());
        fetchSpinnerData(URL_CLASSES, spinnerClases);
        fetchSpinnerData(URL_RACES, spinnerRazas);
        fetchSpinnerData(URL_ALIGNMENTS, spinnerAlineamientos);
        // Antecedentes estático
        ArrayList<String> BGS = new ArrayList<>();
        BGS.add("Acólito"); BGS.add("Charlatán"); BGS.add("Criminal");
        BGS.add("Artista"); BGS.add("Héroe popular"); BGS.add("Artesano de gremio");
        BGS.add("Ermitaño"); BGS.add("Noble"); BGS.add("Forastero");
        BGS.add("Sabio"); BGS.add("Soldado"); BGS.add("Polizón");
        ArrayAdapter<String> bgAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, BGS);
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAntecedentes.setAdapter(bgAdapter);

        // 7) TextWatchers atributos → salvaciones y habilidades
        agregarTextWatcher(etFuerza, bonus -> {
            etSalvacionFue.setText(String.valueOf(bonus));
            baseAtletismo = bonus;
            updateAbility(etAtletismo, baseAtletismo, tbAtletismo.isChecked());
        });
        agregarTextWatcher(etDestreza, bonus -> {
            etSalvacionDes.setText(String.valueOf(bonus));
            baseAcrobacias = baseJuegoManos = baseSigilo = bonus;
            updateAbility(etAcrobacias, baseAcrobacias, tbAcrobacias.isChecked());
            updateAbility(etJuegoManos, baseJuegoManos, tbJuegoManos.isChecked());
            updateAbility(etSigilo, baseSigilo, tbSigilo.isChecked());
        });
        agregarTextWatcher(etConstitucion, bonus -> etSalvacionCon.setText(String.valueOf(bonus)));
        agregarTextWatcher(etInteligencia, bonus -> {
            etSalvacionInt.setText(String.valueOf(bonus));
            baseArcanos = baseHistoria = baseInvestigacion = baseNaturaleza = baseReligion = bonus;
            updateAbility(etArcanos, baseArcanos, tbArcanos.isChecked());
            updateAbility(etHistoria, baseHistoria, tbHistoria.isChecked());
            updateAbility(etInvestigacion, baseInvestigacion, tbInvestigacion.isChecked());
            updateAbility(etNaturaleza, baseNaturaleza, tbNaturaleza.isChecked());
            updateAbility(etReligion, baseReligion, tbReligion.isChecked());
        });
        agregarTextWatcher(etSabiduria, bonus -> {
            etSalvacionSab.setText(String.valueOf(bonus));
            baseMedicina = basePercepcion = basePerspicacia = baseSupervivencia = baseTratoAnimales = bonus;
            updateAbility(etMedicina, baseMedicina, tbMedicina.isChecked());
            updateAbility(etPercepcion, basePercepcion, tbPercepcion.isChecked());
            updateAbility(etPerspicacia, basePerspicacia, tbPerspicacia.isChecked());
            updateAbility(etSupervivencia, baseSupervivencia, tbSupervivencia.isChecked());
            updateAbility(etTratoAnimales, baseTratoAnimales, tbTratoAnimales.isChecked());
        });
        agregarTextWatcher(etCarisma, bonus -> {
            etSalvacionCar.setText(String.valueOf(bonus));
            baseEngano = baseInterpretacion = baseIntimidacion = basePersuasion = bonus;
            updateAbility(etEngano, baseEngano, tbEngano.isChecked());
            updateAbility(etInterpretacion, baseInterpretacion, tbInterpretacion.isChecked());
            updateAbility(etIntimidacion, baseIntimidacion, tbIntimidacion.isChecked());
            updateAbility(etPersuasion, basePersuasion, tbPersuasion.isChecked());
        });

        // 8) Nivel → competencia + refresco
        etNivel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                actualizarBonificadorCompetencia();
                // refrescar todas...
                updateAbility(etAtletismo, baseAtletismo, tbAtletismo.isChecked());
                // ... (igual que antes)
            }
        });

        // 9) Toggles
        setupSavingThrowToggle(tbSalvacionFue, etSalvacionFue);
        // … resto de toggles de salvaciones …
        tbAtletismo.setOnCheckedChangeListener((b,c)->updateAbility(etAtletismo, baseAtletismo, c));
        // … resto de toggles de habilidades …

        // 10) Carga para edición si existe argumento
        if (getArguments() != null && getArguments().containsKey("nombreFicha")) {
            nombreOriginal = getArguments().getString("nombreFicha");
            fichasRef.child(nombreOriginal)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snap) {
                            @SuppressWarnings("unchecked")
                            Map<String,Object> m = (Map<String,Object>)snap.getValue();
                            if (m!=null) {
                                etNombre.setText((String)m.get("nombreFicha"));
                                etNivel.setText(String.valueOf(m.get("nivel")));
                                // … rellena atributos y demás desde el mapa …
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e){}
                    });
        }

        // 11) Guardar/editar
        buttonGuardarFicha.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String nivelStr = etNivel.getText().toString().trim();
            if (nombre.isEmpty() || nivelStr.isEmpty()) {
                Toast.makeText(getContext(),"Completa todos los campos",Toast.LENGTH_SHORT).show();
                return;
            }
            int nivel = Integer.parseInt(nivelStr);
            // Cuenta y guarda:
            fichasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    long count = snap.getChildrenCount();
                    if (nombreOriginal==null && count>=5) {
                        Toast.makeText(getContext(),"Máximo 5 fichas",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String,Object> mapa = new HashMap<>();
                    mapa.put("nombreFicha", nombre);
                    mapa.put("nivel", nivel);
                    mapa.put("xp", etXP.getText().toString().trim());
                    mapa.put("class", spinnerClases.getSelectedItem().toString());
                    mapa.put("race", spinnerRazas.getSelectedItem().toString());
                    mapa.put("alignment", spinnerAlineamientos.getSelectedItem().toString());
                    mapa.put("background", spinnerAntecedentes.getSelectedItem().toString());
                    mapa.put("proficiencyBonus", tvBonificadorCompetencia.getText().toString().trim());
                    // … añade todos los atributos, salvaciones y habilidades al mapa …

                    if (nombreOriginal==null) {
                        fichasRef.child(nombre).setValue(mapa)
                                .addOnCompleteListener(t->{
                                    if(t.isSuccessful()){
                                        Toast.makeText(getContext(),"Ficha creada",Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    }else Toast.makeText(getContext(),"Error al guardar",Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        if (!nombre.equals(nombreOriginal)) {
                            fichasRef.child(nombreOriginal).removeValue();
                        }
                        fichasRef.child(nombre).setValue(mapa)
                                .addOnCompleteListener(t->{
                                    if(t.isSuccessful()){
                                        Toast.makeText(getContext(),"Ficha actualizada",Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    }else Toast.makeText(getContext(),"Error al actualizar",Toast.LENGTH_SHORT).show();
                                });
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError e){}
            });
        });

        return view;
    }

    // --- Helpers copiados de antes ---

    private void fetchSpinnerData(String url, final Spinner spinner) {
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        ArrayList<String> list = new ArrayList<>();
                        for (int i = 0; i < results.length(); i++) {
                            String name = results.getJSONObject(i).getString("name");
                            list.add(traducciones.getOrDefault(name, name));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                list
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),"Error parseando JSON",Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(),"Error conectando API",Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    private void agregarTextWatcher(EditText et, OnAttributeChanged cb) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                int v=0;
                try { v=Integer.parseInt(s.toString()); } catch(Exception ignored){}
                cb.onChanged(v);
            }
        });
    }

    private void actualizarBonificadorCompetencia() {
        int nivel=1;
        try { nivel=Integer.parseInt(etNivel.getText().toString()); }
        catch(Exception ignored){}
        int b= nivel<=4?2: nivel<=8?3: nivel<=12?4: nivel<=16?5: nivel<=20?6:2;
        tvBonificadorCompetencia.setText("+"+b);
    }

    private int getCompetenceBonus() {
        String t = tvBonificadorCompetencia.getText().toString().replace("+","");
        try {return Integer.parseInt(t);}catch(Exception e){return 2;}
    }

    private void updateAbility(EditText et, int base, boolean prof) {
        et.setText(String.valueOf(prof?base+getCompetenceBonus():base));
    }

    private void setupSavingThrowToggle(ToggleButton tb, EditText et) {
        tb.setOnCheckedChangeListener((b,on)->{
            int base=0;
            try{ base=Integer.parseInt(et.getText().toString()); } catch(Exception ignored){}
            int pb = getCompetenceBonus();
            et.setText(String.valueOf(on?base+pb:base-pb));
        });
    }

    private interface OnAttributeChanged {
        void onChanged(int bonus);
    }
}
