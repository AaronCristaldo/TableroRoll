package org.demoforge.tableroroll;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import androidx.appcompat.app.AppCompatActivity;

public class GridActivity extends AppCompatActivity {
    private FrameLayout gridContainer;
    private static final int BASE_CELL_SIZE = 80; // 🔹 Tamaño de cada celda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);

        gridContainer = findViewById(R.id.gridContainer);

        int numFilas = 33; // 🔹 Ahora será 80x80
        int numColumnas = 33;

        GridTableView gridTableView = new GridTableView(GridActivity.this, BASE_CELL_SIZE);
        TableLayout gridTable = gridTableView.createGrid(numFilas, numColumnas);
        gridTable.setBackgroundColor(Color.TRANSPARENT);


        gridContainer.addView(gridTable);
    }
}
