
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour order.
 * 
 * <p>Le fragment de schma suivant indique le contenu attendu figurant dans cette classe.
 * <pre>
 * &lt;simpleType name="order"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ASC"/&gt;
 *     &lt;enumeration value="DESC"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "order")
@XmlEnum
public enum Order {

    ASC,
    DESC;

    public String value() {
        return name();
    }

    public static Order fromValue(String v) {
        return valueOf(v);
    }

}
