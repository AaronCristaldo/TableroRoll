package org.demoforge.tableroroll;

import android.app.Activity;
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
    private int selectedImageType = -1; // Tipo de imagen seleccionada
    private Uri imageUri; // URI de la imagen seleccionada

    public CrearTableroFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_tablero, container, false);

        // Botones para seleccionar tipo de imagen
        Button btnImagen1 = view.findViewById(R.id.btnImagen715x1000);
        Button btnImagen2 = view.findViewById(R.id.btnImagen2000x1430);
        Button btnImagen3 = view.findViewById(R.id.btnImagen1000x1000);

        // Manejadores de clic para cada botón (Abrir selector de imágenes)
        btnImagen1.setOnClickListener(v -> seleccionarImagen(1, "Imagen 715x1000 requerida para formato vertical."));
        btnImagen2.setOnClickListener(v -> seleccionarImagen(2, "Imagen 2000x1430 recomendada para uso en ancho."));
        btnImagen3.setOnClickListener(v -> seleccionarImagen(3, "Imagen 1000x1000 utilizada para formatos cuadrados."));

        return view;
    }

    private void seleccionarImagen(int imageType, String mensaje) {
        selectedImageType = imageType;
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();

        // Abrir el selector de imágenes
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            validarYSubirImagen();
        }
    }

    private void validarYSubirImagen() {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            boolean isValid = false;

            // Validar dimensiones según el tipo seleccionado
            if (selectedImageType == 1 && width == 715 && height == 1000) {
                isValid = true;
            } else if (selectedImageType == 2 && width == 2000 && height == 1430) {
                isValid = true;
            } else if (selectedImageType == 3 && width == 1000 && height == 1000) {
                isValid = true;
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "La imagen no cumple con los requisitos de tamaño.", Toast.LENGTH_LONG).show();
                return;
            }

            // Convertir a Base64
            String base64Image = convertirBase64(bitmap);

            // Guardar en Firebase Realtime Database
            subirImagenAFirebase(base64Image);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al procesar la imagen.", Toast.LENGTH_LONG).show();
        }
    }

    private String convertirBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void subirImagenAFirebase(String base64Image) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("imagenes");

        String key = ref.push().getKey(); // Genera una clave única para la imagen

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("tipo", selectedImageType);
        imageData.put("imagenBase64", base64Image);

        assert key != null;
        ref.child(key).setValue(imageData).addOnSuccessListener(aVoid -> {
            Toast.makeText(requireContext(), "Imagen guardada en Firebase.", Toast.LENGTH_SHORT).show();
            abrirGridActivity();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error al guardar la imagen.", Toast.LENGTH_SHORT).show();
        });
    }

    private void abrirGridActivity() {
        Intent intent = new Intent(getActivity(), GridActivity.class);
        intent.putExtra("IMAGE_TYPE", selectedImageType);
        startActivity(intent);
    }
}
