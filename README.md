# Ether
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d6feedbe4e5421693e9d20bacbd0e20)](https://app.codacy.com/gh/avaerian/Ether/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Code lines](https://sloc.xyz/github/avaerian/ether/?category=code)](https://github.com/avaerian/ether)

> [!IMPORTANT]
> Ether is currently under development; beware when testing posted dev builds.

Ether is an open-source skyblock plugin for Spigot and Paper servers, intended as a performant, expandable, and feature-packed option for skyblock servers. Codebase also contains a variety of useful tools for building future projects.

Ether takes a new approach to skyblock, providing an emphasis on automation, exploration, collaboration, and customizability. Gameplay features include:
- Assemble bots to automate farming/mining/slaying/etc.
- Travel interdimensional portals for unique resources and island expansion
- Visit other players' islands via warps (with or without warp signs; configurable)
- Play solo, or with a team on an island

#### Technical features include:
- Config registry for reading, writing, and updating configs
- Schematic API for reading, writing, and pasting schematics
  - Simple transforms are supported: rotate, flip 
- Support for async island management (see issue https://github.com/avaerian/Ether/issues/58)
- Support for custom dimensions from datapacks
- Database support
  - SQL: SQlite, PostgreSQL, H2, MySQL
  - BIN: under development; unsupported

---
## 🏗️ Getting Started

#### For server admins
To use, ~~simply download the compiled jar from the set of releases provided~~, and install by dragging and dropping the jar into the plugins folder on your Spigot or Paper server. Note that it is always preferrable to use on Paper servers for a better gain in performance; Spigot is available to allow for backwards compatibility, but discouraged.

- Get the release here: TBD

#### For developers
For interfacing, Ether is available as a dependency; TBD

To modify the source code, ensure Java SE 17 is installed. Ether uses Gradle as the utilized build system.
- TBD


## 📗 Documentation
Documentation is currently unavailable as the internals and API are in a volatile state. Contributions to any documentation or related changes are welcome; read the contribute section to learn more.

## 💖 Contribute
Any and all contributions are welcome; ensure that the contribution guidelines (TODO) are adhered to when making a pull request.
