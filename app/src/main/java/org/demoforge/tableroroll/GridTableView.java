package org.demoforge.tableroroll;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

public class GridTableView {
    private Context context;
    private int cellSize;
    private Random random;

    public GridTableView(Context context, int cellSize) {
        this.context = context;
        this.cellSize = cellSize;
        this.random = new Random();
    }

    public TableLayout createGrid(int rows, int cols) {
        TableLayout tableLayout = new TableLayout(context);

        // Calcular el tamaño total de la tabla en base a las filas y columnas
        int totalWidth = cols * cellSize;
        int totalHeight = rows * cellSize;

        // Establecer el tamaño del layout de la tabla
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                totalWidth,  // El ancho total
                totalHeight  // El alto total
        ));

        tableLayout.setBackgroundColor(Color.BLACK);

        // Generar las filas y columnas
        for (int i = 0; i < rows; i++) {
            TableRow tableRow = new TableRow(context);
            for (int j = 0; j < cols; j++) {
                TextView textView = new TextView(context);

                // Generar un número aleatorio del 1 al 10
                int randomNumber = random.nextInt(10) + 1;
                textView.setText(String.valueOf(randomNumber));

                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
                textView.setWidth(cellSize);
                textView.setHeight(cellSize);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(18); // Aumentamos el tamaño del texto para que sea más visible

                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }

        return tableLayout;
    }
}
