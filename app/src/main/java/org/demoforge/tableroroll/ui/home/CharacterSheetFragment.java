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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.demoforge.tableroroll.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CharacterSheetFragment extends Fragment {

    // --- Firebase & Firestore ---
    private FirebaseAuth auth;
    private CollectionReference charsRef;

    // --- UI Básica ---
    private TextView tvTituloFicha, tvAtributos, tvSalvaciones, tvBonificadorCompetencia;
    private EditText etNombre, etXP, etNivel;
    private Spinner spinnerClases, spinnerRazas, spinnerAlineamientos, spinnerAntecedentes;
    private Button btnGuardarFicha;

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

    // --- Base values de habilidades ---
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
        // Constructor público vacío
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup    container,
                             @Nullable Bundle       savedInstanceState) {
        // 1) Inflar layout
        View view = inflater.inflate(R.layout.fragment_ficha, container, false);

        // 2) Inicializar FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            charsRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("characters");
        } else {
            Toast.makeText(getContext(),
                    "Error: usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // 3) Encontrar vistas en XML
        tvTituloFicha              = view.findViewById(R.id.tvTituloFicha);
        etNombre                   = view.findViewById(R.id.etNombre);
        etXP                       = view.findViewById(R.id.etXP);
        spinnerClases              = view.findViewById(R.id.spinnerClases);
        spinnerRazas               = view.findViewById(R.id.spinnerRazas);
        spinnerAlineamientos       = view.findViewById(R.id.spinnerAlineamientos);
        spinnerAntecedentes        = view.findViewById(R.id.spinnerAntecedentes);
        tvAtributos                = view.findViewById(R.id.tvAtributos);
        tvSalvaciones              = view.findViewById(R.id.tvSalvaciones);
        etNivel                    = view.findViewById(R.id.etNivel);
        tvBonificadorCompetencia   = view.findViewById(R.id.tvBonificadorCompetencia);
        btnGuardarFicha            = view.findViewById(R.id.btnGuardarFicha);

        etFuerza        = view.findViewById(R.id.etFuerza);
        etDestreza      = view.findViewById(R.id.etDestreza);
        etConstitucion  = view.findViewById(R.id.etConstitucion);
        etInteligencia  = view.findViewById(R.id.etInteligencia);
        etSabiduria     = view.findViewById(R.id.etSabiduria);
        etCarisma       = view.findViewById(R.id.etCarisma);

        etSalvacionFue  = view.findViewById(R.id.etSalvacionFue);
        etSalvacionDes  = view.findViewById(R.id.etSalvacionDes);
        etSalvacionCon  = view.findViewById(R.id.etSalvacionCon);
        etSalvacionInt  = view.findViewById(R.id.etSalvacionInt);
        etSalvacionSab  = view.findViewById(R.id.etSalvacionSab);
        etSalvacionCar  = view.findViewById(R.id.etSalvacionCar);

        tbSalvacionFue  = view.findViewById(R.id.tbSalvacionFue);
        tbSalvacionDes  = view.findViewById(R.id.tbSalvacionDes);
        tbSalvacionCon  = view.findViewById(R.id.tbSalvacionCon);
        tbSalvacionInt  = view.findViewById(R.id.tbSalvacionInt);
        tbSalvacionSab  = view.findViewById(R.id.tbSalvacionSab);
        tbSalvacionCar  = view.findViewById(R.id.tbSalvacionCar);

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

        // 4) Volley RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());

        // 5) Configurar Spinners
        fetchSpinnerData(URL_CLASSES,    spinnerClases);
        fetchSpinnerData(URL_RACES,      spinnerRazas);
        fetchSpinnerData(URL_ALIGNMENTS, spinnerAlineamientos);

        // Antecedentes manual en español
        ArrayList<String> bgs = new ArrayList<>();
        bgs.add("Acólito"); bgs.add("Charlatán"); bgs.add("Criminal");
        bgs.add("Artista"); bgs.add("Héroe popular"); bgs.add("Artesano de gremio");
        bgs.add("Ermitaño"); bgs.add("Noble"); bgs.add("Forastero");
        bgs.add("Sabio"); bgs.add("Soldado"); bgs.add("Polizón");
        ArrayAdapter<String> bgAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                bgs
        );
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAntecedentes.setAdapter(bgAdapter);

        // 6) TextWatchers Atributos → Salvaciones y Habilidades
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

        // 7) Nivel → Competencia + Refrescar habilidades
        etNivel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                actualizarBonificadorCompetencia();
                // refrescar todas las habilidades marcadas
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

        // 8) Toggles Salvaciones
        setupSavingThrowToggle(tbSalvacionFue, etSalvacionFue);
        setupSavingThrowToggle(tbSalvacionDes, etSalvacionDes);
        setupSavingThrowToggle(tbSalvacionCon, etSalvacionCon);
        setupSavingThrowToggle(tbSalvacionInt, etSalvacionInt);
        setupSavingThrowToggle(tbSalvacionSab, etSalvacionSab);
        setupSavingThrowToggle(tbSalvacionCar, etSalvacionCar);

        // 9) Toggles Habilidades
        tbAtletismo.setOnCheckedChangeListener((b,c)->updateAbility(etAtletismo, baseAtletismo, c));
        tbAcrobacias.setOnCheckedChangeListener((b,c)->updateAbility(etAcrobacias, baseAcrobacias, c));
        tbJuegoManos.setOnCheckedChangeListener((b,c)->updateAbility(etJuegoManos, baseJuegoManos, c));
        tbSigilo.setOnCheckedChangeListener((b,c)->updateAbility(etSigilo, baseSigilo, c));
        tbArcanos.setOnCheckedChangeListener((b,c)->updateAbility(etArcanos, baseArcanos, c));
        tbHistoria.setOnCheckedChangeListener((b,c)->updateAbility(etHistoria, baseHistoria, c));
        tbInvestigacion.setOnCheckedChangeListener((b,c)->updateAbility(etInvestigacion, baseInvestigacion, c));
        tbNaturaleza.setOnCheckedChangeListener((b,c)->updateAbility(etNaturaleza, baseNaturaleza, c));
        tbReligion.setOnCheckedChangeListener((b,c)->updateAbility(etReligion, baseReligion, c));
        tbMedicina.setOnCheckedChangeListener((b,c)->updateAbility(etMedicina, baseMedicina, c));
        tbPercepcion.setOnCheckedChangeListener((b,c)->updateAbility(etPercepcion, basePercepcion, c));
        tbPerspicacia.setOnCheckedChangeListener((b,c)->updateAbility(etPerspicacia, basePerspicacia, c));
        tbSupervivencia.setOnCheckedChangeListener((b,c)->updateAbility(etSupervivencia, baseSupervivencia, c));
        tbTratoAnimales.setOnCheckedChangeListener((b,c)->updateAbility(etTratoAnimales, baseTratoAnimales, c));
        tbEngano.setOnCheckedChangeListener((b,c)->updateAbility(etEngano, baseEngano, c));
        tbInterpretacion.setOnCheckedChangeListener((b,c)->updateAbility(etInterpretacion, baseInterpretacion, c));
        tbIntimidacion.setOnCheckedChangeListener((b,c)->updateAbility(etIntimidacion, baseIntimidacion, c));
        tbPersuasion.setOnCheckedChangeListener((b,c)->updateAbility(etPersuasion, basePersuasion, c));

        // 10) Botón Guardar
        btnGuardarFicha.setOnClickListener(v -> saveCharacter());

        return view;
    }

    private void saveCharacter() {
        final Map<String,Object> ficha = new HashMap<>();
        AtomicReference<String> name = new AtomicReference<>(etNombre.getText().toString().trim());
        String level = etNivel.getText().toString().trim();

        // Contar existentes y luego guardar
        charsRef.get()
                .addOnSuccessListener(snap -> {
                    if (name.get().isEmpty()) {
                        name.set("ficha" + (snap.size() + 1));
                    }
                    if (snap.size() >= 5 && !name.get().startsWith("ficha")) {
                        Toast.makeText(getContext(),
                                "Límite de 5 fichas alcanzado", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Llenar datos mínimos
                    ficha.put("name", name);
                    ficha.put("level", level);
                    ficha.put("xp", etXP.getText().toString().trim());
                    ficha.put("class",     spinnerClases.getSelectedItem().toString());
                    ficha.put("race",      spinnerRazas.getSelectedItem().toString());
                    ficha.put("alignment", spinnerAlineamientos.getSelectedItem().toString());
                    ficha.put("background",spinnerAntecedentes.getSelectedItem().toString());
                    ficha.put("proficiencyBonus", tvBonificadorCompetencia.getText().toString().trim());

                    ficha.put("str", etFuerza.getText().toString().trim());
                    ficha.put("dex", etDestreza.getText().toString().trim());
                    ficha.put("con", etConstitucion.getText().toString().trim());
                    ficha.put("int", etInteligencia.getText().toString().trim());
                    ficha.put("wis", etSabiduria.getText().toString().trim());
                    ficha.put("cha", etCarisma.getText().toString().trim());

                    ficha.put("saveStr_value", etSalvacionFue.getText().toString().trim());
                    ficha.put("saveStr_prof",  tbSalvacionFue.isChecked());
                    ficha.put("saveDex_value", etSalvacionDes.getText().toString().trim());
                    ficha.put("saveDex_prof",  tbSalvacionDes.isChecked());
                    ficha.put("saveCon_value", etSalvacionCon.getText().toString().trim());
                    ficha.put("saveCon_prof",  tbSalvacionCon.isChecked());
                    ficha.put("saveInt_value", etSalvacionInt.getText().toString().trim());
                    ficha.put("saveInt_prof",  tbSalvacionInt.isChecked());
                    ficha.put("saveWis_value", etSalvacionSab.getText().toString().trim());
                    ficha.put("saveWis_prof",  tbSalvacionSab.isChecked());
                    ficha.put("saveCha_value", etSalvacionCar.getText().toString().trim());
                    ficha.put("saveCha_prof",  tbSalvacionCar.isChecked());

                    ficha.put("athletics_value",   etAtletismo.getText().toString().trim());
                    ficha.put("athletics_prof",    tbAtletismo.isChecked());
                    ficha.put("acrobatics_value",  etAcrobacias.getText().toString().trim());
                    ficha.put("acrobatics_prof",   tbAcrobacias.isChecked());
                    ficha.put("sleightHand_value", etJuegoManos.getText().toString().trim());
                    ficha.put("sleightHand_prof",  tbJuegoManos.isChecked());
                    ficha.put("stealth_value",     etSigilo.getText().toString().trim());
                    ficha.put("stealth_prof",      tbSigilo.isChecked());
                    ficha.put("arcana_value",      etArcanos.getText().toString().trim());
                    ficha.put("arcana_prof",       tbArcanos.isChecked());
                    ficha.put("history_value",     etHistoria.getText().toString().trim());
                    ficha.put("history_prof",      tbHistoria.isChecked());
                    ficha.put("investigation_value", etInvestigacion.getText().toString().trim());
                    ficha.put("investigation_prof",  tbInvestigacion.isChecked());
                    ficha.put("nature_value",        etNaturaleza.getText().toString().trim());
                    ficha.put("nature_prof",         tbNaturaleza.isChecked());
                    ficha.put("religion_value",      etReligion.getText().toString().trim());
                    ficha.put("religion_prof",       tbReligion.isChecked());
                    ficha.put("medicine_value",      etMedicina.getText().toString().trim());
                    ficha.put("medicine_prof",       tbMedicina.isChecked());
                    ficha.put("perception_value",    etPercepcion.getText().toString().trim());
                    ficha.put("perception_prof",     tbPercepcion.isChecked());
                    ficha.put("insight_value",       etPerspicacia.getText().toString().trim());
                    ficha.put("insight_prof",        tbPerspicacia.isChecked());
                    ficha.put("survival_value",      etSupervivencia.getText().toString().trim());
                    ficha.put("survival_prof",       tbSupervivencia.isChecked());
                    ficha.put("animalHandling_value", etTratoAnimales.getText().toString().trim());
                    ficha.put("animalHandling_prof",  tbTratoAnimales.isChecked());
                    ficha.put("deception_value",     etEngano.getText().toString().trim());
                    ficha.put("deception_prof",      tbEngano.isChecked());
                    ficha.put("performance_value",   etInterpretacion.getText().toString().trim());
                    ficha.put("performance_prof",    tbInterpretacion.isChecked());
                    ficha.put("intimidation_value",  etIntimidacion.getText().toString().trim());
                    ficha.put("intimidation_prof",   tbIntimidacion.isChecked());
                    ficha.put("persuasion_value",    etPersuasion.getText().toString().trim());
                    ficha.put("persuasion_prof",     tbPersuasion.isChecked());

                    // Guardar en Firestore
                    charsRef.add(ficha)
                            .addOnSuccessListener(docRef -> Toast.makeText(getContext(),
                                    "Ficha guardada con éxito.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(),
                                    "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error al leer fichas existentes: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    /** TextWatcher helper **/
    private void agregarTextWatcher(EditText et, OnAttributeChanged cb) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                int val = 0;
                try { val = Integer.parseInt(s.toString()); } catch (NumberFormatException ignored){}
                cb.onChanged(val);
            }
        });
    }

    /** Fetch spinner data and translate **/
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
                        Toast.makeText(getContext(), "Error parseando JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    /** Compute and display proficiency bonus **/
    private void actualizarBonificadorCompetencia() {
        int nivel = 1;
        try { nivel = Integer.parseInt(etNivel.getText().toString()); } catch (NumberFormatException ignored){}
        int bonif;
        if      (nivel <= 4)  bonif = 2;
        else if (nivel <= 8)  bonif = 3;
        else if (nivel <= 12) bonif = 4;
        else if (nivel <= 16) bonif = 5;
        else if (nivel <= 20) bonif = 6;
        else                  bonif = 2;
        tvBonificadorCompetencia.setText("+" + bonif);
    }

    /** Read proficiency bonus **/
    private int getCompetenceBonus() {
        String t = tvBonificadorCompetencia.getText().toString().replace("+", "");
        try { return Integer.parseInt(t); } catch (NumberFormatException e) { return 2; }
    }

    /** Update a skill EditText **/
    private void updateAbility(EditText et, int baseValue, boolean withProf) {
        et.setText(String.valueOf(withProf ? baseValue + getCompetenceBonus() : baseValue));
    }

    /** Setup saving throw toggle **/
    private void setupSavingThrowToggle(ToggleButton tb, EditText etSave) {
        tb.setOnCheckedChangeListener((btn, prof) -> {
            int base = 0;
            try { base = Integer.parseInt(etSave.getText().toString()); } catch (NumberFormatException ignored){}
            int profb = getCompetenceBonus();
            etSave.setText(String.valueOf(prof ? base + profb : base - profb));
        });
    }

    /** Interface for attribute changes **/
    private interface OnAttributeChanged {
        void onChanged(int bonus);
    }

}
