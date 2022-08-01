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

package gov.nist.secauto.metaschema.model.common.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class ReplacementScanner {
  private ReplacementScanner() {
    // disable construction
  }

  /**
   * Search for instances of {@code pattern} in {@code text}. Replace each matching occurrence using
   * the {@code replacementFunction}.
   * 
   * @param text
   *          the text to search
   * @param pattern
   *          the pattern to search for
   * @param replacementFunction
   *          a function that will provided the replacement text
   * @return the resulting text after replacing matching occurrences in {@code text}
   */
  public static CharSequence replaceTokens(@NonNull CharSequence text, @NonNull Pattern pattern,
      Function<Matcher, CharSequence> replacementFunction) {
    int lastIndex = 0;
    StringBuilder retval = new StringBuilder();
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      retval.append(text, lastIndex, matcher.start())
          .append(replacementFunction.apply(matcher));

      lastIndex = matcher.end();
    }
    if (lastIndex < text.length()) {
      retval.append(text, lastIndex, text.length());
    }
    return retval;
  }
}
