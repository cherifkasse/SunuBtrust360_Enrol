
package com.SunuBtrust360_Enrol.wsdl;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour queryArchiveWithIds complex type.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="queryArchiveWithIds"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uniqueIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="includeData" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryArchiveWithIds", propOrder = {
    "uniqueIds",
    "includeData"
})
public class QueryArchiveWithIds {

    protected List<String> uniqueIds;
    protected boolean includeData;

    /**
     * Gets the value of the uniqueIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the uniqueIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUniqueIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUniqueIds() {
        if (uniqueIds == null) {
            uniqueIds = new ArrayList<String>();
        }
        return this.uniqueIds;
    }

    /**
     * Obtient la valeur de la propri�t� includeData.
     * 
     */
    public boolean isIncludeData() {
        return includeData;
    }

    /**
     * D�finit la valeur de la propri�t� includeData.
     * 
     */
    public void setIncludeData(boolean value) {
        this.includeData = value;
    }

}
