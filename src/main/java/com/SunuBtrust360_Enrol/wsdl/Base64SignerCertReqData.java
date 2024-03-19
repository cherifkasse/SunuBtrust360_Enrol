
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour base64SignerCertReqData complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="base64SignerCertReqData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="base64CertReq" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "base64SignerCertReqData", propOrder = {
    "base64CertReq"
})
public class Base64SignerCertReqData {

    protected byte[] base64CertReq;

    /**
     * Obtient la valeur de la propriété base64CertReq.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBase64CertReq() {
        return base64CertReq;
    }

    /**
     * Définit la valeur de la propriété base64CertReq.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBase64CertReq(byte[] value) {
        this.base64CertReq = value;
    }

}
