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

package gov.nist.secauto.metaschema.core.metapath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class EQNameUtils {
  private static final Pattern URI_QUALIFIED_NAME = Pattern.compile("^Q\\{([^{}]*)\\}(.+)$");
  private static final Pattern LEXICAL_NAME = Pattern.compile("^(?:([^:]+):)?(.+)$");
  private static final Pattern NCNAME = Pattern.compile(String.format("^(\\p{L}|_)(\\p{L}|\\p{N}|[.\\-_])*$"));

  private EQNameUtils() {
    // disable construction
  }

  @NonNull
  public static QName parseName(
      @NonNull String name,
      @Nullable IEQNamePrefixResolver resolver) {
    Matcher matcher = URI_QUALIFIED_NAME.matcher(name);
    return matcher.matches()
        ? newUriQualifiedName(matcher)
        : parseLexicalQName(name, resolver);
  }

  @NonNull
  public static QName parseUriQualifiedName(@NonNull String name) {
    Matcher matcher = URI_QUALIFIED_NAME.matcher(name);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format("The name '%s' is not a valid BracedURILiteral of the form: Q{URI}local-name", name));
    }
    return newUriQualifiedName(matcher);
  }

  @NonNull
  private static QName newUriQualifiedName(@NonNull Matcher matcher) {
    return new QName(matcher.group(1), matcher.group(2));
  }

  @NonNull
  public static QName parseLexicalQName(
      @NonNull String name,
      @Nullable IEQNamePrefixResolver resolver) {
    Matcher matcher = LEXICAL_NAME.matcher(name);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format("The name '%s' is not a valid lexical QName of the form: prefix:local-name or local-name",
              name));
    }
    String prefix = matcher.group(1);

    if (prefix == null) {
      prefix = XMLConstants.DEFAULT_NS_PREFIX;
    }

    String namespace = resolver == null ? XMLConstants.NULL_NS_URI : resolver.resolve(prefix);
    return new QName(namespace, matcher.group(2), prefix);
  }

  public static boolean isNcName(@NonNull String name) {
    return NCNAME.matcher(name).matches();
  }

  @FunctionalInterface
  public interface IEQNamePrefixResolver {
    @NonNull
    String resolve(@NonNull String prefix);
  }
}
