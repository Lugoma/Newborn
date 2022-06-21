package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.project.newborn.DTO.IncubatorDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.URLS;
import com.project.newborn.resources.UtilViews;

import org.json.JSONArray;

public class IncubatorList extends Activity {

    private JSONArray incubatorList = new JSONArray();
    private TableLayout tblyIncubatorsList;
    private Context context;
    private Button btnAddIncubator;

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.incubator_list);
        context = this;

        btnAddIncubator = findViewById(R.id.btnAddIncubator);
        tblyIncubatorsList = findViewById(R.id.tblyIncubatorsList);

        setButtonAddIncubator();
    }

    @Override
    protected void onResume() {

        super.onResume();
        requestIncubatorList();
    }

    /**
     * Method to request all incubators in system
     */
    private void requestIncubatorList() {

        HttpHandler.arrayObjectGETRequest(URLS.URL_INCUBADORA + "/", responseData -> {
            incubatorList = responseData;
            tblyIncubatorsList.removeAllViews();
            tblyIncubatorsList.addView(setUpIncubatorListLegend());

            for(int i=0 ; i<incubatorList.length() ; ++i) {
                TableRow tableRow = new TableRow(context);

                TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                tableRowParams.setMargins(3,3,3,3);
                tableRow.setLayoutParams(tableRowParams);
                tableRow.setBackgroundResource(R.color.white);

                try {
                    IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(incubatorList.getJSONObject(i));

                    tableRow.addView(UtilViews.setUpTextView(context, incubator.getNumber(), 20, Color.BLACK));
                    tableRow.addView(UtilViews.setUpTextView(context, incubator.getName(), 12, Color.BLACK));
                    String active = incubator.isActive() ? "Activada" : "Desactivada";
                    Integer color = incubator.isActive() ? Color.BLUE : Color.BLACK;
                    tableRow.addView(UtilViews.setUpTextView(context, active, 16, color));
                    tableRow.addView(UtilViews.setUpGoToButton(context, "ir a", incubator.getId(), Incubator.class));

                    tblyIncubatorsList.addView(tableRow);

                } catch(Exception e) {
                    BusinessException exception = new BusinessException("Error al recuperar la lista de incubadoras", context);
                    exception.showErrorMessage();
                }
            }
        }, context);
    }

    /**
     * Configure incubator list table legend
     * @return
     */
    private View setUpIncubatorListLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Id",14, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Nombre",12, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Estado",12, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",12, Color.BLACK));

        return tableRow;
    }

    /**
     * Set up button add new incubator
     */
    private void setButtonAddIncubator() {

        btnAddIncubator.setOnClickListener(view -> {
            Intent intent = new Intent(context, Incubator.class);
            startActivity(intent);
        });
    }
}
