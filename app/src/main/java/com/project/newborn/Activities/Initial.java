package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.project.newborn.R;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.URLS;

public class Initial extends Activity {

    private Button btnStartApp;
    private EditText etIpAddress, etPort;
    private Context context;

    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.initial);

        context = this;
        btnStartApp = findViewById(R.id.btnStartApp);
        etIpAddress = findViewById(R.id.etIpAddress);
        etPort = findViewById(R.id.etPort);

        loadPreferences();
        setUpButtonStartApp();

    }

    /**
     * Method to load preferences and show it on screen for fast login
     */
    private void loadPreferences() {

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String ipAddress = preferences.getString("ipAddress", "");
        String port = preferences.getString("port", "");

        etIpAddress.setText(ipAddress);
        etPort.setText(port);
    }

    /**
     * Method to configure start app button
     */
    private void setUpButtonStartApp() {

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        btnStartApp.setOnClickListener(view -> {
            if(validateIpPort()) {
                String ipAddress = etIpAddress.getText().toString();
                String port = etPort.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ipAddress", ipAddress);
                editor.putString("port", port);
                editor.apply();
                URLS.setServerIP(ipAddress, port);

                Intent intent = new Intent(context, PatientList.class);
                startActivity(intent);

            } else {
                BusinessException exception = new BusinessException("Los campos no pueden estar vacíos", context);
                exception.showErrorMessage();
            }
        });
    }

    /**
     * Check empty fields
     * @return true if all fields are filled
     */
    private boolean validateIpPort() {

        if(TextUtils.isEmpty(etIpAddress.getText().toString()) || etIpAddress.getText() == null)
            return false;
        return !(TextUtils.isEmpty(etPort.getText().toString()) || etPort.getText() == null);
    }
}
