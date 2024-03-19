
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour addAuthorizedClient complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="addAuthorizedClient"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="workerId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="authClient" type="{http://adminws.signserver.org/}authorizedClient" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addAuthorizedClient", propOrder = {
    "workerId",
    "authClient"
})
public class AddAuthorizedClient {

    protected int workerId;
    protected AuthorizedClient authClient;

    /**
     * Obtient la valeur de la propriété workerId.
     * 
     */
    public int getWorkerId() {
        return workerId;
    }

    /**
     * Définit la valeur de la propriété workerId.
     * 
     */
    public void setWorkerId(int value) {
        this.workerId = value;
    }

    /**
     * Obtient la valeur de la propriété authClient.
     * 
     * @return
     *     possible object is
     *     {@link AuthorizedClient }
     *     
     */
    public AuthorizedClient getAuthClient() {
        return authClient;
    }

    /**
     * Définit la valeur de la propriété authClient.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorizedClient }
     *     
     */
    public void setAuthClient(AuthorizedClient value) {
        this.authClient = value;
    }

}
