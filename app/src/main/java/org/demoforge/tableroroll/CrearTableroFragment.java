package org.demoforge.tableroroll;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CrearTableroFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private int selectedImageType = -1;
    private Uri imageUri;
    private Bitmap selectedBitmap;

    public CrearTableroFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_tablero, container, false);

        Button btnImagen1 = view.findViewById(R.id.btnImagen715x1000);
        Button btnImagen2 = view.findViewById(R.id.btnImagen2000x1430);
        Button btnImagen3 = view.findViewById(R.id.btnImagen1000x1000);

        btnImagen1.setOnClickListener(v -> seleccionarImagen(1,
                "Imagen 715×1000 requerida para formato vertical."));
        btnImagen2.setOnClickListener(v -> seleccionarImagen(2,
                "Imagen 2000×1430 recomendada para uso en ancho."));
        btnImagen3.setOnClickListener(v -> seleccionarImagen(3,
                "Imagen 1000×1000 utilizada para formatos cuadrados."));

        return view;
    }

    private void seleccionarImagen(int imageType, String mensaje) {
        selectedImageType = imageType;
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE_REQUEST
                || resultCode != Activity.RESULT_OK
                || data == null
                || data.getData() == null) return;

        imageUri = data.getData();
        try {
            InputStream is = requireContext()
                    .getContentResolver()
                    .openInputStream(imageUri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();

            if (validarDimensiones(bmp, selectedImageType)) {
                selectedBitmap = bmp;
                pedirNombreUsuario();
            } else {
                Toast.makeText(requireContext(),
                        "La imagen no cumple con los requisitos de tamaño.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Error al procesar la imagen.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarDimensiones(Bitmap bmp, int tipo) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        return (tipo == 1 && w==715 && h==1000)
                || (tipo == 2 && w==2000 && h==1430)
                || (tipo == 3 && w==1000 && h==1000);
    }

    private void pedirNombreUsuario() {
        final EditText input = new EditText(requireContext());
        input.setHint("Nombre del mapa");

        new AlertDialog.Builder(requireContext())
                .setTitle("Asignar nombre")
                .setMessage("Introduce un nombre para este mapa:")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String nombre = input.getText()
                            .toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "El nombre no puede estar vacío.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        subirImagenAFirebase(nombre);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void subirImagenAFirebase(String nombre) {
        String base64 = convertirBase64(selectedBitmap);
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("imagenes")
                .push();

        Map<String,Object> data = new HashMap<>();
        data.put("tipo", selectedImageType);
        data.put("nombre", nombre);
        data.put("imagenBase64", base64);

        ref.setValue(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(requireContext(),
                            "Mapa \""+nombre+"\" guardado.",
                            Toast.LENGTH_SHORT).show();
                    abrirGridActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Error al guardar la imagen.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String convertirBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(
                baos.toByteArray(), Base64.DEFAULT);
    }

    private void abrirGridActivity() {
        Intent intent = new Intent(getActivity(),
                GridActivity.class);
        // opcional: podrías pasar selectedImageType o forzar recarga
        startActivity(intent);
    }
}
