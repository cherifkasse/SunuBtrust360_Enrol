
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour pkcs10CertReqInfo complex type.
 * 
 * <p>Le fragment de schema suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="pkcs10CertReqInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="attributes" type="{http://adminws.signserver.org/}asn1Set" minOccurs="0"/&gt;
 *         &lt;element name="signatureAlgorithm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="subjectDN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pkcs10CertReqInfo", propOrder = {
    "attributes",
    "signatureAlgorithm",
    "subjectDN"
})
public class Pkcs10CertReqInfo {

    protected Asn1Set attributes;
    protected String signatureAlgorithm;
    protected String subjectDN;
    public Pkcs10CertReqInfo(Asn1Set attributes, String signatureAlgorithm, String subjectDN) {
        this.attributes = attributes;
        this.signatureAlgorithm = signatureAlgorithm;
        this.subjectDN = subjectDN;
    }

    public Pkcs10CertReqInfo() {

    }

    public Pkcs10CertReqInfo(byte[] encoded) {
    }


    /**
     * Obtient la valeur de la propri�t� attributes.
     * 
     * @return
     *     possible object is
     *     {@link Asn1Set }
     *     
     */
    public Asn1Set getAttributes() {
        return attributes;
    }

    /**
     * D�finit la valeur de la propri�t� attributes.
     * 
     * @param value
     *     allowed object is
     *     {@link Asn1Set }
     *     
     */
    public void setAttributes(Asn1Set value) {
        this.attributes = value;
    }

    /**
     * Obtient la valeur de la propri�t� signatureAlgorithm.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * D�finit la valeur de la propri�t� signatureAlgorithm.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignatureAlgorithm(String value) {
        this.signatureAlgorithm = value;
    }

    /**
     * Obtient la valeur de la propri�t� subjectDN.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubjectDN() {
        return subjectDN;
    }

    /**
     * D�finit la valeur de la propri�t� subjectDN.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubjectDN(String value) {
        this.subjectDN = value;
    }

}
