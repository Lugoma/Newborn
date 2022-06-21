package com.project.newborn.resources;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.project.newborn.R;

public class UtilViews {

    /**
     * Method to set up TextView columns in table
     * @param context
     * @param text
     * @return
     */
    public static TableRow setUpTextView(Context context, String text, Integer textSize, Integer textColor) {

        TableRow layout = new TableRow(context);
        layout.setBackgroundResource(R.color.black);
        layout.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        TextView textView = new TextView(context);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(1,1,1,1);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        textView.setGravity(Gravity.START);
        textView.setBackgroundColor(Color.WHITE);
        layout.addView(textView);

        return layout;
    }

    /**
     * Method to set up Go To button in table
     * @param context
     * @param text
     * @param id
     * @return
     */
    public static Button setUpGoToButton(Context context, String text, String id, Class class_) {

        Button button = new Button(context);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.setMargins(3,3,3,3);
        button.setLayoutParams(layoutParams);
        button.setText(text);
        button.setHeight(100);
        button.setWidth(100);
        button.setBackgroundColor(Color.RED);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(context, class_);
            intent.putExtra("id", id);
            context.startActivity(intent);
        });

        return button;
    }
}
