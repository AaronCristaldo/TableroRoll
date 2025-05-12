
package org.demoforge.tableroroll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridActivity extends AppCompatActivity {

    private static final int BASE_CELL_SIZE = 80;
    private boolean playerSelected = false;

    private Spinner spinnerMaps;
    private ImageView mapImage;
    private FrameLayout gridContainer;

    private ArrayList<MapEntry> mapas = new ArrayList<>();
    private ArrayAdapter<MapEntry> adapter;

    // Jugador
    private Player player;
    private ImageView playerView;
    private int currentRows, currentCols;

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ArrayList<Player> jugadores = new ArrayList<>();
    private ArrayList<ImageView> jugadorViews = new ArrayList<>();
    private Player jugadorSeleccionado = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);

        spinnerMaps = findViewById(R.id.spinnerMaps);
        mapImage = findViewById(R.id.mapImage);
        gridContainer = findViewById(R.id.gridContainer);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Spinner
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                mapas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaps.setAdapter(adapter);
        spinnerMaps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                MapEntry mapa = mapas.get(pos);
                mostrarMapaYGrid(mapa);

                new android.os.Handler().postDelayed(() -> {
                    mostrarDialogoCrearJugador(mapa);
                }, 300);
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });

        databaseReference.child("imagenes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        mapas.clear();
                        for (DataSnapshot ds : snap.getChildren()) {
                            Integer tipo = ds.child("tipo").getValue(Integer.class);
                            String nombre = ds.child("nombre").getValue(String.class);
                            if (tipo != null && nombre != null)
                                mapas.add(new MapEntry(ds.getKey(), tipo, nombre));
                        }
                        adapter.notifyDataSetChanged();
                        if (!mapas.isEmpty()) spinnerMaps.setSelection(0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        Toast.makeText(GridActivity.this,
                                "Error cargando mapas", Toast.LENGTH_SHORT).show();
                    }
                });

        player = new Player("Heroe", 10, 0, 0, R.drawable.player_sprite);
        playerView = new ImageView(this);
        playerView.setImageResource(player.getSpriteResId());
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(
                BASE_CELL_SIZE, BASE_CELL_SIZE);
        gridContainer.addView(playerView, plp);

        playerView.setOnClickListener(v -> {
            playerSelected = true;
            Toast.makeText(this, "Jugador seleccionado. Toca una celda para moverlo.", Toast.LENGTH_SHORT).show();
        });

        gridContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && jugadorSeleccionado != null) {
                float touchX = event.getX();
                float touchY = event.getY();

                int cellX = (int)(touchX / BASE_CELL_SIZE);
                int cellY = (int)(touchY / BASE_CELL_SIZE);

                cellX = Math.max(0, Math.min(currentCols - 1, cellX));
                cellY = Math.max(0, Math.min(currentRows - 1, cellY));

                jugadorSeleccionado.setPosition(cellX, cellY);

                int index = jugadores.indexOf(jugadorSeleccionado);
                if (index != -1) {
                    ImageView view = jugadorViews.get(index);
                    view.setX(cellX * BASE_CELL_SIZE);
                    view.setY(cellY * BASE_CELL_SIZE);
                }

                jugadorSeleccionado = null;
                return true;
            }
            return false;
        });
    }

    private void mostrarMapaYGrid(MapEntry entry) {
        databaseReference.child("imagenes")
                .child(entry.key)
                .child("imagenBase64")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        String b64 = ds.getValue(String.class);
                        if (b64 == null) return;
                        Bitmap bmp = convertirBase64ABitmap(b64);

                        switch (entry.tipo) {
                            case 1:
                                currentRows = 33;
                                currentCols = 23;
                                break;
                            case 2:
                                currentRows = 47;
                                currentCols = 65;
                                break;
                            default:
                                currentRows = 33;
                                currentCols = 33;
                                break;
                        }

                        int tw = currentCols * BASE_CELL_SIZE;
                        int th = currentRows * BASE_CELL_SIZE;
                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, tw, th, true);

                        ViewGroup.LayoutParams lpMap = mapImage.getLayoutParams();
                        if (lpMap == null) {
                            lpMap = new FrameLayout.LayoutParams(tw, th);
                        } else {
                            lpMap.width = tw;
                            lpMap.height = th;
                        }
                        mapImage.setLayoutParams(lpMap);
                        mapImage.setImageBitmap(scaled);

                        reconstruirGridParaTipo(entry.tipo);

                        playerView.setX(player.getX() * BASE_CELL_SIZE);
                        playerView.setY(player.getY() * BASE_CELL_SIZE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                    }
                });
        safeAddView(gridContainer, playerView);
    }
    private void safeAddView(ViewGroup parent, View child) {
        if (child.getParent() != null) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        parent.addView(child);
    }

    private void reconstruirGridParaTipo(int tipo) {
        gridContainer.removeAllViews(); // Limpiar las vistas previas

        int f, c;
        switch (tipo) {
            case 1: f = 33; c = 23; break;
            case 2: f = 47; c = 65; break;
            default: f = 33; c = 33; break;
        }

        // Asegúrate de agregar mapImage de manera segura
        safeAddView(gridContainer, mapImage);

        // Crea la cuadrícula y agrégala de manera segura
        GridTableView gtv = new GridTableView(this, BASE_CELL_SIZE);
        TableLayout tabla = gtv.createGrid(f, c);
        safeAddView(gridContainer, tabla);

        // Añadir todos los jugadores
        for (int i = 0; i < jugadores.size(); i++) {
            Player p = jugadores.get(i);
            ImageView iv = jugadorViews.get(i);
            iv.setX(p.getX() * BASE_CELL_SIZE);
            iv.setY(p.getY() * BASE_CELL_SIZE);
            safeAddView(gridContainer, iv); // Agregar jugadores de manera segura
        }
    }


    private Bitmap convertirBase64ABitmap(String s) {
        byte[] d = Base64.decode(s, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(d, 0, d.length);
    }

    private void mostrarDialogoCrearJugador(MapEntry mapa) {
        // No se ha modificado, sigue igual
    }

    private String convertirDrawableABase64(int drawableResId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableResId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void crearJugadorEnFirebase(String nombre, int vida, String imagenBase64, MapEntry mapa) {
        // No se ha modificado, sigue igual
    }
}
