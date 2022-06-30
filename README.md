<p align="center">
<img src="ids-messaging-services-b140.PNG">
</p>

<p align="center">
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/development/LICENSE">License</a> •
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues">Issues</a> •
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/discussions">Discussions</a> •
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki">Wiki</a> •
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/development/CONTRIBUTING.md">Contributing</a> •
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/development/CODE_OF_CONDUCT.md">Code of Conduct</a>
</p>

# IDS-Messaging-Services

All participants of IDS-based data ecosystems must be able to communicate with each other. Even though different participants may have different implementations of IDS-Connectors, they all need to send and receive IDS-Messages. This commonality is addressed by the IDS-Messaging-Services, which provide a lightweight implementation for IDS-Message-Handling. The IDS-Messaging-Services offer open-source functionality for sending IDS-Messages as well as interfaces for processing received IDS-Messages. The architecture relies on a modern modular architecture-approach so that the functionalities needed for communication in a data ecosystems can be easily selected specific to the infrastructure components present in the data ecosystems. In addition, advanced functionalities are implemented, such as checking the validity of the Dynamic-Attribute-Token of incoming messages.

## Overview: Versioning

The IDS-Messaging-Service follows the SemanticVersioning system.

## Overview: IDS-Infomodel-Artifacts

The following IDS-Infomodel-Artifacts are used as dependencies.

| Group | Artifact | Version |
| ------ | ------ |---------| 
| de.fraunhofer.iais.eis.ids.infomodel | java | 4.2.7   |
| de.fraunhofer.iais.eis.ids | infomodel-serializer | 4.2.8   |
| de.fraunhofer.iais.eis.ids | interaction | 4.2.7   |

## Overview: Supported IDS-Message protocols

The following transmission options for IDS-Messages are currently supported:
- HTTP Multipart

Currently being worked on:
- IDS-LDP / REST

## Overview: IDS-Infrastructure-Components

Supported out-of-the-box connectivity to the following IDS-Infrastructure-Components with advanced functionality:
- Other IDS-Connectors
- IDS-AppStore
- IDS-Broker
- IDS-ClearingHouse
- IDS-DAPS
- IDS-ParIS
- IDS-VoCol

## Quick Start: Technical requirements

- The IDS-Messaging-Services use asymmetric encryption concepts and requires public and private key of the Connector-Instance.
- The IDS-Messaging-Services utilize contents of the IDSConfiguration Model which is part of the IDS Information Model. Therefor a *configmodel.json*-file should exist to load the configuration of the IDS-Connector. For example, the configuration file should reference the key- and trust-store.
- The IDS-Messaging-Services assume a SpringBoot project and therefore require various specific SpringBoot functionalities.
- [Settings in the Application Properties](https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki/09.-Settings:-Connector-Configuration)

## Quick Start: Integration into a Maven-Java-Project

### Step 1

The Java-modules provided by the project are accessible as Maven artifact dependencies. The repository must be added as a repository in the project's pom:

```xml
<repository>
    <id>sovity-public</id>
    <url>https://pkgs.dev.azure.com/sovity/5bec6cbd-c80a-47ac-86ce-1deb26cee853/_packaging/artifact/maven/v1</url>
</repository>
```

### Step 2

The current module artifact structure of the IDS-Messaging-Services is built along the different IDS-Infrastructure-Components, which are currently supported with advanced functionalities out-of-the-box.

Modules with basic functions:
- core
- messaging

Modules for different infrastructure components:
- appstore
- broker
- clearinghouse
- paris
- vocol

In general, the core-module artifact is the main module artifact with the configuration of the IDS-Messaging-Services. The messaging-module artifact provides all needed functionalities to send and receive IDS-Messages. The messaging-module artifact in turn holds the core-module artifact as a dependency. So it is enough to specify the messaging-module artifact in the project's pom and the functionality of the core-module artifact will be loaded automatically.

The individual module-artifacts of the IDS-Infrastructure-Components in turn require functionalities of the messaging-module artifact and have it therefore linked as a dependency in each module case. This simplified architecture means that it is sufficient, for example, to integrate the broker-module artifact into the project's pom, which automatically makes the functionalities of the messaging- and thus also the core-module artifact available.

So, if an IDS-Connector should be implemented, in whose data ecosystem an IDS-Broker occurs as IDS-Infrastructure-Component, the following entry in the project's pom is completely sufficient as dependencies to get all needed functionalities to exchange messages with the IDS-Broker with advanced functionalities, without the need to use the full-module:

```xml
<dependency>
    <groupId>ids.messaging</groupId>
    <artifactId>broker</artifactId>
    <version>IDS_MESSAGING_SERVICES_VERSION</version>
</dependency>
```
Of course, the entry IDS_MESSAGING_SERVICES_VERSION must be exchanged with the desired version number of the IDS-Messaging-Services artifact.


## Quick Start: First steps towards use

For a detailed description of the usage of the different IDS-Messaging-Services artifacts see the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki">Wiki</a> pages of this project.

Following is a first step guide to using the IDS-Messaging-Services after including the required dependencies in the project's pom.

### Step 3

Add the ``@ComponentScan`` annotation to the SpringBoot-Application and scan for the IDS-Messaging-Services packages. This can be implemented, for example, as follows using wildcards:
```java
@ComponentScan({
        "ids.*",
        "ids.messaging.*"
})
```

### Step 4

Start implementing Message-Handler e.g. as instances of RequestMessage which your IDS-Connector should be able to process as received IDS-Message. An example for a MessageHandler is given below. The following message-handler receives the arrived message and sends the received message payload directly back to the sending IDS-Connector. This can be customized as desired and serves only as an example.

```java
@Component
@SupportedMessageType(RequestMessageImpl.class)
public class RequestMessageHandler implements MessageHandler<RequestMessageImpl> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMessageHandler.class);

    private final Connector connector;
    private final DapsTokenProvider provider;

    public RequestMessageHandler(final ConfigContainer container,
                                 final DapsTokenProvider provider) {
        this.connector = container.getConnector();
        this.provider = provider;
    }

    /**
     * This message implements the logic that is needed to handle the message. As it just returns the input as string
     * the messagePayload-InputStream is converted to a String.
     *
     * @param requestMessage The RequestMessageImpl (part one of the multipart message) containing interesting meta data.
     * @param messagePayload The received payload that has to be handled here
     * @return received payload as string, wrapped inside a @{@link BodyResponse}
     */
    @Override
    public MessageResponse handleMessage(final RequestMessageImpl requestMessage,
                                         final MessagePayload messagePayload) {
        LOGGER.info("Received a RequestMessage!");

        try {
            final var receivedPayload = IOUtils.toString(messagePayload.getUnderlyingInputStream(), StandardCharsets.UTF_8.name()) + " - from RequestMessage!";

            final var message = new ResponseMessageBuilder()
                    ._securityToken_(provider.getDAT())
                    ._correlationMessage_(requestMessage.getId())
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._issuerConnector_(connector.getId())
                    ._modelVersion_(this.connector.getOutboundModelVersion())
                    ._senderAgent_(connector.getId())
                    .build();

            return BodyResponse.create(message, receivedPayload);
        } catch (Exception e) {
            return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                    e.getMessage(),
                    connector.getId(),
                    connector.getOutboundModelVersion());
        }
    }
}
```

### Step 5

Sending IDS-Messages: Now that messages can be received, the functionality for sending IDS-messages is shown with an example.

The following is an example without connection to an IDS-Infrastructure-Component. The idsHttpService can be accessed at any time to send a message to any recipient. For the messaging to the supported IDS-Infrastructure-Components, however, ready-made methods are already available in the respective modules, so that the specific message itself does not have to be created. The following is a minimal example, which assumes that only the messaging-module is present.

Building the IDS-message for a specific message type:
```java
Message message = new RequestMessageBuilder()
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._modelVersion_(baseConnector.getOutboundModelVersion())
            ._issuerConnector_(baseConnector.getId())
            ._senderAgent_(baseConnector.getId())
            ._securityToken_(tokenProvider.getDAT())
            .build();

MultipartBody body = this.buildRequestBody(InfomodelMessageBuilder.messageWithString(message, payload));
final var response = idsHttpService.sendAndCheckDat(body, targetUri);
```

The above is a standard example of HTTP-Multipart using the services' sendAndCheckDat()-method. In addition, there are other methods like a plain send()-Method which will not check the DAT of the received response to the message. 

The sendAndCheckDat() returns a Map<String, String> where, for example, response.get("header") and response.get("payload") can be used to access the message-fields.

For extended instructions and info on the other modules, see the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki">GitHub-Wiki</a>.

## Other: Log-Codes

Log-codes exist for different log-levels. They allow easy search for the code location that produced the log.

Syntax: IMS-XY-L-1234, shortened to: IMSXYL1234
 - IMS = IDS-Messaging-Services
 - XY = Subsystem Module (CO Core, AP AppStore, BR Broker, CL ClearingHouse, ME Messaging, PA Paris, VO Vocol)
 - L = Event Severity (E Error, W Warn, D Debug, I Info)
 - 1234 = Log identifier
 
Will e.g. print as [code=(IMSCOE0001)]: IDS-Messaging-Services Core-Module Error 0001.

## Other: Project-Wiki

This project has a <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki">GitHub-Wiki</a> where more details about the individual java-modules and their functionalities are documented.

## Other: Contributing

You are very welcome to contribute to this project when you find a bug, want to suggest an improvement, or have an idea for a useful feature. Please find a set of guidelines at the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/CONTRIBUTING.md">CONTRIBUTING-Guideline</a> and the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/CODE_OF_CONDUCT.md">CODE_OF_CONDUCT-Guideline</a>.

## Other: Questions and suggestions

For any questions or suggestions please refer to the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues">GitHub-Issues</a> or the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/discussions">GitHub-Discussion</a> area.

## Other: License

This project is licensed under the Apache License 2.0 - see the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/LICENSE">LICENSE</a> for details.
