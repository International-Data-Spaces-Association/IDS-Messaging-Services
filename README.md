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

..

## Quick Start: Integration into a Maven-Java-Project

..

## Quick Start: First steps towards use

..

## Other: Project-Wiki

..

## Other: Contributing

..

## Other: Current contact persons

..

## Other: License

..
