/**
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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.AssemblyJsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.AssemblyXmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

class AssemblyClassBindingImpl<CLASS>
    extends AbstractClassBinding<CLASS, AssemblyXmlParsePlan<CLASS>, AssemblyXmlWriter<CLASS>>
    implements AssemblyClassBinding<CLASS> {
  private final List<ModelItemPropertyBinding> modelItemPropertyBindings;
  private final RootWrapper rootWrapper;
  private QName rootName;
  private AssemblyJsonWriter<CLASS> assemblyJsonWriter;

  public AssemblyClassBindingImpl(Class<CLASS> clazz) throws BindingException {
    super(clazz);
    this.modelItemPropertyBindings = ClassIntrospector.getModelItemBindings(clazz);
    this.rootWrapper = clazz.getAnnotation(RootWrapper.class);
  }

  @Override
  public List<ModelItemPropertyBinding> getModelItemPropertyBindings() {
    return modelItemPropertyBindings;
  }

  @Override
  public Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext,
      PropertyBindingFilter filter) throws BindingException {
    Map<String, PropertyBinding> retval = super.getJsonPropertyBindings(bindingContext, filter);
    List<? extends NamedPropertyBinding> modelItems = getModelItemPropertyBindings();

    if (!modelItems.isEmpty()) {
      for (NamedPropertyBinding binding : modelItems) {
        String jsonFieldName = binding.getJsonFieldName(bindingContext);
        if (jsonFieldName != null && retval.put(jsonFieldName, binding) != null) {
          throw new BindingException(
              String.format("The same field name '%s' is used on multiple properties.", jsonFieldName));
        }
      }
    }
    return Collections.unmodifiableMap(retval);
  }

  @Override
  public boolean isRootElement() {
    return rootWrapper != null;
  }

  @Override
  public QName getRootQName() {
    synchronized (this) {
      if (rootWrapper != null && rootName == null) {
        String namespace = ModelUtil.resolveNamespace(rootWrapper.namespace(), getClazz());
        String localName = ModelUtil.resolveLocalName(rootWrapper.name(), getClazz().getSimpleName());
        rootName = new QName(namespace, localName);
      }
      return rootName;
    }
  }

  @Override
  public AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException {
    synchronized (this) {
      if (assemblyJsonWriter == null) {
        assemblyJsonWriter = newAssemblyJsonWriter();
      }
      return assemblyJsonWriter;
    }
  }

  @Override
  public AssemblyXmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
    return new AssemblyXmlParsePlan<CLASS>(this, bindingContext);
  }

  @Override
  public AssemblyXmlWriter<CLASS> newXmlWriter() {
    return new AssemblyXmlWriter<CLASS>(this);
  }

  public AssemblyJsonWriter<CLASS> newAssemblyJsonWriter() {
    return new AssemblyJsonWriter<CLASS>(this);
  }

  @Override
  public RootWrapper getRootWrapper() {
    return rootWrapper;
  }

  @Override
  public AssemblyJsonReader<CLASS> getJsonReader(BindingContext bindingContext) throws BindingException {
    return new AssemblyJsonReader<CLASS>(this);
  }

}
