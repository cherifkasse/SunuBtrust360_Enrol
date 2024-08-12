
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour uploadSignerCertificate complex type.
 * 
 * <p>Le fragment de schema suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="uploadSignerCertificate"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="signerId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="signerCert" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uploadSignerCertificate", propOrder = {
    "signerId",
    "signerCert",
    "scope"
})
public class UploadSignerCertificate {

    protected int signerId;
    protected byte[] signerCert;
    protected String scope;

    /**
     * Obtient la valeur de la propri�t� signerId.
     * 
     */
    public int getSignerId() {
        return signerId;
    }

    /**
     * D�finit la valeur de la propri�t� signerId.
     * 
     */
    public void setSignerId(int value) {
        this.signerId = value;
    }

    /**
     * Obtient la valeur de la propri�t� signerCert.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSignerCert() {
        return signerCert;
    }

    /**
     * D�finit la valeur de la propri�t� signerCert.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSignerCert(byte[] value) {
        this.signerCert = value;
    }

    /**
     * Obtient la valeur de la propri�t� scope.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * D�finit la valeur de la propri�t� scope.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String value) {
        this.scope = value;
    }

}
