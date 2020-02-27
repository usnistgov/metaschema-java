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
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractClassBinding<CLASS, XML_PARSE_PLAN extends XmlParsePlan<CLASS>, XML_WRITER extends XmlWriter>
    implements ClassBinding<CLASS> {
  private final Class<CLASS> clazz;
  private final List<FlagPropertyBinding> flagPropertyBindings;
  private final FlagPropertyBinding jsonKeyFlagPropertyBinding;
  private XML_PARSE_PLAN xmlParsePlan;
  private XML_WRITER xmlWriter;

  public AbstractClassBinding(Class<CLASS> clazz) throws BindingException {
    Objects.requireNonNull(clazz, "clazz");
    this.clazz = clazz;
    this.flagPropertyBindings = Collections.unmodifiableList(ClassIntrospector.getFlagPropertyBindings(clazz));

    FlagPropertyBinding jsonKey = null;
    for (FlagPropertyBinding flag : flagPropertyBindings) {
      if (flag.isJsonKey()) {
        jsonKey = flag;
        break;
      }
    }
    this.jsonKeyFlagPropertyBinding = jsonKey;
  }

  @Override
  public XML_PARSE_PLAN getXmlParsePlan(BindingContext bindingContext) throws BindingException {
    synchronized (this) {
      if (xmlParsePlan == null) {
        xmlParsePlan = newXmlParsePlan(bindingContext);
      }
      return xmlParsePlan;
    }
  }

  protected abstract XML_PARSE_PLAN newXmlParsePlan(BindingContext bindingContext) throws BindingException;

  @Override
  public XML_WRITER getXmlWriter() throws BindingException {
    synchronized (this) {
      if (xmlWriter == null) {
        xmlWriter = newXmlWriter();
      }
      return xmlWriter;
    }
  }

  protected abstract XML_WRITER newXmlWriter();

  @Override
  public Class<CLASS> getClazz() {
    return clazz;
  }

  @Override
  public List<FlagPropertyBinding> getFlagPropertyBindings() {
    return flagPropertyBindings;
  }

  @Override
  public FlagPropertyBinding getJsonKeyFlagPropertyBinding() {
    return jsonKeyFlagPropertyBinding;
  }

  @Override
  public Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext,
      PropertyBindingFilter filter) throws BindingException {
    Map<String, PropertyBinding> retval = new HashMap<>();
    List<FlagPropertyBinding> flags = getFlagPropertyBindings();

    if (!flags.isEmpty()) {
      for (NamedPropertyBinding binding : flags) {
        String jsonFieldName = binding.getJsonFieldName(bindingContext);
        if (jsonFieldName != null && retval.put(jsonFieldName, binding) != null) {
          throw new BindingException(
              String.format("The same field name '%s' is used on multiple properties.", jsonFieldName));
        }
      }
    }
    return retval;
  }

  @Override
  public boolean hasRootWrapper() {
    return getRootWrapper() != null;
  }

  @Override
  public CLASS newInstance() throws BindingException {
    Class<CLASS> clazz = getClazz();
    CLASS retval;
    try {
      Constructor<CLASS> constructor = (Constructor<CLASS>) clazz.getDeclaredConstructor();
      retval = constructor.newInstance();
    } catch (NoSuchMethodException e) {
      String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
      throw new BindingException(msg);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new BindingException(e);
    }
    return retval;
  }

}
