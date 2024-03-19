package com.SunuBtrust360_Enrol.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Properties;

/**
 * @author Cherif KASSE
 * @project SunuBtrust360_Enrol
 * @created 04/01/2024/01/2024 - 13:05
 */
public class ModifierWorkerRequest {
    private Properties properties;
    @JsonProperty("Worker ID")
    private int workerId;
    @JsonProperty("NAME")
    private String name;
    @JsonProperty("DEFAULTKEY")
    private String defalutKey;
    @JsonProperty("CRYPTOTOKEN")
    private String cryptoToken;
    @JsonProperty("IMPLEMENTATION_CLASS")
    private String implemClass;
    @JsonProperty("TYPE")
    private String type;
    @JsonProperty("SIGNERCERT")
    private String signCert;

    @JsonProperty("SIGNERCERTCHAIN")
    private String signCertChain;

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefalutKey() {
        return defalutKey;
    }

    public void setDefalutKey(String defalutKey) {
        this.defalutKey = defalutKey;
    }

    public String getCryptoToken() {
        return cryptoToken;
    }

    public void setCryptoToken(String cryptoToken) {
        this.cryptoToken = cryptoToken;
    }

    public String getImplemClass() {
        return implemClass;
    }

    public void setImplemClass(String implemClass) {
        this.implemClass = implemClass;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSignCert() {
        return signCert;
    }

    public void setSignCert(String signCert) {
        this.signCert = signCert;
    }

    public String getSignCertChain() {
        return signCertChain;
    }

    public void setSignCertChain(String signCertChain) {
        this.signCertChain = signCertChain;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
