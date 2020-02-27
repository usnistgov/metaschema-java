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

package gov.nist.secauto.metaschema.binding.io.json.writer;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlagUtil {
  private FlagUtil() {
    // disable construction
  }

  public static List<FlagPropertyBinding> filterFlags(Object obj, List<FlagPropertyBinding> flags,
      PropertyBindingFilter filter) throws BindingException {
    List<FlagPropertyBinding> retval;
    if (flags.isEmpty()) {
      retval = Collections.emptyList();
    } else {
      retval = new ArrayList<>(flags.size());
      for (FlagPropertyBinding flag : flags) {
        if (filter == null || !filter.filter(flag)) {
          Object flagValue = flag.getPropertyInfo().getValue(obj);
          if (flagValue != null) {
            retval.add(flag);
          }
        }
      }
      retval = Collections.unmodifiableList(retval);
    }
    return retval;
  }

  public static void writeFlags(Object obj, List<FlagPropertyBinding> flags, JsonWritingContext writingContext)
      throws BindingException, IOException {
    for (FlagPropertyBinding flagBinding : flags) {
      Object value = flagBinding.getPropertyInfo().getValue(obj);
      if (value != null) {
        writeFlag(flagBinding, value, writingContext);
      }
    }
  }

  public static void writeFlag(FlagPropertyBinding flagBinding, Object value, JsonWritingContext writingContext)
      throws IOException, BindingException {
    JsonGenerator generator = writingContext.getEventWriter();
    String name = flagBinding.getJsonFieldName(writingContext.getBindingContext());

    generator.writeFieldName(name);

    JavaTypeAdapter<?> adapter
        = writingContext.getBindingContext().getJavaTypeAdapter(flagBinding.getPropertyInfo().getItemType());

    adapter.writeJsonFieldValue(value, null, writingContext);
  }

  public static PropertyBindingFilter adjustFilterToIncludeFlag(PropertyBindingFilter filter,
      FlagPropertyBinding jsonValueKeyFlag) {
    PropertyBindingFilter retval;
    if (filter == null) {
      retval = (PropertyBinding binding) -> jsonValueKeyFlag.equals(binding);
    } else {
      retval = (PropertyBinding binding) -> filter.filter(binding) || jsonValueKeyFlag.equals(binding);
    }
    return retval;
  }

}
