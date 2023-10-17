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

package gov.nist.secauto.metaschema.core.metapath.antlr;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public abstract class Metapath10ParserBase
    extends Parser {
  protected Metapath10ParserBase(TokenStream input) {
    super(input);
  }

  protected boolean isFuncCall() {
    return !(getInputStream().LA(1) == Metapath10.KW_ARRAY
        || getInputStream().LA(1) == Metapath10.KW_ATTRIBUTE
        || getInputStream().LA(1) == Metapath10.KW_COMMENT
        || getInputStream().LA(1) == Metapath10.KW_DOCUMENT_NODE
        || getInputStream().LA(1) == Metapath10.KW_ELEMENT
        || getInputStream().LA(1) == Metapath10.KW_EMPTY_SEQUENCE
        || getInputStream().LA(1) == Metapath10.KW_FUNCTION
        || getInputStream().LA(1) == Metapath10.KW_IF
        || getInputStream().LA(1) == Metapath10.KW_ITEM
        || getInputStream().LA(1) == Metapath10.KW_MAP
        || getInputStream().LA(1) == Metapath10.KW_NAMESPACE_NODE
        || getInputStream().LA(1) == Metapath10.KW_NODE
        || getInputStream().LA(1) == Metapath10.KW_PROCESSING_INSTRUCTION
        || getInputStream().LA(1) == Metapath10.KW_SCHEMA_ATTRIBUTE
        || getInputStream().LA(1) == Metapath10.KW_SCHEMA_ELEMENT
        || getInputStream().LA(1) == Metapath10.KW_TEXT);
  }
}
