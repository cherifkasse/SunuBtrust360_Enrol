
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour eventStatus.
 * 
 * <p>Le fragment de schema suivant indique le contenu attendu figurant dans cette classe.
 * <pre>
 * &lt;simpleType name="eventStatus"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="FAILURE"/&gt;
 *     &lt;enumeration value="SUCCESS"/&gt;
 *     &lt;enumeration value="VOID"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "eventStatus")
@XmlEnum
public enum EventStatus {

    FAILURE,
    SUCCESS,
    VOID;

    public String value() {
        return name();
    }

    public static EventStatus fromValue(String v) {
        return valueOf(v);
    }

}
