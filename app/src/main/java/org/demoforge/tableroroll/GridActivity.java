package org.demoforge.tableroroll;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GridActivity extends AppCompatActivity {

    private FrameLayout gridContainer;
    private ImageView mapImage;
    private static final int BASE_CELL_SIZE = 80; // Tamaño base de celda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);

        gridContainer = findViewById(R.id.gridContainer);
        mapImage = findViewById(R.id.mapImage); // Referencia al ImageView

        // Obtener el tipo de imagen desde el Intent
        int imageType = getIntent().getIntExtra("IMAGE_TYPE", 1);

        // Determinar filas y columnas según el tipo de imagen
        int numFilas = 33, numColumnas = 33;

        switch (imageType) {
            case 1:
                numFilas = 33;
                numColumnas = 23;
                break;
            case 2:
                numFilas = 47;
                numColumnas = 65;
                break;
            case 3:
                numFilas = 33;
                numColumnas = 33;
                break;
            default:
                Toast.makeText(this, "Tipo de imagen desconocido", Toast.LENGTH_SHORT).show();
                break;
        }

        // Cargar la imagen desde Firebase
        cargarImagenDesdeFirebase(imageType);

        // Crear la cuadrícula
        GridTableView gridTableView = new GridTableView(GridActivity.this, BASE_CELL_SIZE);
        TableLayout gridTable = gridTableView.createGrid(numFilas, numColumnas);
        gridContainer.addView(gridTable);
    }

    private void cargarImagenDesdeFirebase(int imageType) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("imagenes");

        ref.orderByChild("tipo").equalTo(imageType).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String base64Image = data.child("imagenBase64").getValue(String.class);
                            if (base64Image != null) {
                                Bitmap bitmap = convertirBase64ABitmap(base64Image);
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mapImage.getWidth(), mapImage.getHeight(), true);
                                mapImage.setImageBitmap(scaledBitmap); // 📌 Establecer imagen escalada correctamente
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GridActivity.this, "Error al cargar la imagen.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private Bitmap convertirBase64ABitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
