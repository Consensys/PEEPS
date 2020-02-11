# PegaSys End to End Product test Suite

[![CircleCI](https://circleci.com/gh/PegaSysEng/PEEPS.svg?style=svg&circle-token=9bb4214a9d8baeee39bc1fbce181179460b414f5)](https://circleci.com/gh/PegaSysEng/PEEPS)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/PEEPS/blob/master/LICENSE)

[Governance](GOVERNANCE.md)

## Purpose
PEEPS is a product integration test suite. Tests are written levering a custom DSL that provides concise fluent language at an appropriate level of abstraction from the implementation details.

Conceptually PEEPS consists of two parts:
- A DSL (domain specific language) to easily author end to end tests. This layer providing the binding between the abstract language and specific implementations for each product.  
- End to End suite. Test cases that leverage the DSL to produce concise tests, at the appropriate level of abstraction for easy understanding by most.

## Architecture


