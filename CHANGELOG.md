```
  __  __                           _                   ___                 _              
 |  \/  | ___  ___ ___ __ _  __ _ (_) _ _   __ _  ___ / __| ___  _ _ __ __(_) __  ___  ___
 | |\/| |/ -_)(_-<(_-</ _` |/ _` || || ' \ / _` ||___|\__ \/ -_)| '_|\ V /| |/ _|/ -_)(_-<
 |_|  |_|\___|/__//__/\__,_|\__, ||_||_||_|\__, |     |___/\___||_|   \_/ |_|\__|\___|/__/
                            |___/          |___/                                          
```

# Changelog
All notable changes to this project will be documented in this file.

## Version [4.2.0.0] UNRELEASED

### Major Change: New Exceptions
- Added MessageBuilderException: An exception that is thrown if building an IDS-Message with the given information threw a ConstraintViolationException (catches builders RuntimeException).
- Added SerializeException: An exception that is thrown if serializing a message using the IDS-Serializer threw an IOException. Could indicate missing required message-fields.
- Added URISyntaxException: If the URL of the target is not a valid URI.
- Added SendMessageException: If sending the IDS-Request returns an IOException. Recipient not reachable or other further problems.
- Added DeserializeException: An exception that is thrown if deserializing a message using the IDS-Serializer threw an IOException. Could indicate a non-valid IDS-Message.
- Added ShaclValidatorException: SHACL-Validation, received message header does not conform to IDS-Infomodel and did not pass SHACL-Validation.
- Added UnknownResponseException: An exception that is thrown during converting an IDS-Response into a corresponding Object if no possible cast found. Could indicate a new unknown IDS-Message-Type. Error which is caused internally, but is passed on.
- Added UnexpectedResponseException: An exception that is thrown after converting a Response into the corresponding Object if the received response-type is not expected as a response to the request send.
- New Wiki-Page for exception documentation.


### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.1 -> 2.5.2
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.1 -> 2.5.2

### Miscellaneous
- Patch Change: Added Apache 2.0 License Header to all files
- Patch Change: Increased Test-Coverage

## Version [4.1.1.1] 2021-06-21

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.0.10 (released 2021-06-21)
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.0 -> 2.5.1
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.0 -> 2.5.1
- Upgrade: plugins:maven-dependency-plugin 3.1.2 -> 3.2.0

## Version [4.1.1.0] 2021-06-10

### Added
- Minor Change: New feature - FullText SPARQL Broker-Query support in BrokerService
- Minor Change: New feature - Clearing-House Endpoints: Two new fields in application.properties for the query- and logging-endpoint which can be optionally set by the user for the different CH endpoints, if others than default should be used (default endpoints: query: <CH-URL>/messages/query, log: <CH-URL>/messages/log). In total 3 applicaton.properties fields: clearinghouse.url, clearinghouse.query.endpoint, clearinghouse.log.endpoint
- Minor Change: New feature - At Connector runtime, individual additional DAPS DAT validation rules can now be added during the verification process of the DAT. For example, it is possible to create a blacklist of untrusted IDS-Connectors or DAPS-Systems and save them in a Connector-Database and check for them when a message is received. If the rules are not met, a RejectionMessage is sent automatically by the IDS-Messaging-Services.

### Changes
- Patch Change: Improved logging structure and messages for TEST_DEPLOYMENT
- Patch Change: Stopped printing values in logs for "Request token", "Response body of token request", "Dynamic Attribute Token" 

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.0.9 (released 2021-06-08)
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-parent 2.4.5 -> 2.5.0
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.4.5 -> 2.5.0
- Upgrade: org.apache.maven.plugins:maven-javadoc-plugin 3.2.0 -> 3.3.0
- Upgrade: org.bouncycastle:bcprov-jdk15on 1.68 -> 1.69
- Upgrade: org.bouncycastle:bcmail-jdk15o 1.68 -> 1.69
- Upgrade: org.springframework:spring-core 5.3.7 -> 5.3.8
- Upgrade: org.springframework:spring-tx 5.3.7 -> 5.3.8
- Upgrade: org.springframework:spring-test 5.3.7 -> 5.3.8
- Upgrade: org.springframework:spring-web 5.3.7 -> 5.3.8
- Upgrade: org.springframework:spring-webmvc 5.3.7 -> 5.3.8
- Upgrade: org.bitbucket.b_c:jose4j 0.7.7 -> 0.7.8
- Downgrade: plugins:maven-surefire-plugin 2.22.2 -> 2.19.1 (ensure compatibility with JUnit5)

### Miscellaneous
- Patch Change: In POM an unnecessary snapshot-repo has been removed, since snapshot-versions are not currently used as releases
- Patch Change: Move MultipartParser to Messaging Module (no access to the methods known from external)

---

## Version 4.1.0.0 2021-05-18
- First stable non-snapshot version 

### Basis of the functionalities
- Initial functionality based on the IDS-Connector-Framework. Further additional functionalities of the Interaction-Library and dynamic feature requests.

### Infomodel
- Used Dependency Version: 4.0.5
- Used Artifacts: java, infomodel-serializer, interaction
- Changes that may be necessary: ids:hasDefaultEndpoint is now a required entry in the connector properties

### Dependency Maintenance
- Dependabot: Dependabot will now automatically suggest pull requests for updates to dependencies.

### Added / Changed functionalities
- Modular project structure along the connectable infrastructure components.
- Include the ID of the rejected message in automatically send RejectionMessages.
- Extension of TEST_DEPLOYMENT-Mode: use an all-trusting trustmanager, accepting all SSL Certificates. This allows the use of selfsigned certificates in a TEST-environment.
- New exception structure for throwing exceptions for the TokenManagerService: If no token can be retrieved from the DAPS by these functionalities, an exception is returned to the connector developer.
- TokenProviderService will only acquire a new token, if the current one expired.
- Support for multiple DAPS modes via application.properties (daps.mode = aisec/orbiter)
- DAT is now checked against a list of trusted issuers given in application.properties.
- DapsValidator now accepts the DAT Token instead of the full Message.
- MultipartParser now throws MultipartParseException instead of FileUploadException.
- MessageController now accepts messages with empty payloads (as required by AppStore).
- Initial IDS-ClearingHouse support.
- Basic asynchronous message-flow support (support RequestInProcessMessage-Handling).
- Received RejectionMessages are now passed to the connector-developer, regardless of the status of the DAT within the received RejectionMessage
- Implemented Shacl Validation.
- Extended DAT Validation.
- No more static infomodel versioning in code.
- Compare supported inbound info-model-version to version of incoming message.
 
 ### Removed
 - Remove implementation of MQTT-support, as there was no demand for further support at the time of the evaluation. But can be added again in the future.
