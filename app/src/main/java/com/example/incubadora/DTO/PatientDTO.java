package com.example.incubadora.DTO;

import android.text.TextUtils;

import com.example.incubadora.resources.BusinessException;
import com.example.incubadora.resources.DateManager;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Date;


public class PatientDTO {

    private String id, name, surname1, surname2, incubatorId;
    private Date birthDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname1() {
        return surname1;
    }

    public void setSurname1(String surname1) {
        this.surname1 = surname1;
    }

    public String getSurname2() {
        return surname2;
    }

    public void setSurname2(String surname2) {
        this.surname2 = surname2;
    }

    public String getIncubatorId() {
        return incubatorId;
    }

    public void setIncubatorId(String incubatorId) {
        this.incubatorId = incubatorId;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Method to parse JSON data format to PatientDTO object. Used in GET requests
     * @param patientJSON
     * @return
     * @throws BusinessException
     */
    public static PatientDTO parseJSONToDTO(JSONObject patientJSON) throws BusinessException{

        PatientDTO patientDTO = new PatientDTO();
        try {
            patientDTO.setId(patientJSON.getString("id"));
            patientDTO.setName(patientJSON.getString("nombre"));
            patientDTO.setSurname1(patientJSON.getString("apellido1"));
            patientDTO.setSurname2(patientJSON.getString("apellido2"));
            patientDTO.setBirthDate(DateManager.parseToDateFromServer(patientJSON.getString("fecha_nacimiento")));
            if(!patientJSON.getString("incubadora").equals("null"))
                patientDTO.setIncubatorId(patientJSON.getString("incubadora"));
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }

        return patientDTO;
    }

    /**
     * Method to parse PatientDTO object to RequestParam object. Used in POST requests
     * @param patientDTO
     * @return
     * @throws BusinessException
     */
    public static RequestParams parseDTOtoRequestParams(PatientDTO patientDTO) throws BusinessException{

        RequestParams requestParams = new RequestParams();
        try {
            if(patientDTO.getName() != null)
                requestParams.put("nombre", patientDTO.getName());
            if(patientDTO.getSurname1() != null)
                requestParams.put("apellido1", patientDTO.getSurname1());
            if(patientDTO.getSurname2() != null)
                requestParams.put("apellido2", patientDTO.getSurname2());
            if(patientDTO.getBirthDate() != null)
                requestParams.put("fecha_nacimiento", DateManager.parseDateToServerFormat(patientDTO.getBirthDate()));
            if(patientDTO.getIncubatorId() != null)
                requestParams.put("incubadora", patientDTO.getIncubatorId());

            if(!TextUtils.isEmpty(patientDTO.getId()))
                requestParams.put("id", patientDTO.getId());

        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto");
        }

        return requestParams;
    }

    /**
     * Method to return patient full name as string
     * @return name + surname1 + surname2
     */
    public String getPatientFullName() {
        return this.name + " " + this.surname1 + " " + this.surname2;
    }

}
