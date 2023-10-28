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
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.FlagReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFlagDefinitionType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@SuppressWarnings("PMD.NullAssignment") // readability
class XmlFlagContainerSupport implements IFlagContainerSupport<IFlagInstance> {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<Pair<IFlagContainer, Map<String, IFlagInstance>>> FLAG_PARSER
      = new XmlObjectParser<>(ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "flag"),
                  XmlFlagContainerSupport::handleFlag),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-flag"),
                  XmlFlagContainerSupport::handleDefineFlag)))) {

        @Override
        protected Handler<Pair<IFlagContainer, Map<String, IFlagInstance>>> identifyHandler(XmlCursor cursor,
            XmlObject obj) {
          Handler<Pair<IFlagContainer, Map<String, IFlagInstance>>> retval;
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

  @NonNull
  private final Map<String, IFlagInstance> flagInstances;
  @Nullable
  private final IFlagInstance jsonKeyFlag;

  private static void handleFlag(
      @NonNull XmlObject obj,
      Pair<IFlagContainer, Map<String, IFlagInstance>> state) {
    XmlFlagInstance flagInstance = new XmlFlagInstance(
        (FlagReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().put(flagInstance.getEffectiveName(), flagInstance);
  }

  private static void handleDefineFlag(
      @NonNull XmlObject obj,
      Pair<IFlagContainer, Map<String, IFlagInstance>> state) {
    XmlInlineFlagDefinition flagInstance = new XmlInlineFlagDefinition(
        (InlineFlagDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().put(flagInstance.getEffectiveName(), flagInstance);
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull GlobalFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container) {
    // handle flags
    if (xmlField.getFlagList().size() > 0 || xmlField.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlField, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = xmlField.isSetJsonKey() ? flagInstances.get(xmlField.getJsonKey().getFlagRef()) : null;
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull InlineFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container) {
    // handle flags
    if (xmlField.getFlagList().size() > 0 || xmlField.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlField, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = xmlField.isSetJsonKey() ? flagInstances.get(xmlField.getJsonKey().getFlagRef()) : null;
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlField
   *          the XMLBeans instance
   * @param container
   *          the field containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull GroupedInlineFieldDefinitionType xmlField,
      @NonNull IFieldDefinition container) {
    // handle flags
    if (xmlField.getFlagList().size() > 0 || xmlField.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlField, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = null;
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlAssembly
   *          the XMLBeans instance
   * @param container
   *          the assembly containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull GlobalAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container) {
    // handle flags
    if (xmlAssembly.getFlagList().size() > 0 || xmlAssembly.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlAssembly, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = xmlAssembly.isSetJsonKey() ? flagInstances.get(xmlAssembly.getJsonKey().getFlagRef()) : null;
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlAssembly
   *          the XMLBeans instance
   * @param container
   *          the assembly containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull InlineAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container) {
    // handle flags
    if (xmlAssembly.getFlagList().size() > 0 || xmlAssembly.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlAssembly, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = xmlAssembly.isSetJsonKey() ? flagInstances.get(xmlAssembly.getJsonKey().getFlagRef()) : null;
  }

  /**
   * Generate a set of constraints from the provided XMLBeans instance.
   *
   * @param xmlAssembly
   *          the XMLBeans instance
   * @param container
   *          the assembly containing the flag
   */
  public XmlFlagContainerSupport(
      @NonNull GroupedInlineAssemblyDefinitionType xmlAssembly,
      @NonNull IAssemblyDefinition container) {
    // handle flags
    if (xmlAssembly.getFlagList().size() > 0 || xmlAssembly.getDefineFlagList().size() > 0) {
      this.flagInstances = parseLocalFlags(xmlAssembly, container);
    } else {
      this.flagInstances = CollectionUtil.emptyMap();
    }
    this.jsonKeyFlag = null;
  }

  /**
   * Get a mapping of flag effective name to flag instance.
   *
   * @return the mapping of flag effective name to flag instance
   */
  @Override
  @NonNull
  public Map<String, ? extends IFlagInstance> getFlagInstanceMap() {
    return flagInstances;
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    return jsonKeyFlag;
  }

  @NonNull
  private static Map<String, IFlagInstance> parseLocalFlags(@NonNull XmlObject xmlObject,
      @NonNull IFlagContainer parent) {
    // handle flags
    Map<String, IFlagInstance> flagInstances = new LinkedHashMap<>(); // NOPMD - intentional

    FLAG_PARSER.parse(xmlObject, Pair.of(parent, flagInstances));

    return flagInstances.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(flagInstances);
  }
}
