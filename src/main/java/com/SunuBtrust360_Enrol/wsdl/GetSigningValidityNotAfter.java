
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour getSigningValidityNotAfter complex type.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getSigningValidityNotAfter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="workerId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSigningValidityNotAfter", propOrder = {
    "workerId"
})
public class GetSigningValidityNotAfter {

    protected int workerId;

    /**
     * Obtient la valeur de la propri�t� workerId.
     * 
     */
    public int getWorkerId() {
        return workerId;
    }

    /**
     * D�finit la valeur de la propri�t� workerId.
     * 
     */
    public void setWorkerId(int value) {
        this.workerId = value;
    }

}
