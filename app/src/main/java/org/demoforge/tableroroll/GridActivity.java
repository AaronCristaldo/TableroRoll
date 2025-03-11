package org.demoforge.tableroroll;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import androidx.appcompat.app.AppCompatActivity;

public class GridActivity extends AppCompatActivity {
    private FrameLayout gridContainer;
    private static final int BASE_CELL_SIZE = 60; // 🔹 Tamaño de cada celda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);

        gridContainer = findViewById(R.id.gridContainer);

        int numFilas = 50; // 🔹 Ahora será 80x80
        int numColumnas = 50;

        GridTableView gridTableView = new GridTableView(GridActivity.this, BASE_CELL_SIZE);
        TableLayout gridTable = gridTableView.createGrid(numFilas, numColumnas);

        gridContainer.addView(gridTable);
    }
}
