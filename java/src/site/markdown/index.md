# Maven build support for NIST Security Automation Java projects

This project provides a Java implementation of a [Metaschema](https://pages.nist.gov/metaschema/) toolchain for generating schemas (planned), converters, model documentation (planned), and programming language-specific data APIs.

This project contains the following sub-modules:

- [metaschema-model](metaschema-model/): Provides a [Java API](metaschema-model/apidocs/index.html) for processing a Metaschema definition.
- [metaschema-java-codegen](metaschema-java-codegen/): A Java code generator [Java API](metaschema-java-codege/apidocs/index.html) that generate Java classes for a Metaschema definition. These generated classes can be used to read and write XML, JSON, and YAML content that is valid to the associated Metaschema model.
- [metaschema-java-binding](metaschema-java-binding/): The Java parser library providing a [Java API](metaschema-java-binding/apidocs/index.html) used by generated Java code to read and write XML, JSON, and YAML content that is valid to the associated Metaschema model. This library is a dependency for all generated Java code.
- [metaschema-java-datatypes](metaschema-java-datatypes/): A Java library that provides a [Java API](metaschema-java-datatypes/apidocs/index.html) supporting all Metaschema [datatypes](https://pages.nist.gov/metaschema/specification/syntax/#data-types) used by these tools.
- [metaschema-maven-plugin](metaschema-maven-plugin/): A Maven build plugin that automates generation on Java classes for a set of Metaschema definitions as part of a Maven build.

Please refer to each sub-module for usage instructions.
