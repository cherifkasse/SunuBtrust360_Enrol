
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour certReqData complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="certReqData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="armored" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="binary" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fileSuffix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "certReqData", propOrder = {
    "armored",
    "binary",
    "contentType",
    "fileSuffix"
})
public class CertReqData {

    protected String armored;
    protected byte[] binary;
    protected String contentType;
    protected String fileSuffix;

    /**
     * Obtient la valeur de la propriété armored.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArmored() {
        return armored;
    }

    /**
     * Définit la valeur de la propriété armored.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArmored(String value) {
        this.armored = value;
    }

    /**
     * Obtient la valeur de la propriété binary.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBinary() {
        return binary;
    }

    /**
     * Définit la valeur de la propriété binary.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBinary(byte[] value) {
        this.binary = value;
    }

    /**
     * Obtient la valeur de la propriété contentType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Définit la valeur de la propriété contentType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Obtient la valeur de la propriété fileSuffix.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileSuffix() {
        return fileSuffix;
    }

    /**
     * Définit la valeur de la propriété fileSuffix.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileSuffix(String value) {
        this.fileSuffix = value;
    }

}
