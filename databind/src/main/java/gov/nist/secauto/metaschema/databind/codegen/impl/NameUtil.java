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

/*
 * This code is based on https://github.com/eclipse-ee4j/jaxb-ri/blob/master/jaxb-ri/core/src/main/java/org/glassfish/jaxb/core/api/impl/NameUtil.java
 *
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package gov.nist.secauto.metaschema.databind.codegen.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Methods that convert strings into various formats.
 */
@SuppressWarnings("PMD")
class NameUtil {
  protected static boolean isPunct(char ch) {
    return ch == '-' || ch == '.' || ch == ':' || ch == '_' || ch == '·' || ch == '·' || ch == '۝' || ch == '۞';
  }

  /**
   * Capitalizes the first character of the specified string, and de-capitalize
   * the rest of characters.
   *
   * @param str
   *          the string to capitalize
   * @return the capitalized string
   */
  public static String capitalize(String str) {
    if (!Character.isLowerCase(str.charAt(0))) {
      return str;
    }
    StringBuilder sb = new StringBuilder(str.length());
    sb.append(String.valueOf(str.charAt(0)).toUpperCase(Locale.ENGLISH));
    sb.append(str.substring(1).toLowerCase(Locale.ENGLISH));
    return sb.toString();
  }

  // Precondition: s[start] is not punctuation
  @SuppressWarnings({
      "checkstyle:MissingSwitchDefaultCheck", // intentional
  })
  private static int nextBreak(String str, int start) {
    int len = str.length();

    char c1 = str.charAt(start);
    int t1 = classify(c1);

    for (int i = start + 1; i < len; i++) {
      // shift (c1,t1) into (c0,t0)
      // char c0 = c1; --- conceptually, but c0 won't be used
      int t0 = t1;

      c1 = str.charAt(i);
      t1 = classify(c1);

      switch (actionTable[t0 * 5 + t1]) {
      case ACTION_CHECK_PUNCT:
        if (isPunct(c1)) {
          return i;
        }
        break;
      case ACTION_CHECK_C2:
        if (i < len - 1) {
          char c2 = str.charAt(i + 1);
          if (Character.isLowerCase(c2)) {
            return i;
          }
        }
        break;
      case ACTION_BREAK:
        return i;
      }
    }
    return -1;
  }

  // the 5-category classification that we use in this code
  // to find work breaks
  protected static final int UPPER_LETTER = 0;
  protected static final int LOWER_LETTER = 1;
  protected static final int OTHER_LETTER = 2;
  protected static final int DIGIT = 3;
  protected static final int OTHER = 4;

  /**
   * Look up table for actions. type0*5+type1 would yield the action to be taken.
   */
  private static final byte[] actionTable = new byte[5 * 5];

  // action constants. see nextBreak for the meaning
  private static final byte ACTION_CHECK_PUNCT = 0;
  private static final byte ACTION_CHECK_C2 = 1;
  private static final byte ACTION_BREAK = 2;
  private static final byte ACTION_NOBREAK = 3;

  /**
   * Decide the action to be taken given the classification of the preceding
   * character 't0' and the classification of the next character 't1'.
   */
  private static byte decideAction(int t0, int t1) {
    if (t0 == OTHER && t1 == OTHER) {
      return ACTION_CHECK_PUNCT;
    }
    if ((t0 == DIGIT) ^ (t1 == DIGIT)) {
      return ACTION_BREAK;
    }
    if (t0 == LOWER_LETTER && t1 != LOWER_LETTER) {
      return ACTION_BREAK;
    }
    if ((t0 <= OTHER_LETTER) ^ (t1 <= OTHER_LETTER)) {
      return ACTION_BREAK;
    }
    if ((t0 == OTHER_LETTER) ^ (t1 == OTHER_LETTER)) {
      return ACTION_BREAK;
    }
    if (t0 == UPPER_LETTER && t1 == UPPER_LETTER) {
      return ACTION_CHECK_C2;
    }

    return ACTION_NOBREAK;
  }

  static {
    // initialize the action table
    for (int t0 = 0; t0 < 5; t0++) {
      for (int t1 = 0; t1 < 5; t1++) {
        actionTable[t0 * 5 + t1] = decideAction(t0, t1);
      }
    }
  }

  /**
   * Classify a character into 5 categories that determine the word break.
   *
   * @param ch
   *          the character
   * @return the categorization
   */
  protected static int classify(char ch) {
    switch (Character.getType(ch)) {
    case Character.UPPERCASE_LETTER:
      return UPPER_LETTER;
    case Character.LOWERCASE_LETTER:
      return LOWER_LETTER;
    case Character.TITLECASE_LETTER:
    case Character.MODIFIER_LETTER:
    case Character.OTHER_LETTER:
      return OTHER_LETTER;
    case Character.DECIMAL_DIGIT_NUMBER:
      return DIGIT;
    default:
      return OTHER;
    }
  }

  /**
   * Tokenizes a string into words and capitalizes the first character of each
   * word.
   * <p>
   * This method uses a change in character type as a splitter of two words. For
   * example, "abc100ghi" will be splitted into {"Abc", "100","Ghi"}.
   *
   * @param str
   *          the string to split into a word list
   * @return the word list
   */
  public static List<String> toWordList(String str) {
    ArrayList<String> retval = new ArrayList<>();
    int len = str.length();
    for (int i = 0; i < len;) {

      // Skip punctuation
      while (i < len) {
        if (!isPunct(str.charAt(i))) {
          break;
        }
        i++;
      }
      if (i >= len) {
        break;
      }

      // Find next break and collect word
      int breakPos = nextBreak(str, i);
      String word = (breakPos == -1) ? str.substring(i) : str.substring(i, breakPos);
      retval.add(escape(capitalize(word)));
      if (breakPos == -1) {
        break;
      }
      i = breakPos;
    }

    // we can't guarantee a valid Java identifier anyway,
    // so there's not much point in rejecting things in this way.
    // if (ss.size() == 0)
    // throw new IllegalArgumentException("Zero-length identifier");
    return retval;
  }

  protected static String toMixedCaseName(List<String> ss, boolean startUpper) {
    StringBuilder sb = new StringBuilder();
    if (!ss.isEmpty()) {
      sb.append(startUpper ? ss.get(0) : ss.get(0).toLowerCase(Locale.ENGLISH));
      for (int i = 1; i < ss.size(); i++) {
        sb.append(ss.get(i));
      }
    }
    return sb.toString();
  }

  protected static String toMixedCaseVariableName(String[] ss,
      boolean startUpper,
      boolean cdrUpper) {
    if (cdrUpper) {
      for (int i = 1; i < ss.length; i++) {
        ss[i] = capitalize(ss[i]);
      }
    }
    StringBuilder sb = new StringBuilder();
    if (ss.length > 0) {
      sb.append(startUpper ? ss[0] : ss[0].toLowerCase(Locale.ENGLISH));
      for (int i = 1; i < ss.length; i++) {
        sb.append(ss[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Formats a string into "THIS_KIND_OF_FORMAT_ABC_DEF".
   *
   * @param str
   *          the string to format
   * @return Always return a string but there's no guarantee that the generated
   *         code is a valid Java identifier.
   */
  public String toConstantName(String str) {
    return toConstantName(toWordList(str));
  }

  /**
   * Formats a string into "THIS_KIND_OF_FORMAT_ABC_DEF".
   *
   * @param ss
   *          a list of words
   * @return Always return a string but there's no guarantee that the generated
   *         code is a valid Java identifier.
   */
  public String toConstantName(List<String> ss) {
    StringBuilder sb = new StringBuilder();
    if (!ss.isEmpty()) {
      sb.append(ss.get(0).toUpperCase(Locale.ENGLISH));
      for (int i = 1; i < ss.size(); i++) {
        sb.append('_');
        sb.append(ss.get(i).toUpperCase(Locale.ENGLISH));
      }
    }
    return sb.toString();
  }

  /**
   * Escapes characters is the given string so that they can be printed by only
   * using US-ASCII characters.
   *
   * The escaped characters will be appended to the given StringBuffer.
   *
   * @param sb
   *          StringBuffer that receives escaped string.
   * @param str
   *          String to be escaped. <code>s.substring(start)</code> will be
   *          escaped and copied to the string buffer.
   * @param start
   *          the starting position in the string
   */
  @SuppressWarnings({
      "checkstyle:MissingSwitchDefaultCheck", // intentional
      "checkstyle:AvoidEscapedUnicodeCharactersCheck" // ok
  })
  public static void escape(StringBuilder sb, String str, int start) {
    int len = str.length();
    for (int i = start; i < len; i++) {
      char ch = str.charAt(i);
      if (Character.isJavaIdentifierPart(ch)) {
        sb.append(ch);
      } else {
        sb.append('_');
        if (ch <= '\u000f') {
          sb.append("000");
        } else if (ch <= '\u00ff') {
          sb.append("00");
        } else if (ch <= '\u0fff') {
          sb.append('0');
        }
        sb.append(Integer.toString(ch, 16));
      }
    }
  }

  /**
   * Escapes characters that are unusable as Java identifiers by replacing unsafe
   * characters with safe characters.
   */
  private static String escape(String str) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      if (!Character.isJavaIdentifierPart(str.charAt(i))) {
        StringBuilder sb = new StringBuilder(str.substring(0, i));
        escape(sb, str, i);
        return sb.toString();
      }
    }
    return str;
  }
}
