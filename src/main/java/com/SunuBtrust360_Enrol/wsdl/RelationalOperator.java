
package com.SunuBtrust360_Enrol.wsdl;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour relationalOperator.
 * 
 * <p>Le fragment de schema suivant indique le contenu attendu figurant dans cette classe.
 * <pre>
 * &lt;simpleType name="relationalOperator"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="GT"/&gt;
 *     &lt;enumeration value="GE"/&gt;
 *     &lt;enumeration value="LT"/&gt;
 *     &lt;enumeration value="LE"/&gt;
 *     &lt;enumeration value="EQ"/&gt;
 *     &lt;enumeration value="NEQ"/&gt;
 *     &lt;enumeration value="BETWEEN"/&gt;
 *     &lt;enumeration value="LIKE"/&gt;
 *     &lt;enumeration value="NULL"/&gt;
 *     &lt;enumeration value="NOTNULL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "relationalOperator")
@XmlEnum
public enum RelationalOperator {

    GT,
    GE,
    LT,
    LE,
    EQ,
    NEQ,
    BETWEEN,
    LIKE,
    NULL,
    NOTNULL;

    public String value() {
        return name();
    }

    public static RelationalOperator fromValue(String v) {
        return valueOf(v);
    }

}
