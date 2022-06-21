package com.project.newborn.DTO;

import com.project.newborn.resources.BusinessException;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class SensorDTO {

    private String id, number, name, incubatorId, sensorTypeId, sensorType;
    private Boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncubatorId() {
        return incubatorId;
    }

    public void setIncubatorId(String incubatorId) {
        this.incubatorId = incubatorId;
    }

    public String getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(String sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Method to parse JSON data format to SensorDTO object. Used in GET requests
     * @param sensorJSON
     * @return
     * @throws BusinessException
     */
    public static SensorDTO parseJSONToDTO(JSONObject sensorJSON) throws BusinessException {
        SensorDTO sensorDTO = new SensorDTO();
        try {
            sensorDTO.setId(sensorJSON.getString("id"));
            sensorDTO.setNumber(sensorJSON.getString("numero"));
            sensorDTO.setName(sensorJSON.getString("nombre"));
            sensorDTO.setIncubatorId(sensorJSON.getString("incubadora"));
            sensorDTO.setActive(sensorJSON.getBoolean("activo"));
            sensorDTO.setSensorTypeId(sensorJSON.getString("tipo_sensor"));
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }
        return sensorDTO;
    }

    /**
     * Method to parse SensorDTO object to RequestParam object. Used in POST requests
     * @param sensorDTO
     * @return
     * @throws BusinessException
     */
    public static RequestParams parseDTOtoRequestParams(SensorDTO sensorDTO) throws BusinessException {

        RequestParams requestParams = new RequestParams();
        try {
            if(sensorDTO.getNumber() != null)
                requestParams.put("numero", sensorDTO.getNumber());
            if(sensorDTO.getName() != null)
                requestParams.put("nombre", sensorDTO.getName());
            if(sensorDTO.getSensorTypeId() != null)
                requestParams.put("tipo_sensor", sensorDTO.getSensorTypeId());
            if(sensorDTO.getIncubatorId() != null)
                requestParams.put("incubadora", sensorDTO.getIncubatorId());

        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto");
        }

        return requestParams;
    }
}
