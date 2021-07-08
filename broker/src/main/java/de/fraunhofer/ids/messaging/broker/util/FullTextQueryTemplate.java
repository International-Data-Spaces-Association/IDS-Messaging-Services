package de.fraunhofer.ids.messaging.broker.util;

import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

@UtilityClass
@FieldDefaults(makeFinal = true)
public class FullTextQueryTemplate {
    public static String FULL_TEXT_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/>\n"
                                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                                    + "SELECT DISTINCT ?resultUri ?res { \n"
                                    + "  GRAPH ?g { \n"
                                    + "    { ?resultUri a ids:Resource } \n"
                                    + "    UNION \n"
                                    + "    { ?resultUri a ids:BaseConnector }\n"
                                    + "    ?resultUri ?predicate ?res .\n"
                                    + "    FILTER ( DATATYPE(?res) = xsd:string ) .\n"
                                    + "    FILTER(REGEX(?res, \"%1$s\", \"i\"))\n"
                                    + "  }\n"
                                    + "} LIMIT %2$d OFFSET %3$d\n";
}
