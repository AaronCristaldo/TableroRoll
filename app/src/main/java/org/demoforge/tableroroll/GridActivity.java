package org.demoforge.tableroroll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private static final int REQUEST_IMAGE_PICK = 1001;
    private ImageView imagenPreview;
    private String imagenSeleccionadaBase64 = null;



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
        databaseReference.child("jugadores")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Primero limpiar todos los jugadores existentes
                        limpiarJugadoresActuales();

                        jugadores.clear();
                        jugadorViews.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String mapaId = ds.child("mapaId").getValue(String.class);
                            if (mapaId == null || !mapaId.equals(getMapaActualKey())) continue;

                            String nombre = ds.child("nombre").getValue(String.class);
                            Integer vida = ds.child("vida").getValue(Integer.class);
                            Integer x = ds.child("x").getValue(Integer.class);
                            Integer y = ds.child("y").getValue(Integer.class);
                            String imagenBase64 = ds.child("imagenBase64").getValue(String.class);

                            if (nombre == null || vida == null || x == null || y == null) continue;

                            Player jugador = new Player(nombre, vida, x, y, 0);
                            jugadores.add(jugador);

                            ImageView jugadorView = new ImageView(GridActivity.this);
                            if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                                Bitmap bmp = convertirBase64ABitmap(imagenBase64);
                                jugadorView.setImageBitmap(bmp);
                            } else {
                                jugadorView.setImageResource(R.drawable.player_sprite);
                            }

                            jugadorView.setLayoutParams(new FrameLayout.LayoutParams(BASE_CELL_SIZE, BASE_CELL_SIZE));
                            jugadorView.setX(x * BASE_CELL_SIZE);
                            jugadorView.setY(y * BASE_CELL_SIZE);

                            jugadorView.setOnClickListener(v -> {
                                jugadorSeleccionado = jugador;
                                Toast.makeText(GridActivity.this, "Jugador seleccionado", Toast.LENGTH_SHORT).show();
                            });

                            jugadorView.setOnLongClickListener(v -> {
                                mostrarDialogoEliminarJugador(jugador);
                                return true;
                            });

                            gridContainer.addView(jugadorView);
                            jugadorViews.add(jugadorView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
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

                    // Actualizar posición en Firebase
                    actualizarPosicionJugadorEnFirebase(jugadorSeleccionado, cellX, cellY);
                }

                jugadorSeleccionado = null;
                return true;
            }
            return false;
        });
        FloatingActionButton fabCrearJugador = findViewById(R.id.fabCrearJugador);
        FloatingActionButton fabCrearFicha = findViewById(R.id.fabCrearFicha);

        fabCrearJugador.setOnClickListener(v -> {
            int pos = spinnerMaps.getSelectedItemPosition();
            if (pos >= 0 && pos < mapas.size()) {
                mostrarDialogoCrearJugador(mapas.get(pos));
            } else {
                Toast.makeText(this, "No hay mapa seleccionado", Toast.LENGTH_SHORT).show();
            }
        });

        fabCrearFicha.setOnClickListener(v -> {
            Toast.makeText(this, "Función para crear ficha aún no implementada", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para limpiar los jugadores actuales del gridContainer
    private void limpiarJugadoresActuales() {
        // Eliminar solo las vistas de jugadores, no el mapa ni la cuadrícula
        for (ImageView view : jugadorViews) {
            gridContainer.removeView(view);
        }
    }

    // Método para actualizar la posición del jugador en Firebase
    private void actualizarPosicionJugadorEnFirebase(Player jugador, int x, int y) {
        databaseReference.child("jugadores")
                .orderByChild("nombre")
                .equalTo(jugador.getNombre())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String mapaId = ds.child("mapaId").getValue(String.class);
                            if (mapaId != null && mapaId.equals(getMapaActualKey())) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("x", x);
                                updates.put("y", y);
                                ds.getRef().updateChildren(updates);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GridActivity.this, "Error al actualizar posición", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getMapaActualKey() {
        int pos = spinnerMaps.getSelectedItemPosition();
        if (pos >= 0 && pos < mapas.size()) {
            return mapas.get(pos).key;
        }
        return null;
    }

    private void mostrarDialogoEliminarJugador(Player jugador) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar jugador")
                .setMessage("¿Estás seguro de que quieres eliminar a " + jugador.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Buscar al jugador en Firebase usando nombre y mapaId
                    databaseReference.child("jugadores")
                            .orderByChild("nombre")
                            .equalTo(jugador.getNombre())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String mapaId = ds.child("mapaId").getValue(String.class);
                                        if (mapaId != null && mapaId.equals(getMapaActualKey())) {
                                            ds.getRef().removeValue(); // Elimina el jugador
                                            Toast.makeText(GridActivity.this, "Jugador eliminado", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    Toast.makeText(GridActivity.this, "Jugador no encontrado", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(GridActivity.this, "Error eliminando jugador", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        if (playerView.getParent() == null) {
            gridContainer.addView(playerView);
        }
    }
    private void safeAddView(ViewGroup parent, View child) {
        if (child.getParent() != null) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        parent.addView(child);
    }

    private void reconstruirGridParaTipo(int tipo) {

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
    }


    private Bitmap convertirBase64ABitmap(String s) {
        byte[] d = Base64.decode(s, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(d, 0, d.length);
    }

    private void mostrarDialogoCrearJugador(MapEntry mapa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Jugador");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del jugador");
        layout.addView(inputNombre);

        imagenPreview = new ImageView(this);
        imagenPreview.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imagenPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layout.addView(imagenPreview);

        View btnSeleccionarImagen = new View(this);
        btnSeleccionarImagen = new android.widget.Button(this);
        ((android.widget.Button) btnSeleccionarImagen).setText("Seleccionar Imagen");
        layout.addView(btnSeleccionarImagen);

        builder.setView(layout);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombre = inputNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            // Usa la imagen seleccionada o la predeterminada si no se seleccionó ninguna
            String imgBase64 = imagenSeleccionadaBase64 != null ? imagenSeleccionadaBase64 : convertirDrawableABase64(R.drawable.player_sprite);

            int vida = 10;
            crearJugadorEnFirebase(nombre, vida, imgBase64, mapa);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imagenPreview.setImageBitmap(bitmap);
                imagenSeleccionadaBase64 = convertirBitmapABase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private String convertirDrawableABase64(int drawableResId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableResId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void crearJugadorEnFirebase(String nombre, int vida, String imagenBase64, MapEntry mapa) {
        String uid = currentUser != null ? currentUser.getUid() : "anon";
        String key = databaseReference.child("jugadores").push().getKey();

        if (key == null) return;

        Map<String, Object> jugadorData = new HashMap<>();
        jugadorData.put("nombre", nombre);
        jugadorData.put("vida", vida);
        jugadorData.put("imagenBase64", imagenBase64); // Guardamos la imagen en base64
        jugadorData.put("x", 0);
        jugadorData.put("y", 0);
        jugadorData.put("mapaId", mapa.key);
        jugadorData.put("uid", uid);

        // Guardamos en Firebase
        databaseReference.child("jugadores").child(key).setValue(jugadorData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Jugador creado", Toast.LENGTH_SHORT).show();
                    // El listener de Firebase se encarga de mostrarlo.
                    imagenSeleccionadaBase64 = null; // Resetear la imagen seleccionada
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creando jugador", Toast.LENGTH_SHORT).show());
    }
}