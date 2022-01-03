```
  __  __                           _                   ___                 _              
 |  \/  | ___  ___ ___ __ _  __ _ (_) _ _   __ _  ___ / __| ___  _ _ __ __(_) __  ___  ___
 | |\/| |/ -_)(_-<(_-</ _` |/ _` || || ' \ / _` ||___|\__ \/ -_)| '_|\ V /| |/ _|/ -_)(_-<
 |_|  |_|\___|/__//__/\__,_|\__, ||_||_||_|\__, |     |___/\___||_|   \_/ |_|\__|\___|/__/
                            |___/          |___/                                          
```

# Changelog
All notable changes to this project will be documented in this file.

## Version [5.3.1] UNRELEASED

### Patch Change: Infomodel Maintenance
- Downgrade to older version: 4.2.7 ([PR 401](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/401))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.6.1 -> 2.6.2 ([PR 410](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/410))
- Upgrade: org.springframework.boot:spring-boot-starter 2.6.1 -> 2.6.2 ([PR 410](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/410))
- Upgrade: org.springframework:spring-webmvc 5.3.13 -> 5.3.14 ([PR 400](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/400))
- Upgrade: org.springframework:spring-core 5.3.13 -> 5.3.14 ([PR 400](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/400))
- Upgrade: org.springframework:spring-web 5.3.13 -> 5.3.14 ([PR 400](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/400))
- Upgrade: org.springframework:spring-test 5.3.13 -> 5.3.14 ([PR 400](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/400))
- Upgrade: org.springframework:spring-tx 5.3.13 -> 5.3.14 ([PR 400](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/400))
- Upgrade: org.apache.jena:jena-core 4.3.0 -> 4.3.2 ([PR 393](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/393), [PR 410](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/410))
- Upgrade: com.puppycrawl.tools:checkstyle 9.2 -> 9.2.1 ([PR 410](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/410))

## Version [5.3.0] 2021-12-13

### Minor Change: New application.properties flags
- `daps.jwt.signature.algorithm=RSA256/ECDSA256`, to be able to choose between RSA 256 and ECDSA 256 as signature signing algorithm for the JWTs to the DAPS for the DAT request. Default if not set is RSA256 (using RSA as signing algorithm). ([Issue 376](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/376))

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.8 ([PR 391](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/391))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.6.0 -> 2.6.1 ([PR 385](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/363), [PR 373](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/385))
- Upgrade: org.springframework.boot:spring-boot-starter 2.6.0 -> 2.6.1 ([PR 385](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/363), [PR 373](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/385))
- Upgrade: org.junit.jupiter:junit-jupiter-engine 5.8.1 -> 5.8.2 ([PR 382](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/382))
- Upgrade: org.junit.jupiter:junit-jupiter-api 5.8.1 -> 5.8.2 ([PR 382](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/382))
- Upgrade: org.junit.jupiter:junit-jupiter 5.8.1 -> 5.8.2 ([PR 382](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/382))
- Upgrade: com.puppycrawl.tools:checkstyle 9.1 -> 9.2 ([PR 381](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/382))
- Upgrade: org.bouncycastle:bcprov-jdk15on 1.69 -> 1.70 ([PR 388](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/388))
- Upgrade: org.bouncycastle:bcmail-jdk15o 1.69 -> 1.70 ([PR 388](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/388))
- Upgrade: org.json:json 20210307 -> 20211205 ([PR 389](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/389))
- Upgrade: org.apache.jena:jena-core 4.2.0 -> 4.3.0 ([PR 390](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/390))   

## Version [5.2.0] 2021-11-24

### Minor Change: New application.properties flags
- `daps.enable.log.jwt=true/false`, to enable DAPS response logging including the JWT. Default if not set is `false` (logging not enabled). ([PR 353](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/353))
- `daps.enable.cache.dat=true/false`, enables or disables caching of DAPS DAT. Default if not set is `true` (caching enabled). ([PR 354](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/354))
- `daps.time.offset.seconds=<INTEGER>`, to freely configure a possible time difference between the system of the connector and the DAPS. The entered interger value is subtracted from the current time in the form of seconds and the `isa` and `nbf` are set in the JWT from the connector to the DAPS with the adjusted time. Default if not set is `10` (current time minus 10 seconds). ([PR 354](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/354))

### Patch Change: Enhancements
- If the validation of the SecurityProfile is performed and is not successful, the reason is now also output in the logs. Possible reasons: no security profile given in DAT; registered security profile at DAPS does not match given security profile in message. ([PR 352](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/352))
- When a DAPS DAT is received, the expiry time is cached directly when the DAT is received instead of being read from the claims for each message to be sent. Prevents an error-log-message from the JWT parser. ([Issue 351](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/351))
- When using a cached DAPS DAT for sending a message, the expiration date is now logged at info-level, e.g. `Using cached DAPS DAT. [expiration=(Thu Nov 11 13:08:13 CET 2021)]` ([PR 354](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/354))

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.7 ([PR 350](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/350))
- Used Artifacts: java, infomodel-serializer, interaction
- Note: There can be major changes depending on the setup, identified by us could be following:
  - Configmodel: value of `ids:hasDefaultEndpoint` `@id` needs to be different from values of `ids:curator` `@id` and `ids:maintainer` `@id`, was allowed before (e.g. for testing).
  - Serializer: `tokenvalue` of `DynamicAttributeToken` must not contain an empty string anymore (e.g. for testing or also in incoming requests), otherwise the serializer throws an `IOException` during deserializing -> `The following mandatory field(s) of DynamicAttributeToken are not filled or invalid: ids:tokenValue. [...]`. This change has no impact on the functionality of the `TEST_DEPLOYMENT`: if no DAT can be fetched, we use a dummy value as `tokenvalue` and thus no empty string. However, if, for whatever reason, requests are received with an empty `tokenvalue` in `TEST_DEPLOYMENT`, the serializer will now throw an `IOException`.

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.6 -> 2.6.0 ([PR 363](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/363), [PR 373](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/373))
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.6 -> 2.6.0 ([PR 363](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/363), [PR 373](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/373))
- Upgrade: org.springframework:spring-webmvc 5.3.12 -> 5.3.13 ([PR 360](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/360))
- Upgrade: org.springframework:spring-core 5.3.12 -> 5.3.13 ([PR 360](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/360))
- Upgrade: org.springframework:spring-web 5.3.12 -> 5.3.13 ([PR 360](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/360))
- Upgrade: org.springframework:spring-test 5.3.12 -> 5.3.13 ([PR 360](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/360))
- Upgrade: org.springframework:spring-tx 5.3.12 -> 5.3.13 ([PR 360](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/360))
- Upgrade: com.puppycrawl.tools:checkstyle 9.0.1 -> 9.1 ([PR 347](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/347))
- Upgrade: com.squareup.okhttp3:mockwebserver 4.9.2 -> 4.9.3 ([PR 371](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/371))
- Upgrade: com.squareup.okhttp3:okhttp 4.9.2 -> 4.9.3 ([PR 372](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/372))

## Version [5.1.1] 2021-10-26

### Patch Change: Enhancements
- If the request for the DAPS DAT fails, the response body is additionally logged if present. ([PR 342](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/342))

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.6 ([PR 343](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/343))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.5 -> 2.5.6 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.5 -> 2.5.6 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework:spring-webmvc 5.3.11 -> 5.3.12 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework:spring-core 5.3.11 -> 5.3.12 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework:spring-test 5.3.11 -> 5.3.12 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework:spring-web 5.3.11 -> 5.3.12 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))
- Upgrade: org.springframework:spring-tx 5.3.11 -> 5.3.12 ([PR 341](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/341))

## Version [5.1.0] 2021-10-20

### Minor Change: Validation referringConnector vs ids:issuerConnector
- New application.properties flag `referred.check=true/false`, to enable comparison between DAT claims `referringConnector` and `ids:issuerConnector` in message-header for the validation of incoming messages. Can only be used in `PRODUCTIVE_DEPLOYMENT`. Default if not set is `false` (not enabled). ([PR 329](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/329))
- Automatically sends RejectionMessage on incoming messages if validation not passed. Example of text included in RejectionMessage: `ids:issuerConnector in message-header (https://w3id.org/idsa/autogen/baseConnector/691b3a17-1e09-4a5a-9d9a-5627772222e9) does not match referringConnector in body of DAT claims (http://isst_ids_framework_demo_connector.demo)!`

### Patch Change: Enhancement Log-Codes
- Log-codes now exist for different log-levels. They allow easy search for the code location that produced the log. No log-code will be printed for log-info level. ([PR 332](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/332))
- Syntax: IMS-XY-L-1234 shortened to IMSXYL1234. Will e.g. log as [code=(IMSCOE0001)].
  - IMS = IDS-Messaging-Services
  - XY = Subsystem Module (CO Core, AP AppStore, BR Broker, CL ClearingHouse, ME Messaging, PA Paris, VO Vocol)
  - L = Event Severity (E Error, W Warn, D Debug)
  - 1234 = Error number

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.5 ([PR 330](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/330))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework:spring-core 5.3.10 -> 5.3.11 ([PR 326](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/326))
- Upgrade: org.springframework:spring-tx 5.3.10 -> 5.3.11 ([PR 326](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/326))
- Upgrade: org.springframework:spring-webmvc 5.3.10 -> 5.3.11 ([PR 326](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/326))
- Upgrade: org.springframework:spring-web 5.3.10 -> 5.3.11 ([PR 326](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/326))
- Upgrade: org.springframework:spring-test 5.3.10 -> 5.3.11 ([PR 326](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/326))
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.4 -> 2.5.5 ([PR 314](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/314))
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.4 -> 2.5.5 ([PR 314](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/314))
- Upgrade: org.junit.jupiter:junit-jupiter-engine 5.8.0 -> 5.8.1 ([PR 311](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/311))
- Upgrade: org.junit.jupiter:junit-jupiter-api 5.8.0 -> 5.8.1 ([PR 311](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/311))
- Upgrade: org.junit.jupiter:junit-jupiter 5.8.0 -> 5.8.1 ([PR 311](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/311))
- Upgrade: com.squareup.okhttp3:okhttp 4.9.1 -> 4.9.2 ([PR 317](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/317))
- Upgrade: com.squareup.okhttp3:mockwebserver 4.9.1 -> 4.9.2 ([PR 318](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/318))
- Upgrade: com.puppycrawl.tools:checkstyle 9.0 -> 9.0.1 ([PR 319](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/319))
- Upgrade: org.projectlombok:lombok 1.18.20 -> 1.18.22 ([PR 320](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/320))

## Version [5.0.1] 2021-09-21

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework:spring-webmvc 5.3.9 -> 5.3.10 ([PR 300](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/300))
- Upgrade: org.springframework:spring-test 5.3.9 -> 5.3.10 ([PR 299](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/299))
- Upgrade: org.springframework:spring-core 5.3.9 -> 5.3.10 ([PR 297](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/297))
- Upgrade: org.springframework:spring-web 5.3.9 -> 5.3.10 ([PR 301](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/301))
- Upgrade: org.springframework:spring-tx 5.3.9 -> 5.3.10 ([PR 298](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/298))
- Replace: de.fraunhofer.iais.eis.ids:interaction org.apache.jena:jena-core 4.1.0 -> 4.2.0 ([PR 303](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/303))   

## Version [5.0.0] 2021-09-14

### Major Change: ClearingHouse Module - New Endpoint
- The ClearingHouse has a new endpoint, which allows the registration of a freely selectable PID that has not been assigned, whereby the PID access-authorized Connectors (Owners) must be specified as with their IDs in the body. As a result the previous existing functionality of the ClearingHouse module to log a message at the ClearingHouse, where the Messaging-Services randomly generated the PID, was removed (ClearingHouseService sendLogToClearingHouse). The new method is the ClearingHouseService.registerPidAtClearingHouse, which expects as parameters the desired PID and the IDs of the Connectors, which should all be set as Owners for the PID (can also be exactly 1 Connector-Id). ([Issue 259](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/259))

### Patch Change: Enhancements
- For incoming responses to sent requests, an IOException is now no longer thrown for response codes outside 200-299. These responses may also be valid IDS-messages, for example a RejectionMessage with the status BAD_REQUEST. ([Issue 277](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/277))
- If the connector's proxy configuration contains an incorrect empty hostname or proxy, a warning message is now logged and an attempt is made to send the message without this proxy instead of throwing an IllegalArgumentException. ([Issue 285](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/285))
- Due to an incorrect or missing connector configuration it could happen that the location of the KeyStore is null. This is now handled in the form of a KeyStoreManagerInitializationException and log message at KeyStoreManager init. ([Issue 290](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/290))
- Outgoing messages can now be logged in loglevel:debug (requires loglevel-config:debug for IDS-Messaging-Services e.g.: `<Loggername="de.fraunhofer.ids.messaging" level="debug"/>`) . ([Issue 284](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/284))
- Other minor enhancements to existing Javadoc.

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.3 ([Issue 288](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/288))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Dependency Maintenance
- Upgrade: com.puppycrawl.tools:checkstyle 8.45.1 -> 9.0 ([PR 275](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/275))
- Upgrade: org.apache.maven.plugins:maven-javadoc-plugin 3.3.0 -> 3.3.1 ([PR 281](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/281))
- Upgrade: org.junit.jupiter:junit-jupiter 5.7.2 -> 5.8.0 ([PR 295](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/295))
- Upgrade: org.junit.jupiter:junit-jupiter-api 5.7.2 -> 5.8.0 ([PR 295](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/295))
- Upgrade: org.junit.jupiter:junit-jupiter-engine 5.7.2 -> 5.8.0 ([PR 295](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/295))

## Version [4.3.0] 2021-08-31

### Minor Change: Incoming message infomodel compatibility check
- The check whether an incoming message is compatible with its ModelVersion to the inbound ModelVersions of the Connector can be switched on or off via application.properties (infomodel.compatibility.validation=true/false). The default value if not set is true (switched on). ([PR 267](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/267))

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.2.1 (released 2021-08-30) ([Issue 268](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/268))
- Used Artifacts: java, infomodel-serializer, interaction

### Patch Change: Enhancements
- If no private key can be found for a given alias within a keystore, a KeyStoreException is now thrown and an error is logged, preventing an otherwise possible NullpointerException (KeystoreManager getPrivateKeyFromKeyStore). ([Issue 263](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues/263))
- The payload of incoming messages is now first validated for valid JSON and whether the securityProfile attribute is present, if not, this check is skipped instead of issuing an error message in the logs (IdsHttpService checkDatFromResponse). ([PR 266](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/266))
- For log error messages and others important logs with dynamic content, the e.g. exception reason is now highlighted to distinguish it from the rest of the log message. Format: [exception=(...)]. ([PR 269](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/pull/269))

### Patch Change: Miscellaneous
- Other minor enhancements to existing Javadoc and log messages.

## Version [4.2.2] 2021-08-26

### Patch Change: Miscellaneous
- When the security-profile attribute of an incoming message is determined, all exceptions are now caught, regardless of their cause.

## Version [4.2.1] 2021-08-26

### Patch Change: Miscellaneous
- Refactored debug/info/warn/error log-messages (e.g. for KeyStoreManager and IdsHttpService).

## Version [4.2.0] 2021-08-24

### Minor Change: FullTextQuery template update
- Extended FullTextQuery template in messaging.util.FULL_TEXT_QUERY, with the following new features:
  - provides the original identifier
  - returns the type of the found entity
  - gives the AccessUrl of the related Connector
  - searches also for non-xsd:string Literals
  - also checks for entities that are not a BaseConnector or a Resource
- Note: returns of APIs that are based on the template could be extended to include this new informations, depending on the connector implementation.

### Patch Change: Miscellaneous
- Missing Javadoc annotations for thrown exceptions to services were added where they were missing.

### Patch Change: Dependency Maintenance
- Upgrade: org.bitbucket.b_c:jose4j 0.7.8 -> 0.7.9
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.3 -> 2.5.4
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.3 -> 2.5.4

## Version [4.1.0] SKIPPED

### Note on the version number - skipping v4.1.0
- In the past, a different version numbering system approach with 4 digits was followed and various old versions of the IDS-Messaging-Services exist, which start with 4.1.0, for example the old version 4.1.0.0. To avoid confusion, we skip version-number 4.1.0, to not have versions 4.1.0 and 4.1.0.0 present at the same time.

## Version [4.0.0] 2021-08-17

### Major Change: Dependency Infomodel Artifacts 4.2.0
- The update of the infomodel artifacts to version 4.2.0 may result in breaking changes for the connector developers.
- One of the possible breaking changes is that PaymentModality is no longer defined as a list and therefore calls like isEmpty() will no longer compile.

### Minor Change: CertificateSubjectCnProvider
- New feature: **CertificateSubjectCnProvider.certificateSubjectCn** provides static access to the subject-CN of the connector certificate, which could be used as the connector UUID depending on the implementation of the connector. Value is initialized by the KeyStoreManager and reset at each update. If no valid certificate with Subject-CN is available, a random UUID is generated in the KeyStoreManager instead.

### Patch Change: Log Message Changes
- Changes in printed info/warning/error logs
  - Removed error log message "ERROR - JWT strings must contain exactly 2 period characters. Found: 0" which occurred only in TEST_DEPLOYMENT and has caused confusion
  - Print warn message "Could not parse jwt!" only in PRODUCTIVE_DEPLOYMENT and adjusted warn message content
  - Adjusted error message "Mandatory required information of the connector certificate is missing (AKI/SKI)!" with reference to the connector fingerprint
  - Stopped printing connector fingerprint in log message in TokenManagerService
  - Other minor adjustments to make log messages more consistent (parameterized logs, more useful outputs) 

### Patch Change: Miscellaneous
- Internal change: Identifiers and documentation in the code that previously described the supposed connector UUID have been changed to connector fingerprint to reflect their actual meaning.

### Patch Change: Dependency Maintenance
- Upgrade: com.puppycrawl.tools:checkstyle 8.45 -> 8.45.1 

## Version [3.1.0] 2021-08-09

### Infomodel Maintenance (Patch Change)
- Used Dependency Version: 4.1.2 (released 2021-07-30)
- Used Artifacts: java, infomodel-serializer, interaction

### Added Features (Minor change)
- Service-Module for AppStore communication (AppStoreService)
- Service-Module for ParIS communication (ParisService)
- Service-Module for Vocol communication (VocolService)

### Internal Changes (Patch Change)
- Patch change: The BrokerService now internally uses the Messaging-Module QueryService for building Query-Messages.

### Dependency Maintenance (Patch Change)
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.2 -> 2.5.3
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.2 -> 2.5.3
- Upgrade: com.puppycrawl.tools:checkstyle 8.44 -> 8.45
- Upgrade: org.springframework.spring-webmvc 5.3.8 -> 5.3.9
  
### Transitive Dependencies CVE Exclusions (Patch Change) 
- Exclude: commons-io 2.2 from commons-fileupload, replace with commons-io 2.11
- Exclude: commons-compress 1.20 from interaction, replace with commons-compress 1.21

### Miscellaneous
- New Repo pipeline setup: New required checks to pass for PR - Checkstyle & License

## Version [3.0.0] 2021-07-20

### Major Change: Dependency Infomodel Artifacts 4.1.1
- The update of the infomodel artifacts to version 4.1.1 may result in breaking changes for the connector developers.
One of the possible breaking changes is that all lists are now initialized as empty ArrayList.
For a complete changelog of artifacts, see: https://github.com/International-Data-Spaces-Association/Java-Representation-of-IDS-Information-Model/blob/main/Changelog.md

## Version [2.0.1] 2021-07-20

### Changes
- Patch Change: If the search term for the fulltext broker search is already passed in quotes, these are now removed and the adjusted search term is passed to the query template.

### Patch Change: Dependency Maintenance
- Upgrade: org.springframework:spring-core 5.3.8 -> 5.3.9
- Upgrade: org.springframework:spring-tx 5.3.8 -> 5.3.9
- Upgrade: org.springframework:spring-web 5.3.8 -> 5.3.9
- Upgrade: org.springframework:spring-test 5.3.8 -> 5.3.9

## Version [2.0.0] 2021-07-16
With this version we switch to the versions-format of semantic versioning. In principle, only the first version-number position is omitted compared to the previous versions.

### Major Change: MessageContainer
- Services now return MessageContainer, instead of xyzMAP tied to a specific expected response
  - allows access to headerContainer (all header info in via getter), underlyingMessage (as infomodel message), receivedPayload and if the response is a RejectionMessage the rejectionReason

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

### Added
- Patch change: Added Maven plugin to generate Apache 2.0 license-header in files
- Minor change: New feature, Token-Claims can now be accessed
- Minor change: New feature, the ConfigProducer can now be intercepted by the Connector-Developer (Pre and Post)

### Changes
- Patch Change: Improved logging structure and messages for TEST_DEPLOYMENT vs PRODUCTIVE_DEPLOYMENT
- Patch Change: Improved error log level messages (no more plain error stack trace)

### Dependency Maintenance
- Upgrade: org.springframework.boot:spring-boot-starter-test 2.5.1 -> 2.5.2
- Upgrade: org.springframework.boot:spring-boot-starter 2.5.1 -> 2.5.2

### Miscellaneous
- Patch Change: Added Apache 2.0 License Header to all files
- Patch Change: Increased Test-Coverage

## Version [4.1.1.4] 2021-07-07

### Changes
- Patch change: KeyStoreManager: look up absolute path at system scope

## Version [4.1.1.3] 2021-07-05

### Patch Change: Infomodel Maintenance
- Used Dependency Version: 4.1.0 (released 2021-07-05)
- Used Artifacts: java, infomodel-serializer, interaction

## Version [4.1.1.2] 2021-07-02 - Recommended Security Update

### Patch Change: Dependency Maintenance
- Remove: org.apache.maven.plugins:maven-project-info-reports-plugin

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
- Minor Change: New feature - Clearing-House Endpoints: Two new fields in application.properties for the query- and logging-endpoint which can be optionally set by the user for the different CH endpoints, if others than default should be used (default endpoints: query: <CH-URL>/messages/query, log: <CH-URL>/messages/log). In total 3 application.properties fields: clearinghouse.url, clearinghouse.query.endpoint, clearinghouse.log.endpoint
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
