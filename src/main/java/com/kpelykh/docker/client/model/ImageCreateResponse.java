package com.kpelykh.docker.client.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parse reponses from /images/create
 *
 * @author Ryan Campbell (ryan.campbell@gmail.com)
 *
 */
public class ImageCreateResponse {

    @JsonProperty("status")
    private String id;


    public String getId() {
        return id;
    }


    @Override
    public String toString() {
        return "ContainerCreateResponse{" +
                "id='" + id + '\'' +
                '}';
    }
}
