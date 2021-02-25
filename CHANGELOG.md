# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

Documentation started on: 2021-01-20.

## Version 1.6-SNAPSHOT (UNRELEASED)
### Changed
- Received RejectionMessages are now passed to the connector-developer, regardless of the status of the DAT within the received RejectionMessage
- Asynchronous Message-Flow Support (support RequestInProcessMessage-Handling)
- Initial IDS-Broker support
- Initial IDS-ClearingHouse support
- Code refactoring

## Version 1.5-SNAPSHOT
### Removed
- Remove initial untested implementation of MQTT-support
### Added
- Support for multiple DAPS modes via application.properties (daps.mode = aisec/orbiter)
### Changed
- TokenProviderService will only get a new token, if the current one expired

## Version 1.4-SNAPSHOT
### Added
- New Exception structure for throwing exceptions for TokenManagerService
### Changed
- Extension of TEST_DEPLOYMENT-Mode: use an all-trusting trustmanager, accepting all SSL Certificates. This allows the use of selfsigned certificates in a TEST-environment

## Version 1.3-SNAPSHOT
### Changed
- The connector developer can now specify a protocol with which he wants to transmit his message
- Include ID of rejected message in RejectionMessage
- Update referenced dependencies
- Code refactoring

## Version 1.2-SNAPSHOT
### Added
- Bugfix for KeyStoreManager & ClientProvider
- Support for DemoConnector
- Update used infomodel-serializer to version 4.0.2-SNAPSHOT (as requested)

## Version 1.1-SNAPSHOT
### Added
- Initial functionality for messaging and DAPS DAT based on the functionalities of the IDS-Framework mapped to the new architecture
- First refactorings of the code base in the core and messaging module

## Version 1.0-SNAPSHOT
### Added
- Project Setup: Basic architecture
- basic Changelog
