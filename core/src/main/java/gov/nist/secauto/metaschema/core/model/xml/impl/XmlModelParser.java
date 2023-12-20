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
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IGroupedFieldInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IStandardGroupedModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.IStandardModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.AssemblyModelType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.AssemblyReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ChoiceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.FieldReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupAsType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedAssemblyReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedChoiceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedFieldReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedInlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.math.BigInteger;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

//@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class XmlModelParser {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<Pair<IAssemblyDefinition, IStandardModelContainerSupport>> ASSEMBLY_MODEL_PARSER
      = new XmlObjectParser<>(ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "assembly"),
                  XmlModelParser::handleAssemmbly),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-assembly"),
                  XmlModelParser::handleDefineAssembly),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "field"),
                  XmlModelParser::handleField),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-field"),
                  XmlModelParser::handleDefineField),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "choice"),
                  XmlModelParser::handleChoice),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "choice-group"),
                  XmlModelParser::handleChoiceGroup)))) {

        @Override
        protected Handler<Pair<IAssemblyDefinition, IStandardModelContainerSupport>> identifyHandler(
            XmlCursor cursor,
            XmlObject obj) {
          Handler<Pair<IAssemblyDefinition, IStandardModelContainerSupport>> retval;
          if (obj instanceof FieldReferenceType) {
            retval = XmlModelParser::handleField;
          } else if (obj instanceof InlineFieldDefinitionType) {
            retval = XmlModelParser::handleDefineField;
          } else if (obj instanceof AssemblyReferenceType) {
            retval = XmlModelParser::handleAssemmbly;
          } else if (obj instanceof InlineAssemblyDefinitionType) {
            retval = XmlModelParser::handleDefineAssembly;
          } else if (obj instanceof ChoiceType) {
            retval = XmlModelParser::handleChoice;
          } else if (obj instanceof GroupedChoiceType) {
            retval = XmlModelParser::handleChoiceGroup;
          } else {
            retval = super.identifyHandler(cursor, obj);
          }
          return retval;
        }
      };

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<Pair<IChoiceInstance, IStandardModelContainerSupport>> CHOICE_MODEL_PARSER
      = new XmlObjectParser<>(ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "assembly"),
                  XmlModelParser::handleChoiceAssemmbly),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-assembly"),
                  XmlModelParser::handleChoiceDefineAssembly),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "field"),
                  XmlModelParser::handleChoiceField),
              Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-field"),
                  XmlModelParser::handleChoiceDefineField)))) {

        @Override
        protected Handler<Pair<IChoiceInstance, IStandardModelContainerSupport>> identifyHandler(
            XmlCursor cursor,
            XmlObject obj) {
          Handler<Pair<IChoiceInstance, IStandardModelContainerSupport>> retval;
          if (obj instanceof FieldReferenceType) {
            retval = XmlModelParser::handleChoiceField;
          } else if (obj instanceof InlineFieldDefinitionType) {
            retval = XmlModelParser::handleChoiceDefineField;
          } else if (obj instanceof AssemblyReferenceType) {
            retval = XmlModelParser::handleChoiceAssemmbly;
          } else if (obj instanceof InlineAssemblyDefinitionType) {
            retval = XmlModelParser::handleChoiceDefineAssembly;
          } else {
            retval = super.identifyHandler(cursor, obj);
          }
          return retval;
        }
      };

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<
      Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport>> CHOICE_GROUP_PARSER
          = new XmlObjectParser<>(ObjectUtils.notNull(
              Map.ofEntries(
                  Map.entry(new QName(IModule.METASCHEMA_XML_NS, "assembly"),
                      XmlModelParser::handleGroupedAssembly),
                  Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-assembly"),
                      XmlModelParser::handleGroupedDefineAssembly),
                  Map.entry(new QName(IModule.METASCHEMA_XML_NS, "field"),
                      XmlModelParser::handleGroupedField),
                  Map.entry(new QName(IModule.METASCHEMA_XML_NS, "define-field"),
                      XmlModelParser::handleGroupedDefineField)))) {

            @Override
            protected Handler<Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport>> identifyHandler(
                XmlCursor cursor,
                XmlObject obj) {
              Handler<Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport>> retval;
              if (obj instanceof GroupedFieldReferenceType) {
                retval = XmlModelParser::handleGroupedField;
              } else if (obj instanceof GroupedInlineFieldDefinitionType) {
                retval = XmlModelParser::handleGroupedDefineField;
              } else if (obj instanceof GroupedAssemblyReferenceType) {
                retval = XmlModelParser::handleGroupedAssembly;
              } else if (obj instanceof GroupedInlineAssemblyDefinitionType) {
                retval = XmlModelParser::handleGroupedDefineAssembly;
              } else {
                retval = super.identifyHandler(cursor, obj);
              }
              return retval;
            }
          };

  private XmlModelParser() {
    // disable construction
  }

  private static void handleField(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    IFieldInstance field = new XmlFieldInstance(
        (FieldReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleDefineField(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    IFieldInstance field = new XmlInlineFieldDefinition(
        (InlineFieldDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleAssemmbly(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    IAssemblyInstance assembly = new XmlAssemblyInstance(
        (AssemblyReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(assembly, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleDefineAssembly(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    IAssemblyInstance assembly = new XmlInlineAssemblyDefinition(
        (InlineAssemblyDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(assembly, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleChoice(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    XmlChoiceInstance choice = new XmlChoiceInstance(
        (ChoiceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().getModelInstances().add(choice);
  }

  private static void handleChoiceGroup(
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, IStandardModelContainerSupport> state) {
    XmlChoiceGroupInstance choice = new XmlChoiceGroupInstance(
        (GroupedChoiceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    state.getRight().getModelInstances().add(choice);
  }

  private static void handleChoiceField(
      @NonNull XmlObject obj,
      Pair<IChoiceInstance, IStandardModelContainerSupport> state) {
    IFieldInstance field = new XmlFieldInstance(
        (FieldReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleChoiceDefineField(
      @NonNull XmlObject obj,
      Pair<IChoiceInstance, IStandardModelContainerSupport> state) {
    IFieldInstance field = new XmlInlineFieldDefinition(
        (InlineFieldDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleChoiceAssemmbly(
      @NonNull XmlObject obj,
      Pair<IChoiceInstance, IStandardModelContainerSupport> state) {
    IAssemblyInstance assembly = new XmlAssemblyInstance(
        (AssemblyReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(assembly, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleChoiceDefineAssembly(
      @NonNull XmlObject obj,
      Pair<IChoiceInstance, IStandardModelContainerSupport> state) {
    IAssemblyInstance assembly = new XmlInlineAssemblyDefinition(
        (InlineAssemblyDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(assembly, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleGroupedField(
      @NonNull XmlObject obj,
      Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport> state) {
    IGroupedFieldInstance field = new XmlGroupedFieldInstance(
        (GroupedFieldReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleGroupedDefineField(
      @NonNull XmlObject obj,
      Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport> state) {
    IGroupedFieldInstance field = new XmlGroupedInlineFieldDefinition(
        (GroupedInlineFieldDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleGroupedAssembly(
      @NonNull XmlObject obj,
      Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport> state) {
    IGroupedAssemblyInstance field = new XmlGroupedAssemblyInstance(
        (GroupedAssemblyReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void handleGroupedDefineAssembly(
      @NonNull XmlObject obj,
      Pair<IChoiceGroupInstance, IStandardGroupedModelContainerSupport> state) {
    IGroupedAssemblyInstance field = new XmlGroupedInlineAssemblyDefinition(
        (GroupedInlineAssemblyDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    append(field, ObjectUtils.notNull(state.getRight()));
  }

  private static void append(
      @NonNull IFieldInstance field,
      @NonNull IStandardModelContainerSupport container) {
    String key = field.getEffectiveName();
    container.getFieldInstanceMap().put(key, field);
    container.getNamedModelInstanceMap().put(key, field);
    container.getModelInstances().add(field);
  }

  private static void append(
      @NonNull IAssemblyInstance assembly,
      @NonNull IStandardModelContainerSupport container) {
    String key = assembly.getEffectiveName();
    container.getAssemblyInstanceMap().put(key, assembly);
    container.getNamedModelInstanceMap().put(key, assembly);
    container.getModelInstances().add(assembly);
  }

  private static void append(
      @NonNull IGroupedFieldInstance field,
      @NonNull IStandardGroupedModelContainerSupport container) {
    String key = field.getEffectiveName();
    container.getFieldInstanceMap().put(key, field);
    container.getNamedModelInstanceMap().put(key, field);
  }

  private static void append(
      @NonNull IGroupedAssemblyInstance assembly,
      @NonNull IStandardGroupedModelContainerSupport container) {
    String key = assembly.getEffectiveName();
    container.getAssemblyInstanceMap().put(key, assembly);
    container.getNamedModelInstanceMap().put(key, assembly);
  }

  /**
   * Parse a choice XMLBeans object.
   *
   * @param xmlObject
   *          the XMLBeans object
   * @param parent
   *          the parent Metaschema node, either an assembly definition or choice
   * @param container
   *          the model container
   */
  public static void parseChoice(
      @NonNull ChoiceType xmlObject,
      @NonNull IChoiceInstance parent,
      @NonNull IStandardModelContainerSupport container) {
    CHOICE_MODEL_PARSER.parse(xmlObject, Pair.of(parent, container));
  }

  /**
   * Parse a choice group XMLBeans object.
   *
   * @param xmlObject
   *          the XMLBeans object
   * @param parent
   *          the parent Metaschema node, either an assembly definition or choice
   * @param container
   *          the model container
   */
  public static void parseChoiceGroup(
      @NonNull GroupedChoiceType xmlObject,
      @NonNull IChoiceGroupInstance parent,
      @NonNull IStandardGroupedModelContainerSupport container) {
    CHOICE_GROUP_PARSER.parse(xmlObject, Pair.of(parent, container));
  }

  /**
   * Parse a choice XMLBeans object.
   *
   * @param xmlObject
   *          the XMLBeans object
   * @param parent
   *          the parent Metaschema assembly definition
   * @param container
   *          the model container
   */
  public static void parseModel(
      @NonNull AssemblyModelType xmlObject,
      @NonNull IAssemblyDefinition parent,
      @NonNull IStandardModelContainerSupport container) {
    ASSEMBLY_MODEL_PARSER.parse(xmlObject, Pair.of(parent, container));
  }

  @NonNull
  public static JsonGroupAsBehavior getJsonGroupAsBehavior(@Nullable GroupAsType groupAs) {
    JsonGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR;
    if (groupAs != null && groupAs.isSetInJson()) {
      retval = ObjectUtils.notNull(groupAs.getInJson());
    }
    return retval;
  }

  @NonNull
  public static XmlGroupAsBehavior getXmlGroupAsBehavior(@Nullable GroupAsType groupAs) {
    XmlGroupAsBehavior retval = MetaschemaModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR;
    if (groupAs != null && groupAs.isSetInXml()) {
      retval = ObjectUtils.notNull(groupAs.getInXml());
    }
    return retval;
  }

  public static int getMinOccurs(@Nullable BigInteger value) {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;
    if (value != null) {
      retval = value.intValueExact();
    }
    return retval;
  }

  public static int getMaxOccurs(@Nullable Object value) {
    int retval = MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS;
    if (value != null) {
      if (value instanceof String) {
        // unbounded
        retval = -1;
      } else if (value instanceof BigInteger) {
        retval = ((BigInteger) value).intValueExact();
      } else {
        throw new IllegalStateException("Invalid type: " + value.getClass().getName());
      }
    }
    return retval;
  }
}
