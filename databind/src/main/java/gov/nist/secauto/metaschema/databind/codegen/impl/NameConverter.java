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
import java.util.StringTokenizer;

import javax.lang.model.SourceVersion;

/**
 * Converts aribitrary strings into Java identifiers.
 *
 * @author <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface NameConverter {
  /**
   * The name converter implemented by Code Model.
   *
   * This is the standard name conversion for JAXB.
   */
  NameConverter STANDARD = new Standard();

  /**
   * converts a string into an identifier suitable for classes.
   *
   * In general, this operation should generate "NamesLikeThis".
   *
   * @param token
   *          the string to convert
   * @return the equivalent class name
   */
  String toClassName(String token);

  /**
   * converts a string into an identifier suitable for interfaces.
   *
   * In general, this operation should generate "NamesLikeThis". But for example,
   * it can prepend every interface with 'I'.
   *
   * @param token
   *          the string to convert
   * @return the equivalent interface name
   */
  String toInterfaceName(String token);

  /**
   * converts a string into an identifier suitable for properties.
   *
   * In general, this operation should generate "NamesLikeThis", which will be
   * used with known prefixes like "get" or "set".
   *
   * @param token
   *          the string to convert
   * @return the equivalent property name
   */
  String toPropertyName(String token);

  /**
   * converts a string into an identifier suitable for constants.
   *
   * In the standard Java naming convention, this operation should generate
   * "NAMES_LIKE_THIS".
   *
   * @param token
   *          the string to convert
   * @return the equivalent constant name
   */
  String toConstantName(String token);

  /**
   * Converts a string into an identifier suitable for variables.
   *
   * In general it should generate "namesLikeThis".
   *
   * @param token
   *          the string to convert
   * @return the equivalent variable name
   */
  String toVariableName(String token);

  /**
   * Converts a namespace URI into a package name. This method should expect
   * strings like "http://foo.bar.zot/org", "urn:abc:def:ghi" "", or even "###"
   * (basically anything) and expected to return a package name, liks
   * "org.acme.foo".
   *
   * @param namespaceUri
   *          the string to convert
   * @return the equivalent package name
   */
  String toPackageName(String namespaceUri);

  class Standard
      extends NameUtil
      implements NameConverter {

    /**
     * Default constructor.
     */
    public Standard() {
      // do nothing
    }

    @Override
    public String toClassName(String token) {
      return toMixedCaseName(toWordList(token), true);
    }

    @Override
    public String toVariableName(String token) {
      return toMixedCaseName(toWordList(token), false);
    }

    @Override
    public String toInterfaceName(String token) {
      return toClassName(token);
    }

    @Override
    public String toPropertyName(String token) {
      String prop = toClassName(token);
      // property name "Class" with collide with Object.getClass,
      // so escape this.
      if ("Class".equals(prop)) {
        prop = "Clazz";
      }
      return prop;
    }

    /**
     * Computes a Java package name from a namespace URI, as specified in the spec.
     *
     * @return {@code null} if it fails to derive a package name.
     */
    @SuppressWarnings({
        "PMD.CyclomaticComplexity", "PMD.NPathComplexity", // acceptable
        "PMD.OnlyOneReturn", // readability
        "PMD.UseStringBufferForStringAppends" // ok
    })
    @Override
    public String toPackageName(String uri) {
      String nsUri = uri;
      // remove scheme and :, if present
      // spec only requires us to remove 'http' and 'urn'...
      int idx = nsUri.indexOf(':');
      String scheme = "";
      if (idx >= 0) {
        scheme = nsUri.substring(0, idx);
        if ("http".equalsIgnoreCase(scheme) || "urn".equalsIgnoreCase(scheme)) {
          nsUri = nsUri.substring(idx + 1);
        }
      }

      // tokenize string
      List<String> tokens = tokenize(nsUri, "/: ");
      if (tokens.isEmpty()) {
        return null;
      }

      // remove trailing file type, if necessary
      if (tokens.size() > 1) {
        // for uri's like "www.foo.com" and "foo.com", there is no trailing
        // file, so there's no need to look at the last '.' and substring
        // otherwise, we loose the "com" (which would be wrong)
        String lastToken = tokens.get(tokens.size() - 1);
        idx = lastToken.lastIndexOf('.');
        if (idx > 0) {
          lastToken = lastToken.substring(0, idx);
          tokens.set(tokens.size() - 1, lastToken);
        }
      }

      // tokenize domain name and reverse. Also remove :port if it exists
      String domain = tokens.get(0);
      idx = domain.indexOf(':');
      if (idx >= 0) {
        domain = domain.substring(0, idx);
      }
      List<String> rev = reverse(tokenize(domain, "urn".equals(scheme) ? ".-" : "."));
      if ("www".equalsIgnoreCase(rev.get(rev.size() - 1))) {
        // remove leading www
        rev.remove(rev.size() - 1);
      }

      // replace the domain name with tokenized items
      tokens.addAll(1, rev);
      tokens.remove(0);

      // iterate through the tokens and apply xml->java name algorithm
      for (int i = 0; i < tokens.size(); i++) {

        // get the token and remove illegal chars
        String token = tokens.get(i);
        token = removeIllegalIdentifierChars(token);

        // this will check for reserved keywords
        if (SourceVersion.isKeyword(token.toLowerCase(Locale.ENGLISH))) {
          token = '_' + token;
        }

        tokens.set(i, token.toLowerCase(Locale.ENGLISH));
      }

      // concat all the pieces and return it
      return combine(tokens, '.');
    }

    private static String removeIllegalIdentifierChars(String token) {
      StringBuilder newToken = new StringBuilder(token.length() + 1); // max expected length
      for (int i = 0; i < token.length(); i++) {
        char ch = token.charAt(i);
        if (i == 0 && !Character.isJavaIdentifierStart(ch)) { // ch can't be used as FIRST char
          newToken.append('_');
        }
        if (Character.isJavaIdentifierPart(ch)) {
          newToken.append(ch); // ch is valid
        } else {
          newToken.append('_'); // ch can't be used
        }
      }
      return newToken.toString();
    }

    private static List<String> tokenize(String str, String sep) {
      StringTokenizer tokens = new StringTokenizer(str, sep);
      ArrayList<String> retval = new ArrayList<>();

      while (tokens.hasMoreTokens()) {
        retval.add(tokens.nextToken());
      }

      return retval;
    }

    private static <T> List<T> reverse(List<T> list) {
      List<T> rev = new ArrayList<>(list.size());

      for (int i = list.size() - 1; i >= 0; i--) {
        rev.add(list.get(i));
      }

      return rev;
    }

    private static String combine(List<String> tokens, char sep) {
      StringBuilder buf = new StringBuilder(tokens.get(0));

      for (int i = 1; i < tokens.size(); i++) {
        buf.append(sep);
        buf.append(tokens.get(i));
      }

      return buf.toString();
    }
  }
}
