package com.project.newborn.DTO;

import com.project.newborn.resources.BusinessException;

import org.json.JSONObject;

public class TopicDTO {

    private String id, name;

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

    /**
     * Method to parse JSON data format to TopicDTO object. Used in GET requests
     * @param topicJSON
     * @return
     * @throws BusinessException
     */
    public static TopicDTO parseJSONToDTO(JSONObject topicJSON) throws BusinessException{

        TopicDTO topicDTO = new TopicDTO();
        try {
            topicDTO.setId(topicJSON.getString("id"));
            topicDTO.setName(topicJSON.getString("nombre"));
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }
        return topicDTO;
    }

}
