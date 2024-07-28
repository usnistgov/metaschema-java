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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IContainerFlagSupport;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagContainerBuilder;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.FlagReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFlagDefinitionType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class XmlFlagContainerSupport {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<Pair<IModelDefinition, IFlagContainerBuilder<IFlagInstance>>> XML_MODEL_PARSER
      = new XmlObjectParser<>(ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.XML_NAMESPACE, "flag"),
                  XmlFlagContainerSupport::handleFlag),
              Map.entry(new QName(IModule.XML_NAMESPACE, "define-flag"),
                  XmlFlagContainerSupport::handleDefineFlag)))) {

        @Override
        protected Handler<Pair<IModelDefinition, IFlagContainerBuilder<IFlagInstance>>> identifyHandler(
            XmlCursor cursor,
            XmlObject obj) {
          Handler<Pair<IModelDefinition, IFlagContainerBuilder<IFlagInstance>>> retval;
          if (obj instanceof FlagReferenceType) {
            retval = XmlFlagContainerSupport::handleFlag;
          } else if (obj instanceof InlineFlagDefinitionType) {
            retval = XmlFlagContainerSupport::handleDefineFlag;
          } else {
            retval = super.identifyHandler(cursor, obj);
          }
          return retval;
        }
      };

  private static void handleFlag( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IModelDefinition, IFlagContainerBuilder<IFlagInstance>> state) {
    XmlFlagInstance flagInstance = new XmlFlagInstance(
        (FlagReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().flag(flagInstance);
  }

  private static void handleDefineFlag( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IModelDefinition, IFlagContainerBuilder<IFlagInstance>> state) {
    XmlInlineFlagDefinition flagInstance = new XmlInlineFlagDefinition(
        (InlineFlagDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().flag(flagInstance);
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull GlobalFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container) {
    if (!xmlField.getFlagList().isEmpty() || !xmlField.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = xmlField.isSetJsonKey()
          ? IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(
              ObjectUtils.requireNonNull(xmlField.getJsonKey().getFlagRef())))
          : IContainerFlagSupport.builder();
      parseLocalFlags(xmlField, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull InlineFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container) {
    if (!xmlField.getFlagList().isEmpty() || !xmlField.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = xmlField.isSetJsonKey()
          ? IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(
              ObjectUtils.requireNonNull(xmlField.getJsonKey().getFlagRef())))
          : IContainerFlagSupport.builder();
      parseLocalFlags(xmlField, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull GroupedInlineFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container,
      @Nullable String jsonKeyName) {
    if (!xmlField.getFlagList().isEmpty() || !xmlField.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = jsonKeyName == null
          ? IContainerFlagSupport.builder()
          : IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(jsonKeyName));
      parseLocalFlags(xmlField, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull GlobalAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container) {
    if (!xmlAssembly.getFlagList().isEmpty() || !xmlAssembly.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = xmlAssembly.isSetJsonKey()
          ? IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(
              ObjectUtils.requireNonNull(xmlAssembly.getJsonKey().getFlagRef())))
          : IContainerFlagSupport.builder();
      parseLocalFlags(xmlAssembly, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull InlineAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container) {
    if (!xmlAssembly.getFlagList().isEmpty() || !xmlAssembly.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = xmlAssembly.isSetJsonKey()
          ? IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(
              ObjectUtils.requireNonNull(xmlAssembly.getJsonKey().getFlagRef())))
          : IContainerFlagSupport.builder();
      parseLocalFlags(xmlAssembly, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  /**
   * Generate a flag container from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  static IContainerFlagSupport<IFlagInstance> newInstance(
      @NonNull GroupedInlineAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container,
      @Nullable String jsonKeyName) {
    if (!xmlAssembly.getFlagList().isEmpty() || !xmlAssembly.getDefineFlagList().isEmpty()) {
      IFlagContainerBuilder<IFlagInstance> builder = jsonKeyName == null
          ? IContainerFlagSupport.builder()
          : IContainerFlagSupport.builder(container.getContainingModule().toFlagQName(jsonKeyName));
      parseLocalFlags(xmlAssembly, container, builder);
      return builder.build();
    }
    return IContainerFlagSupport.empty();
  }

  private static void parseLocalFlags(
      @NonNull XmlObject xmlObject,
      @NonNull IModelDefinition parent,
      @NonNull IFlagContainerBuilder<IFlagInstance> builder) {
    // handle flags
    XML_MODEL_PARSER.parse(xmlObject, Pair.of(parent, builder));
  }

  private XmlFlagContainerSupport() {
    // disable construction
  }
}
