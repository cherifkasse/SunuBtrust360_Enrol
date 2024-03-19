
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour archiveEntry complex type.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="archiveEntry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="archiveData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *         &lt;element name="archiveId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="requestCertSerialNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="requestIP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="requestIssuerDN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="signerId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&gt;
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="uniqueId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archiveEntry", propOrder = {
    "archiveData",
    "archiveId",
    "requestCertSerialNumber",
    "requestIP",
    "requestIssuerDN",
    "signerId",
    "time",
    "type",
    "uniqueId"
})
public class ArchiveEntry {

    protected byte[] archiveData;
    protected String archiveId;
    protected String requestCertSerialNumber;
    protected String requestIP;
    protected String requestIssuerDN;
    protected Integer signerId;
    protected Long time;
    protected Integer type;
    protected String uniqueId;

    /**
     * Obtient la valeur de la propri�t� archiveData.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getArchiveData() {
        return archiveData;
    }

    /**
     * D�finit la valeur de la propri�t� archiveData.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setArchiveData(byte[] value) {
        this.archiveData = value;
    }

    /**
     * Obtient la valeur de la propri�t� archiveId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArchiveId() {
        return archiveId;
    }

    /**
     * D�finit la valeur de la propri�t� archiveId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArchiveId(String value) {
        this.archiveId = value;
    }

    /**
     * Obtient la valeur de la propri�t� requestCertSerialNumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestCertSerialNumber() {
        return requestCertSerialNumber;
    }

    /**
     * D�finit la valeur de la propri�t� requestCertSerialNumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestCertSerialNumber(String value) {
        this.requestCertSerialNumber = value;
    }

    /**
     * Obtient la valeur de la propri�t� requestIP.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestIP() {
        return requestIP;
    }

    /**
     * D�finit la valeur de la propri�t� requestIP.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestIP(String value) {
        this.requestIP = value;
    }

    /**
     * Obtient la valeur de la propri�t� requestIssuerDN.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestIssuerDN() {
        return requestIssuerDN;
    }

    /**
     * D�finit la valeur de la propri�t� requestIssuerDN.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestIssuerDN(String value) {
        this.requestIssuerDN = value;
    }

    /**
     * Obtient la valeur de la propri�t� signerId.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSignerId() {
        return signerId;
    }

    /**
     * D�finit la valeur de la propri�t� signerId.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSignerId(Integer value) {
        this.signerId = value;
    }

    /**
     * Obtient la valeur de la propri�t� time.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTime() {
        return time;
    }

    /**
     * D�finit la valeur de la propri�t� time.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTime(Long value) {
        this.time = value;
    }

    /**
     * Obtient la valeur de la propri�t� type.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getType() {
        return type;
    }

    /**
     * D�finit la valeur de la propri�t� type.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setType(Integer value) {
        this.type = value;
    }

    /**
     * Obtient la valeur de la propri�t� uniqueId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * D�finit la valeur de la propri�t� uniqueId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

}
