/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.io.json.parser;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.util.List;

public abstract class AbstractJsonReader<CLASS, CLASS_BINDING extends ClassBinding<
    CLASS>, OBJECT_PARSER extends BoundObjectParser<CLASS, CLASS_BINDING>> implements JsonReader<CLASS> {
  private final CLASS_BINDING classBinding;

  public AbstractJsonReader(CLASS_BINDING classBinding) {
    this.classBinding = classBinding;
  }

  protected CLASS_BINDING getClassBinding() {
    return classBinding;
  }

  protected abstract OBJECT_PARSER newObjectParser(PropertyBindingFilter filter, JsonParsingContext parsingContext)
      throws BindingException;

  @Override
  public List<CLASS> readJson(JsonParsingContext parsingContext, PropertyBindingFilter filter, boolean parseRoot)
      throws BindingException {

    OBJECT_PARSER parser = newObjectParser(filter, parsingContext);

    return readJsonInternal(parser, parseRoot);
  }

  protected List<CLASS> readJsonInternal(OBJECT_PARSER parser, @SuppressWarnings("unused") boolean parseRoot)
      throws BindingException {
    return parser.parseObjects();
  }
}
