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
import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IContainerModelAssemblySupport;
import gov.nist.secauto.metaschema.core.model.IFieldInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.AssemblyModelType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.AssemblyReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ChoiceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.FieldReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GroupedChoiceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.InlineFieldDefinitionType;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class XmlAssemblyModelContainer
    extends DefaultContainerModelAssemblySupport<
        IModelInstanceAbsolute,
        INamedModelInstanceAbsolute,
        IFieldInstanceAbsolute,
        IAssemblyInstanceAbsolute,
        IChoiceInstance,
        IChoiceGroupInstance> {
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @NonNull
  private static final XmlObjectParser<Pair<IAssemblyDefinition, XmlAssemblyModelContainer>> XML_MODEL_PARSER
      = new XmlObjectParser<>(ObjectUtils.notNull(
          Map.ofEntries(
              Map.entry(new QName(IModule.XML_NAMESPACE, "assembly"),
                  XmlAssemblyModelContainer::handleAssemmbly),
              Map.entry(new QName(IModule.XML_NAMESPACE, "define-assembly"),
                  XmlAssemblyModelContainer::handleDefineAssembly),
              Map.entry(new QName(IModule.XML_NAMESPACE, "field"),
                  XmlAssemblyModelContainer::handleField),
              Map.entry(new QName(IModule.XML_NAMESPACE, "define-field"),
                  XmlAssemblyModelContainer::handleDefineField),
              Map.entry(new QName(IModule.XML_NAMESPACE, "choice"),
                  XmlAssemblyModelContainer::handleChoice),
              Map.entry(new QName(IModule.XML_NAMESPACE, "choice-group"),
                  XmlAssemblyModelContainer::handleChoiceGroup)))) {

        @Override
        protected Handler<Pair<IAssemblyDefinition, XmlAssemblyModelContainer>>
            identifyHandler(XmlCursor cursor, XmlObject obj) {
          Handler<Pair<IAssemblyDefinition, XmlAssemblyModelContainer>> retval;
          if (obj instanceof FieldReferenceType) {
            retval = XmlAssemblyModelContainer::handleField;
          } else if (obj instanceof InlineFieldDefinitionType) {
            retval = XmlAssemblyModelContainer::handleDefineField;
          } else if (obj instanceof AssemblyReferenceType) {
            retval = XmlAssemblyModelContainer::handleAssemmbly;
          } else if (obj instanceof InlineAssemblyDefinitionType) {
            retval = XmlAssemblyModelContainer::handleDefineAssembly;
          } else if (obj instanceof ChoiceType) {
            retval = XmlAssemblyModelContainer::handleChoice;
          } else if (obj instanceof GroupedChoiceType) {
            retval = XmlAssemblyModelContainer::handleChoiceGroup;
          } else {
            retval = super.identifyHandler(cursor, obj);
          }
          return retval;
        }
      };

  /**
   * Parse an assembly XMLBeans object.
   *
   * @param xmlObject
   *          the XMLBeans assembly model object, which may be {@code null}
   * @param parent
   *          the parent assembly definition, either an assembly definition or
   *          choice
   * @return the model container
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static IContainerModelAssemblySupport<
      IModelInstanceAbsolute,
      INamedModelInstanceAbsolute,
      IFieldInstanceAbsolute,
      IAssemblyInstanceAbsolute,
      IChoiceInstance,
      IChoiceGroupInstance> of(
          @Nullable AssemblyModelType xmlObject,
          @NonNull IAssemblyDefinition parent) {
    return xmlObject == null
        ? IContainerModelAssemblySupport.empty()
        : XML_MODEL_PARSER
            .parse(ObjectUtils.notNull(xmlObject), Pair.of(parent, new XmlAssemblyModelContainer()))
            .getRight();
  }

  private static void handleField( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    IFieldInstanceAbsolute instance = new XmlFieldInstance(
        (FieldReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    ObjectUtils.notNull(state.getRight()).append(instance);
  }

  private static void handleDefineField( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    IFieldInstanceAbsolute instance = new XmlInlineFieldDefinition(
        (InlineFieldDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    ObjectUtils.notNull(state.getRight()).append(instance);
  }

  private static void handleAssemmbly( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    IAssemblyInstanceAbsolute instance = new XmlAssemblyInstance(
        (AssemblyReferenceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    ObjectUtils.notNull(state.getRight()).append(instance);
  }

  private static void handleDefineAssembly( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    IAssemblyInstanceAbsolute instance = new XmlInlineAssemblyDefinition(
        (InlineAssemblyDefinitionType) obj,
        ObjectUtils.notNull(state.getLeft()));
    ObjectUtils.notNull(state.getRight()).append(instance);
  }

  private static void handleChoice( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    XmlChoiceInstance instance = new XmlChoiceInstance(
        (ChoiceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    ObjectUtils.notNull(state.getRight()).append(instance);
  }

  private static void handleChoiceGroup( // NOPMD false positive
      @NonNull XmlObject obj,
      Pair<IAssemblyDefinition, XmlAssemblyModelContainer> state) {
    XmlChoiceGroupInstance instance = new XmlChoiceGroupInstance(
        (GroupedChoiceType) obj,
        ObjectUtils.notNull(state.getLeft()));
    XmlAssemblyModelContainer container = ObjectUtils.notNull(state.getRight());

    String groupAsName = instance.getGroupAsName();
    if (groupAsName == null) {
      String location = XmlObjectParser.toLocation(obj);
      String locationCtx = location == null ? "" : " at location " + location;
      throw new IllegalArgumentException(
          String.format("Missing group-as for a choice group within the definition '%s'%s.",
              instance.getContainingDefinition().getName(),
              locationCtx));
    }
    container.getChoiceGroupInstanceMap().put(groupAsName, instance);
    container.getModelInstances().add(instance);
  }

  public void append(@NonNull IFieldInstanceAbsolute instance) {
    QName key = instance.getXmlQName();
    getFieldInstanceMap().put(key, instance);
    getNamedModelInstanceMap().put(key, instance);
    getModelInstances().add(instance);
  }

  public void append(@NonNull IAssemblyInstanceAbsolute instance) {
    QName key = instance.getXmlQName();
    getAssemblyInstanceMap().put(key, instance);
    getNamedModelInstanceMap().put(key, instance);
    getModelInstances().add(instance);
  }

  public void append(@NonNull IChoiceInstance instance) {
    getChoiceInstances().add(instance);
    getModelInstances().add(instance);
  }
}
