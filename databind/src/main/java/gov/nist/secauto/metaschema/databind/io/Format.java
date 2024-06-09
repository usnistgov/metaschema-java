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

package gov.nist.secauto.metaschema.databind.io;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Selections of serialization formats.
 */
public enum Format {
  /**
   * The <a href="https://www.w3.org/XML/">Extensible Markup Language</a> format.
   */
  XML(".xml", Set.of()),
  /**
   * The <a href="https://www.json.org/">JavaScript Object Notation</a> format.
   */
  JSON(".json", Set.of()),
  /**
   * The <a href="https://yaml.org/">YAML Ain't Markup Language</a> format.
   */
  YAML(".yaml", Set.of(".yml"));

  private static final List<String> NAMES;

  @NonNull
  private final String defaultExtension;
  @NonNull
  private final Set<String> recognizedExtensions;

  static {
    NAMES = Arrays.stream(values())
        .map(format -> format.name().toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableList());
  }

  /**
   * Get a list of all format names in lowercase.
   *
   * @return the list of names
   */
  @SuppressFBWarnings(value = "MS_EXPOSE_REP", justification = "Exposes names provided by the enum")
  public static List<String> names() {
    return NAMES;
  }

  Format(@NonNull String defaultExtension, Set<String> otherExtensions) {
    this.defaultExtension = defaultExtension;

    Set<String> recognizedExtensions = new HashSet<>();
    recognizedExtensions.add(defaultExtension);
    recognizedExtensions.addAll(otherExtensions);

    this.recognizedExtensions = CollectionUtil.unmodifiableSet(recognizedExtensions);
  }

  /**
   * Get the default extension to use for the format.
   *
   * @return the default extension
   */
  @NonNull
  public Set<String> getRecognizedExtensions() {
    return recognizedExtensions;
  }

  /**
   * Get the default extension to use for the format.
   *
   * @return the default extension
   */
  @NonNull
  public String getDefaultExtension() {
    return defaultExtension;
  }
}
