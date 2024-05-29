
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour authorizedClient complex type.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="authorizedClient"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="certSN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="issuerDN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authorizedClient", propOrder = {
    "certSN",
    "issuerDN"
})
public class AuthorizedClient {

    protected String certSN;
    protected String issuerDN;

    /**
     * Obtient la valeur de la propri�t� certSN.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertSN() {
        return certSN;
    }

    /**
     * D�finit la valeur de la propri�t� certSN.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertSN(String value) {
        this.certSN = value;
    }

    /**
     * Obtient la valeur de la propri�t� issuerDN.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuerDN() {
        return issuerDN;
    }

    /**
     * D�finit la valeur de la propri�t� issuerDN.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuerDN(String value) {
        this.issuerDN = value;
    }

}
