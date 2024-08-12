
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour destroyKey complex type.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="destroyKey"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="signerId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="purpose" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "destroyKey", propOrder = {
    "signerId",
    "purpose"
})
public class DestroyKey {

    protected int signerId;
    protected int purpose;

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
     * Obtient la valeur de la propri�t� purpose.
     * 
     */
    public int getPurpose() {
        return purpose;
    }

    /**
     * D�finit la valeur de la propri�t� purpose.
     * 
     */
    public void setPurpose(int value) {
        this.purpose = value;
    }

}
