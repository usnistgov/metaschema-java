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

package gov.nist.secauto.metaschema.databind.model;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.core.model.RootAssemblyDefinitionWrapper;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public class RootAssemblyDefinition
    extends RootAssemblyDefinitionWrapper<IAssemblyClassBinding>
    implements IRootAssemblyClassBinding {

  public RootAssemblyDefinition(@NonNull IAssemblyClassBinding rootDefinition) {
    super(rootDefinition);
  }

  @Override
  public IBindingContext getBindingContext() {
    return getRootDefinition().getBindingContext();
  }

  @Override
  public Class<?> getBoundClass() {
    return getRootDefinition().getBoundClass();
  }

  @Override
  public IBoundFlagInstance getJsonKeyFlagInstance() {
    // always null, since this is a root
    return null;
  }

  @Override
  public Map<String, ? extends IBoundNamedInstance>
      getNamedInstances(Predicate<IBoundFlagInstance> flagFilter) {
    return getRootDefinition().getNamedInstances(flagFilter);
  }

  @Override
  public void callBeforeDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getRootDefinition().callBeforeDeserialize(targetObject, parentObject);
  }

  @Override
  public void callAfterDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getRootDefinition().callAfterDeserialize(targetObject, parentObject);
  }

  @Override
  public void writeItem(Object item, QName parentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    getRootDefinition().writeItem(item, parentName, context);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper,
      IJsonWritingContext context) throws IOException {
    getRootDefinition().writeItems(items, writeObjectWrapper, context);
  }

  @Override
  public Object copyBoundObject(Object item, Object parentInstance) throws BindingException {
    return getRootDefinition().copyBoundObject(item, parentInstance);
  }

  @Override
  public IBoundFlagInstance getFlagInstanceByName(String name) {
    return getRootDefinition().getFlagInstanceByName(name);
  }

  @Override
  public Collection<? extends IBoundFlagInstance> getFlagInstances() {
    return getRootDefinition().getFlagInstances();
  }

  @Override
  public Collection<? extends IBoundNamedModelInstance> getModelInstances() {
    return getRootDefinition().getModelInstances();
  }

  @Override
  public Collection<? extends IBoundNamedModelInstance> getNamedModelInstances() {
    return getRootDefinition().getNamedModelInstances();
  }

  @Override
  public IBoundNamedModelInstance getModelInstanceByName(String name) {
    return getRootDefinition().getModelInstanceByName(name);
  }

  @Override
  public Collection<? extends IBoundFieldInstance> getFieldInstances() {
    return getRootDefinition().getFieldInstances();
  }

  @Override
  public IBoundFieldInstance getFieldInstanceByName(String name) {
    return getRootDefinition().getFieldInstanceByName(name);
  }

  @Override
  public Collection<? extends IBoundAssemblyInstance> getAssemblyInstances() {
    return getRootDefinition().getAssemblyInstances();
  }

  @Override
  public IBoundAssemblyInstance getAssemblyInstanceByName(String name) {
    return getRootDefinition().getAssemblyInstanceByName(name);
  }

  @Override
  public <CLASS> CLASS newInstance() throws BindingException {
    return getRootDefinition().newInstance();
  }

  @Override
  public void writeRoot(Object instance, IXmlWritingContext context) throws XMLStreamException, IOException {

    XMLStreamWriter2 writer = context.getWriter();

    writer.writeStartDocument("UTF-8", "1.0");

    QName rootQName = getRootXmlQName();

    NamespaceContext nsContext = writer.getNamespaceContext();
    String prefix = nsContext.getPrefix(rootQName.getNamespaceURI());
    if (prefix == null) {
      prefix = "";
    }

    writer.writeStartElement(prefix, rootQName.getLocalPart(), rootQName.getNamespaceURI());

    writeItem(instance, rootQName, context);

    writer.writeEndElement();
  }

  @Override
  public void writeRoot(Object instance, IJsonWritingContext context) throws IOException {

    @SuppressWarnings("resource") JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    // first read the initial START_OBJECT
    writer.writeStartObject();

    writer.writeFieldName(getRootJsonName());

    writeItems(CollectionUtil.singleton(instance), true, context);

    // end of root object
    writer.writeEndObject();
  }
}
