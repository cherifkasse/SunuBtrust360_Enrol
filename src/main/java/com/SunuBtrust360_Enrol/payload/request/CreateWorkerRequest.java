package com.SunuBtrust360_Enrol.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Properties;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 04/01/2024/01/2024 - 11:04
 */
public class CreateWorkerRequest {
    private Properties properties;
    @JsonProperty("Worker ID")
    private String workerId;

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Implementation Class")
    private String implemClass;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImplemClass() {
        return implemClass;
    }

    public void setImplemClass(String implemClass) {
        this.implemClass = implemClass;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
