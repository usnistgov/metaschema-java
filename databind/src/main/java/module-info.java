/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
module gov.nist.secauto.metaschema.databind {
  // requirements
  requires java.base;
  requires java.compiler;

  requires transitive gov.nist.secauto.metaschema.core;

  requires com.ctc.wstx;
  requires com.fasterxml.jackson.dataformat.yaml;
  requires com.fasterxml.jackson.dataformat.xml;
  requires com.github.spotbugs.annotations;
  requires transitive com.squareup.javapoet;
  requires nl.talsmasoftware.lazy4j;
  requires transitive org.apache.commons.lang3;
  requires org.apache.logging.log4j;
  requires org.apache.xmlbeans;
  requires org.yaml.snakeyaml;

  requires flexmark.util.sequence;
  requires transitive com.google.auto.service;

  exports gov.nist.secauto.metaschema.databind;
  exports gov.nist.secauto.metaschema.databind.codegen;
  exports gov.nist.secauto.metaschema.databind.codegen.config;
  // exports gov.nist.secauto.metaschema.databind.codegen.typeinfo;
  exports gov.nist.secauto.metaschema.databind.io;
  exports gov.nist.secauto.metaschema.databind.io.json;
  exports gov.nist.secauto.metaschema.databind.io.xml;
  exports gov.nist.secauto.metaschema.databind.io.yaml;
  exports gov.nist.secauto.metaschema.databind.model;
  exports gov.nist.secauto.metaschema.databind.model.info;
  exports gov.nist.secauto.metaschema.databind.model.annotations;
  exports gov.nist.secauto.metaschema.databind.model.metaschema;
  exports gov.nist.secauto.metaschema.databind.model.binding.metaschema;

  // need to allow access to the generated XMLBeans files
  opens org.apache.xmlbeans.metadata.system.metaschema.codegen;
  opens gov.nist.secauto.metaschema.databind.codegen.xmlbeans;
  opens gov.nist.secauto.metaschema.databind.codegen.xmlbeans.impl;
  // opens gov.nist.secauto.metaschema.databind.model.metaschema.binding;
}
