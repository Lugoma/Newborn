package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.project.newborn.DTO.CameraDTO;
import com.project.newborn.DTO.IncubatorDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.VideoStreamHandler;
import com.project.newborn.resources.URLS;

public class CameraStream extends Activity {

    private LinearLayout lytCamera;
    private String address;
    private String port;
    private String incubatorId, cameraId;
    private VideoStreamHandler videoStreamHandler;
    private Context context;

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.camera_stream);
        context = this;

        Bundle bundleActivity = getIntent().getExtras();
        if(bundleActivity != null) {
            incubatorId = bundleActivity.getString("incubatorId");
            cameraId = bundleActivity.getString("cameraId");
        }

        lytCamera = findViewById((R.id.lytCamera));

        String url = URLS.URL_INCUBADORA + "/" + incubatorId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(responseData);
                address = incubator.getIpAddress();
                String relativeUrl = URLS.URL_CAMARA + "/" + cameraId;
                HttpHandler.simpleObjectGETRequest(relativeUrl, requestData -> {
                    try {
                        CameraDTO camera = CameraDTO.parseJSONToDTO(requestData);
                        port = camera.getPort();
                        setUpStream();
                    } catch (BusinessException e) {
                        BusinessException exception = new BusinessException("Se ha producido un error. Vuelva a intentarlo", context);
                        exception.showErrorMessage();
                        finish();
                    }
                }, context);
            } catch (BusinessException e) {
                BusinessException exception = new BusinessException("Se ha producido un error. Vuelva a intentarlo", context);
                exception.showErrorMessage();
                videoStreamHandler.disconnect();
                finish();
            }
        }, context);
    }

    @Override
    public void onBackPressed() {

        videoStreamHandler.disconnect();
        super.onBackPressed();
    }

    /**
     * Method to set up and start stream
     */
    private void setUpStream() {

        videoStreamHandler = new VideoStreamHandler(getApplicationContext(), URLS.parseURLWithPortTCP(address, port));
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        videoStreamHandler.setLayoutParams(tableRowParams);

        lytCamera.addView(videoStreamHandler);
    }

}
