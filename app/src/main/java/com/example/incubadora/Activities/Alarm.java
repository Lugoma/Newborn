package com.example.incubadora.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.incubadora.DTO.AlarmDTO;
import com.example.incubadora.R;
import com.example.incubadora.httpInterfaces.HttpHandler;
import com.example.incubadora.resources.BusinessException;
import com.example.incubadora.resources.URLS;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Alarm extends Activity {

    private Integer alarmId, sensorId;
    private Context context;
    private EditText etAlarmMaxLimit, etAlarmMinLimit;
    private Button btnSave, btnRemove;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.alarm);

        Bundle bundleActivity = getIntent().getExtras();
        if (bundleActivity != null) {
            sensorId = bundleActivity.getInt("sensorId");
            String id = bundleActivity.getString("id");
            if (id != null)
                alarmId = Integer.parseInt(id);
        }

        context = this;
        etAlarmMaxLimit = findViewById(R.id.etAlarmMaxLimit);
        etAlarmMinLimit = findViewById(R.id.etAlarmMinLimit);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);

        setSaveAlarmButton();
        setRemoveAlarmButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alarmId != null) {
            requestAlarm();
        } else {
            btnRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Method to request all alarm data from server
     */
    private void requestAlarm() {

        String url = URLS.URL_ALARMA + "/" + alarmId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                showAlarmData(AlarmDTO.parseJSONToDTO(responseData));

            } catch (BusinessException e) {
                BusinessException exception = new BusinessException("Hay un error en los datos recuperados del servidor", context);
                exception.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all alarm data from cameraDTO
     * @param alarm
     */
    private void showAlarmData(AlarmDTO alarm) {

        etAlarmMaxLimit.setText(alarm.getMaxLimit());
        etAlarmMinLimit.setText(alarm.getMinLimit());
    }

    /**
     * Method to configure save button
     */
    private void setSaveAlarmButton() {

        btnSave.setOnClickListener(view -> {
            if(alarmId == null) {
                createNewAlarm();
            } else {
                updateAlarm();
            }
        });
    }

    /**
     * Method to create and post new alarm
     */
    private void createNewAlarm() {

        try {
            AlarmDTO alarm = collectAlarmData();
            alarm.setSensorId(String.valueOf(sensorId));
            RequestParams requestParams = AlarmDTO.parseDTOtoRequestParams(alarm);
            requestParams.put("activo", false);

            HttpHandler.post(URLS.URL_ALARMA + "/", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Toast.makeText(context, "Creado correctamente. Reinicia la aplicación para aplicar los cambios", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch (BusinessException e) {
            if(e.emptyContext())
                e.setContext(context);
            e.showErrorMessage();
        }
    }

    /**
     * Method to update alarm data
     */
    private void updateAlarm() {

        try {
            AlarmDTO alarm = collectAlarmData();
            RequestParams requestParams = AlarmDTO.parseDTOtoRequestParams(alarm);
            String relativeUrl = URLS.URL_ALARMA + "/" + alarmId;

            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Toast.makeText(context, "Guardado correctamente. Reinicia la aplicación para aplicar los cambios", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch (BusinessException e) {
            if(e.emptyContext())
                e.setContext(context);
            e.showErrorMessage();
        }
    }

    /**
     * Method to collect alarm data from interface
     * @return AlarmDTO
     * @throws BusinessException
     */
    private AlarmDTO collectAlarmData() throws BusinessException {

        AlarmDTO alarm = new AlarmDTO();

        if(validateData()) {
            if(limitsAreNumbers()) {
                alarm.setMaxLimit(etAlarmMaxLimit.getText().toString());
                alarm.setMinLimit(etAlarmMinLimit.getText().toString());
            } else {
                throw new BusinessException("Los campos deben ser números");
            }
        } else {
            throw new BusinessException("Se debe completar todos los campos");
        }
        return alarm;
    }

    /**
     * Method to validate all user-inserted data. Checks if any is empty
     * @return Boolean
     */
    private Boolean validateData() {

        if(TextUtils.isEmpty(etAlarmMaxLimit.getText().toString()))
            return false;
        return !TextUtils.isEmpty(etAlarmMinLimit.getText().toString());
    }

    /**
     * Method to validate if limits are digits. Returns false if they are not.
     * @return
     */
    private Boolean limitsAreNumbers() {

        return TextUtils.isDigitsOnly(etAlarmMaxLimit.getText())
            && TextUtils.isDigitsOnly(etAlarmMinLimit.getText());
    }

    /**
     * Configure remove alarm button
     */
    private void setRemoveAlarmButton() {

        btnRemove.setOnClickListener(view -> HttpHandler.delete(URLS.URL_ALARMA + "/" + alarmId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, "Eliminado correctamente. Reinicia la aplicación para aplicar los cambios", Toast.LENGTH_SHORT).show();
                finish();
            }
        }));
    }
}
