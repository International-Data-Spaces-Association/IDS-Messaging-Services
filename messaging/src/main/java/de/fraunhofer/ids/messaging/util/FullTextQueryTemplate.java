/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.ids.messaging.util;

public final class FullTextQueryTemplate {
    public static final String FULL_TEXT_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/>\n"
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

    private FullTextQueryTemplate() {
        // Nothing to do here.
    }
}
