package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.project.newborn.DTO.IncubatorDTO;
import com.project.newborn.DTO.PatientDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.URLS;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AssignIncubator extends Activity {

    private JSONArray incubatorList = new JSONArray();
    private Context context;
    private Integer patientId;
    private TableLayout tblyIncubatorList;

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.assign_incubator);
        context = this;

        Bundle bundleActivity = getIntent().getExtras();
        if(bundleActivity != null) {
            String id = bundleActivity.getString("patientId");
            patientId = Integer.parseInt(id);
        }

        tblyIncubatorList = findViewById(R.id.tableIncubatorList);

        HttpHandler.arrayObjectGETRequest(URLS.URL_INCUBADORA_ACTIVA, ResponseData -> {

            incubatorList = ResponseData;
            try {
                showIncubatorList(incubatorList);
            } catch(BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all incubators found
     * @param incubatorList
     * @throws BusinessException
     */
    private void showIncubatorList(JSONArray incubatorList) throws BusinessException {

        try {
            for (int i=0; i<incubatorList.length(); ++i) {
                IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(incubatorList.getJSONObject(i));

                String relativeUrl = URLS.URL_INCUBADORA + "/" + incubator.getId() + URLS.URL_PACIENTE;
                HttpHandler.simpleObjectGETRequest(relativeUrl, responseData -> {
                    PatientDTO patient;
                    String fullName = "Disponible";
                    Integer color = Color.GREEN;
                    boolean assigned = false;

                    try {
                        patient = PatientDTO.parseJSONToDTO(responseData);
                        fullName = patient.getPatientFullName();
                        color = Color.WHITE;
                        assigned = true;
                    } catch (BusinessException ignored) {}

                    TableRow tableRow = new TableRow(context);

                    if(!assigned)
                        tableRow.setOnClickListener(view -> assignIncubatorToPatient(patientId, incubator));

                    TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    tableRowParams.setMargins(3,3,3,3);
                    tableRow.setLayoutParams(tableRowParams);
                    tableRow.setBackgroundResource(R.color.white);

                    tableRow.addView(setUpTextView(context, incubator.getNumber(), color));
                    tableRow.addView(setUpTextView(context, incubator.getName(), color));
                    tableRow.addView(setUpTextView(context, fullName, color));

                    tblyIncubatorList.addView(tableRow);

                }, context);
            }
        } catch (Exception e) {
            throw new BusinessException("Ha ocurrido un error al mostrar los datos, vuelva a intentarlo.");
        }
    }

    /**
     * Method to configure shown data
     * @param context
     * @param text
     * @param color
     * @return
     */
    private TableRow setUpTextView(Context context, String text, Integer color) {

        TableRow layout = new TableRow(context);
        layout.setBackgroundResource(R.color.black);

        TextView textView = new TextView(context);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.setMargins(1,1,1,1);
        textView.setText(text);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(20);
        textView.setGravity(Gravity.START);
        textView.setBackgroundColor(color);
        layout.addView(textView);

        return layout;
    }

    /**
     * Method to assign an incubator to a patient
     * @param patientId
     * @param incubator
     */
    private void assignIncubatorToPatient(Integer patientId, IncubatorDTO incubator) {

        PatientDTO patient = new PatientDTO();
        patient.setId(String.valueOf(patientId));
        patient.setIncubatorId(incubator.getId());
        String relativeUrl = URLS.URL_PACIENTE + "/" + patientId;

        try {
            RequestParams requestParams = PatientDTO.parseDTOtoRequestParams(patient);

            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    finish();
                }
            });

        }catch (BusinessException e) {
            BusinessException exception = new BusinessException("No se ha podido asignar la incubadora al paciente", context);
            exception.showErrorMessage();
        }
    }
}
