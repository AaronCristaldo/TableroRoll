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
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tableLayout.setBackgroundColor(Color.TRANSPARENT);

        int totalWidth = cols * cellSize;
        int totalHeight = rows * cellSize;

        tableLayout.setMinimumWidth(totalWidth);
        tableLayout.setMinimumHeight(totalHeight);

        for (int i = 0; i < rows; i++) {
            TableRow tableRow = new TableRow(context);
            for (int j = 0; j < cols; j++) {
                TextView textView = new TextView(context);

                int randomNumber = random.nextInt(10) + 1;
                textView.setText(String.valueOf(randomNumber));

                textView.setBackgroundColor(Color.TRANSPARENT);
                textView.setTextColor(Color.BLACK);
                textView.setWidth(cellSize);
                textView.setHeight(cellSize);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(18);

                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }

        return tableLayout;
    }
}
