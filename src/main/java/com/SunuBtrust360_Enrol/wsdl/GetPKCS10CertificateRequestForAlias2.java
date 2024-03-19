
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour getPKCS10CertificateRequestForAlias2 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getPKCS10CertificateRequestForAlias2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="signerId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="certReqInfo" type="{http://adminws.signserver.org/}pkcs10CertReqInfo" minOccurs="0"/&gt;
 *         &lt;element name="explicitEccParameters" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="keyAlias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPKCS10CertificateRequestForAlias2", propOrder = {
    "signerId",
    "certReqInfo",
    "explicitEccParameters",
    "keyAlias"
})
public class GetPKCS10CertificateRequestForAlias2 {

    protected int signerId;
    protected Pkcs10CertReqInfo certReqInfo;
    protected boolean explicitEccParameters;
    protected String keyAlias;

    /**
     * Obtient la valeur de la propriété signerId.
     * 
     */
    public int getSignerId() {
        return signerId;
    }

    /**
     * Définit la valeur de la propriété signerId.
     * 
     */
    public void setSignerId(int value) {
        this.signerId = value;
    }

    /**
     * Obtient la valeur de la propriété certReqInfo.
     * 
     * @return
     *     possible object is
     *     {@link Pkcs10CertReqInfo }
     *     
     */
    public Pkcs10CertReqInfo getCertReqInfo() {
        return certReqInfo;
    }

    /**
     * Définit la valeur de la propriété certReqInfo.
     * 
     * @param value
     *     allowed object is
     *     {@link Pkcs10CertReqInfo }
     *     
     */
    public void setCertReqInfo(Pkcs10CertReqInfo value) {
        this.certReqInfo = value;
    }

    /**
     * Obtient la valeur de la propriété explicitEccParameters.
     * 
     */
    public boolean isExplicitEccParameters() {
        return explicitEccParameters;
    }

    /**
     * Définit la valeur de la propriété explicitEccParameters.
     * 
     */
    public void setExplicitEccParameters(boolean value) {
        this.explicitEccParameters = value;
    }

    /**
     * Obtient la valeur de la propriété keyAlias.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Définit la valeur de la propriété keyAlias.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyAlias(String value) {
        this.keyAlias = value;
    }

}
