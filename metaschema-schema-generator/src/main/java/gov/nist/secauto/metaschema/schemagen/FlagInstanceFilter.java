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

package gov.nist.secauto.metaschema.schemagen;

import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class FlagInstanceFilter {
  private FlagInstanceFilter() {
    // disable construction
  }

  @NotNull
  public static Collection<@NotNull ? extends IFlagInstance> filterFlags(
      @NotNull Collection<@NotNull ? extends IFlagInstance> flags,
      IFlagInstance jsonKeyFlag) {
    Predicate<gov.nist.secauto.metaschema.model.common.IFlagInstance> filter = null;

    // determine if we need to filter a JSON key
    if (jsonKeyFlag != null) {
      filter = filterFlag(jsonKeyFlag);
    }
    return applyFilter(flags, filter);
  }

  @NotNull
  public static Collection<@NotNull ? extends IFlagInstance> filterFlags(
      @NotNull Collection<@NotNull ? extends IFlagInstance> flags,
      IFlagInstance jsonKeyFlag,
      IFlagInstance jsonValueKeyFlag) {
    Predicate<gov.nist.secauto.metaschema.model.common.IFlagInstance> filter = null;

    // determine if we need to filter a JSON key
    if (jsonKeyFlag != null) {
      filter = filterFlag(jsonKeyFlag);
    }

    // determine if we need to filter a JSON value key
    if (jsonValueKeyFlag != null) {
      Predicate<gov.nist.secauto.metaschema.model.common.IFlagInstance> jsonValueKeyFilter
          = filterFlag(jsonValueKeyFlag);
      if (filter == null) {
        filter = jsonValueKeyFilter;
      } else {
        filter = filter.and(jsonValueKeyFilter);
      }
    }

    return applyFilter(flags, filter);
  }

  @NotNull
  protected static Predicate<gov.nist.secauto.metaschema.model.common.IFlagInstance>
      filterFlag(@NotNull IFlagInstance flagToFilter) {
    return flag -> flag != flagToFilter;
  }

  @NotNull
  protected static Collection<@NotNull ? extends IFlagInstance> applyFilter(
      @NotNull Collection<@NotNull ? extends IFlagInstance> flags,
      Predicate<gov.nist.secauto.metaschema.model.common.IFlagInstance> filter) {
    Collection<@NotNull ? extends IFlagInstance> retval;
    if (filter == null) {
      retval = flags;
    } else {
      retval = ObjectUtils.notNull(flags.stream()
          .filter(filter)
          .collect(Collectors.toList()));
    }
    return retval;
  }
}
