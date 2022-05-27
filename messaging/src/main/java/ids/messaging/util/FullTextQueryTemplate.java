/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
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
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.util;

/**
 * Class containing the fulltext query template.
 */
public final class FullTextQueryTemplate {
    /**
     * Template for fulltext query search.
     */
    public static final String FULL_TEXT_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/>\n"
          + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
          + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
          + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
          + "SELECT DISTINCT ?resultUri ?res ?type ?accessUrl ?internalUri {\n"
          + "GRAPH ?g {\n"
          + "    {\n"
          + "      ?internalUri a ids:BaseConnector .\n"
          + "      # binds are directly to cover \"x a A, B .\" cases.\n"
          + "      BIND(ids:BaseConnector AS ?type)\n"
          + "      BIND(?internalUri AS ?connector)\n"
          + "    }\n"
          + "    UNION\n"
          + "    {\n"
          + "      ?internalUri a ids:TrustedConnector .\n"
          + "      BIND(ids:TrustedConnector AS ?type)\n"
          + "      BIND(?internalUri AS ?connector)\n"
          + "    }\n"
          + "    UNION\n"
          + "    {\n"
          + "      ?internalUri a ids:Resource .\n"
          + "      BIND(ids:Resource AS ?type)\n"
          + "      # From Resource backwards to Connector\n"
          + "      ?catalog ids:offeredResource ?internalUri .\n"
          + "      ?connector ids:resourceCatalog ?catalog .\n"
          + "    }\n"
          + "    UNION\n"
          + "    {\n"
          + "      ?internalUri a ids:Representation .\n"
          + "      BIND(ids:Representation AS ?type)\n"
          + "\n"
          + "      # From Representation backwards to Connector\n"
          + "      ?resource ids:representation ?internalUri .\n"
          + "      ?catalog ids:offeredResource ?resource .\n"
          + "      ?connector ids:resourceCatalog ?catalog .\n"
          + "    }\n"
          + "    UNION\n"
          + "    {\n"
          + "      ?internalUri a ids:Artifact .\n"
          + "\n"
          + "      # From Artifact backwards to Connector\n"
          + "      ?representation ids:instance ?internalUri .\n"
          + "      ?resource ids:representation ?representation .\n"
          + "      ?catalog ids:offeredResource ?resource .\n"
          + "      ?connector ids:resourceCatalog ?catalog .\n"
          + "    }\n"
          + "\n"
          + "    ?internalUri ?predicate ?text .\n"
          + "    FILTER ( isLiteral(?text)) .\n"
          + "\n"
          + "    FILTER( REGEX(?text, \"%1$s\", \"i\") || REGEX(str(?uri), \"%1$s\", \"i\") )\n"
          + "\n"
          + "    # Get the Access Endpoint\n"
          + "    ?connector ids:hasDefaultEndpoint ?endpoint .\n"
          + "    ?endpoint ids:accessURL ?accessUrl .\n"
          + "\n"
          + "    {\n"
          + "      # if a renaming happened, show the original URI.\n"
          + "      ?internalUri owl:sameAs ?uri .\n"
          + "    } UNION {\n"
          + "      # maybe this call is expensive.\n"
          + "      FILTER(NOT EXISTS { ?internalUri owl:sameAs ?uri } )\n"
          + "      BIND(?internalUri AS ?uri)\n"
          + "    }\n"
          + "\n"
          + "    BIND(?uri AS ?resultUri) # keep it non-breaking\n"
          + "    BIND(?text AS ?res) # keep it non-breaking\n"
          + "  }\n"
          + "} LIMIT %2$d OFFSET %3$d\n";

    private FullTextQueryTemplate() {
        // Nothing to do here.
    }
}
