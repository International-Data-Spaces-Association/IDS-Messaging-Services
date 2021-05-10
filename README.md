<p align="center">
<img src="ids-messaging-services-b140.PNG">
</p>

<p align="center">
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/development/LICENSE"><img src="https://img.shields.io/github/license/International-Data-Spaces-Association/IDS-Messaging-Services"></a>
<a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues"><img src="https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat"></a>
<img src="https://img.shields.io/github/workflow/status/International-Data-Spaces-Association/IDS-Messaging-Services/Development-Maven-Build">
<img src="https://img.shields.io/github/v/release/International-Data-Spaces-Association/IDS-Messaging-Services">
<img src="https://img.shields.io/github/languages/code-size/International-Data-Spaces-Association/IDS-Messaging-Services">
<img src="https://img.shields.io/github/contributors/International-Data-Spaces-Association/IDS-Messaging-Services">
<img src="https://img.shields.io/badge/Dependabot-Active-green">
</p>

# IDS-Messaging-Services

Each participant in an IDS-Data-Ecosystem must be able to communicate with other participants in the Data-Ecosystem. Even though different participants may have different implementations of IDS-Connectors, they all have in common that IDS-Messages need to be sent and received. This commonality is addressed by the IDS-Messaging-Services, which provide a lightweight available implementation of IDS-Message-Handling. The IDS-Messaging-Services offer open-source functionality for sending IDS-Messages as well as interfaces for processing received IDS-Messages. The architecture relies on a modern modular architecture-approach so that the functionalities needed for communication in a Data-Ecosystem can be easily selected specific to the infrastructure components present in the Data-Ecosystem. In addition, advanced functionalities are implemented, such as checking the validity of the Dynamic-Attribute-Token of incoming messages.

## Overview: Versioning

Due to the importance of the Infomodel-Java-Artifacts used in the IDS-Messaging-Services, the IDS-Messaging-Services follow an extended semantic versioning syntax for version numbering. The version number of the IDS-Messaging-Services artifacts is composed of 4 digits. The first digit of the version number is reserved for specifying the major version of the Infomodel-Java-Artifacts used. The digits 2 to 4 of the version number correspond to the actual versioning of the IDS-Messaging-Service artifact and are based on the widely known best-practice versioning according to Semantic-Versioning.

For example, the scheme is explained by the following version number:

**4.1.0.0** = Infomodel-Java-Artifacts Major Version 4 + IDS-Messaging-Services Semantic-Versioning 1.0.0

## Overview: IDS-Infomodel-Artifacts

Following is a list of all the major IDS-Infomodel-Artifacts and their versions that are used as dependencies.

| Group | Artifact | Version |
| ------ | ------ | ------ | 
| de.fraunhofer.iais.eis.ids.infomodel | java | 4.0.5 |
| de.fraunhofer.iais.eis.ids | infomodel-serializer | 4.0.5 |
| de.fraunhofer.iais.eis.ids | interaction | 4.0.5 |

## Overview: Supported IDS-Message protocols

The following transmission options for IDS-Messages are currently supported:
- HTTP Multipart

Currently being worked on:
- IDS-LDP / REST

## Overview: IDS-Infrastructure-Components

Supported simple out-of-the-box connectivity to the following IDS-Infrastructure-Components with advanced functionality:
- Other IDS-Connectors
- IDS-DAPS
- IDS-Broker
- IDS-ClearingHouse

Currently being worked on advanced functionality for:
- IDS-AppStore
- IDS-ParIS
- IDS-VoCol

## Quick Start: Technical requirements

- The IDS-Messaging-Services use asymmetric encryption concepts and requires public and private key of the Connector-Instance.
- The IDS-Messaging-Services utilize contents of the IDS-Configurationmodel which is part of the IDS-Informationmodel. Therefor a *configmodel.json*-file should exist to load the configuration of the IDS-Connector. For example, the configuration file should reference the key- and trust-store.

## Quick Start: Integration into a Maven-Java-Project

### Step 1

The Java-modules provided by the project are accessible as Maven artifact dependencies. These artifacts are hosted on Fraunhofer ISST's Nexus. In order for the artifacts to be found, the Fraunhofer ISST Nexus repository must be added as a repository in the project's pom:

```xml
<repositories>
    <repository>
        <id>isst-nexus-public</id>
        <name>isst-public</name>
        <url>https://mvn.ids.isst.fraunhofer.de/nexus/repository/ids-public/</url>
    </repository>
</repositories>
```

### Step 2

The current module artifact structure of the IDS-Messaging-Services is built along the different IDS-Infrastructure-Components, which are currently supported with advanced functionalities out-of-the-box:
- core
- messaging
- broker
- clearinghouse

In general, the core-module artifact is the main module artifact with the configuration of the IDS-Messaging-Services. The messaging-module artifact provides all needed functionalities to send and receive IDS-Messages. The messaging-module artifact in turn holds the core-module artifact as a dependency. So it is enough to specify the messaging-module artifact in the project's pom and the functionality of the core-module artifact will be loaded automatically.

The individual module-artifacts of the IDS-Infrastructure-Components in turn require functionalities of the messaging-module artifact and have it therefore linked as a dependency in each module case. This simplified architecture means that it is sufficient, for example, to integrate the broker-module artifact into the project's pom, which automatically makes the functionalities of the messaging- and thus also the core-module artifact available.


So, if an IDS-Connector should be implemented, in whose Data-Ecosystem an IDS-Broker occurs as IDS-Infrastructure-Component, the following entry in the project's pom is completely sufficient as dependcies to get all needed functionalities to exchange messages with the IDS-Broker with advanced functionalities:

```xml
<dependency>
    <groupId>de.fraunhofer.ids.messaging</groupId>
    <artifactId>broker</artifactId>
    <version>IDS_MESSAGING_SERVICES_VERSION</version>
</dependency>
```
Of course, the entry IDS_MESSAGING_SERVICES_VERSION must be exchanged with the desired version number of the IDS-Messaging-Services artifact.



## Quick Start: First steps towards use

..

## Other: Project-Wiki

This project has a <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/wiki">GitHub-Wiki</a> where more details about the individual java-modules and their functionalities are documented.

## Other: Contributing

You are very welcome to contribute to this project when you find a bug, want to suggest an improvement, or have an idea for a useful feature. Please find a set of guidelines at the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/CONTRIBUTING.md">CONTRIBUTING-Guideline</a> and the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/CODE_OF_CONDUCT.md">CODE_OF_CONDUCT-Guideline</a>.

## Other: Contact Persons

For any questions or suggestions that cannot be solved via <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/issues">GitHub-Issues</a> or the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/discussions">GitHub-Discussion</a> area, the following contacts are available:
* [Matthias Böckmann](https://github.com/maboeckmann), [Fraunhofer IAIS](https://www.iais.fraunhofer.de/)
* [Stefanie Koslowski](https://github.com/stefkoslowski), [Fraunhofer IAIS](https://www.iais.fraunhofer.de/)
* [Tim Berthold](https://github.com/tmberthold), [Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html)
* [Erik van den Akker](https://github.com/vdakker), [Fraunhofer ISST](https://www.isst.fraunhofer.de/en.html)

## Other: License

This project is licensed under the Apache License 2.0 - see the <a href="https://github.com/International-Data-Spaces-Association/IDS-Messaging-Services/blob/main/LICENSE">LICENSE</a> for details.
