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

package gov.nist.secauto.metaschema.core.metapath.function.library;

import com.google.auto.service.AutoService;

import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionLibrary;
import gov.nist.secauto.metaschema.core.metapath.function.IFunctionLibrary;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INcNameItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INonNegativeIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IPositiveIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;

@SuppressWarnings({ "removal", "deprecation" })
@AutoService(IFunctionLibrary.class)
public class DefaultFunctionLibrary
    extends FunctionLibrary {

  /**
   * Initialize the built-in function library.
   */
  public DefaultFunctionLibrary() { // NOPMD - intentional
    // https://www.w3.org/TR/xpath-functions-31/#func-abs
    registerFunction(FnAbs.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-adjust-dateTime-to-timezone
    // https://www.w3.org/TR/xpath-functions-31/#func-adjust-date-to-timezone
    // https://www.w3.org/TR/xpath-functions-31/#func-adjust-time-to-timezone
    // https://www.w3.org/TR/xpath-functions-31/#func-avg
    registerFunction(FnAvg.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-base-uri
    registerFunction(FnBaseUri.SIGNATURE_NO_ARG);
    registerFunction(FnBaseUri.SIGNATURE_ONE_ARG);
    // https://www.w3.org/TR/xpath-functions-31/#func-boolean
    registerFunction(FnBoolean.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-ceiling
    registerFunction(FnCeiling.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-compare
    registerFunction(FnCompare.SIGNATURE);
    registerFunction(FnConcat.SIGNATURE);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-concat
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-contains
    // https://www.w3.org/TR/xpath-functions-31/#func-count
    registerFunction(FnCount.SIGNATURE);
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-current-date
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-current-dateTime
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-current-time
    // https://www.w3.org/TR/xpath-functions-31/#func-data
    registerFunction(FnData.SIGNATURE_NO_ARG);
    registerFunction(FnData.SIGNATURE_ONE_ARG);
    // https://www.w3.org/TR/xpath-functions-31/#func-day-from-date
    // https://www.w3.org/TR/xpath-functions-31/#func-day-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-days-from-duration
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-deep-equal
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-distinct-values
    // https://www.w3.org/TR/xpath-functions-31/#func-doc
    registerFunction(FnDoc.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-doc-available
    // https://www.w3.org/TR/xpath-functions-31/#func-document-uri
    registerFunction(FnDocumentUri.SIGNATURE_NO_ARG);
    registerFunction(FnDocumentUri.SIGNATURE_ONE_ARG);
    // https://www.w3.org/TR/xpath-functions-31/#func-empty
    registerFunction(FnEmpty.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-encode-for-uri
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-ends-with
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-exactly-one
    // https://www.w3.org/TR/xpath-functions-31/#func-exists
    registerFunction(FnExists.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-false
    registerFunction(FnFalse.SIGNATURE);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-floor
    registerFunction(NumericFunction.signature(MetapathConstants.NS_METAPATH_FUNCTIONS, "floor", INumericItem::floor));
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-format-date
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-format-dateTime
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-format-integer
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-format-number
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-format-time
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-generate-id
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-has-children
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-head
    registerFunction(FnHead.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-hours-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-hours-from-duration
    // https://www.w3.org/TR/xpath-functions-31/#func-hours-from-time
    // https://www.w3.org/TR/xpath-functions-31/#func-implicit-timezone
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-index-of
    // https://www.w3.org/TR/xpath-functions-31/#func-innermost
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-insert-before
    registerFunction(FnInsertBefore.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-iri-to-uri
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-last
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-lower-case
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-matches
    // https://www.w3.org/TR/xpath-functions-31/#func-max
    registerFunction(FnMinMax.SIGNATURE_MAX);
    // https://www.w3.org/TR/xpath-functions-31/#func-min
    registerFunction(FnMinMax.SIGNATURE_MIN);
    // https://www.w3.org/TR/xpath-functions-31/#func-minutes-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-minutes-from-duration
    // https://www.w3.org/TR/xpath-functions-31/#func-minutes-from-time
    // https://www.w3.org/TR/xpath-functions-31/#func-month-from-date
    // https://www.w3.org/TR/xpath-functions-31/#func-month-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-months-from-duration
    // https://www.w3.org/TR/xpath-functions-31/#func-node-name
    // https://www.w3.org/TR/xpath-functions-31/#func-normalize-space
    // https://www.w3.org/TR/xpath-functions-31/#func-normalize-unicode
    // https://www.w3.org/TR/xpath-functions-31/#func-not
    registerFunction(FnNot.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-number
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-one-or-more
    // https://www.w3.org/TR/xpath-functions-31/#func-outermost
    // https://www.w3.org/TR/xpath-functions-31/#func-parse-ietf-date
    // https://www.w3.org/TR/xpath-functions-31/#func-path
    registerFunction(FnPath.SIGNATURE_NO_ARG);
    registerFunction(FnPath.SIGNATURE_ONE_ARG);
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-position
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-remove
    registerFunction(FnRemove.SIGNATURE);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-replace
    // https://www.w3.org/TR/xpath-functions-31/#func-resolve-uri
    registerFunction(FnResolveUri.SIGNATURE_ONE_ARG);
    registerFunction(FnResolveUri.SIGNATURE_TWO_ARG);
    // https://www.w3.org/TR/xpath-functions-31/#func-reverse
    registerFunction(FnReverse.SIGNATURE_ONE_ARG);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-root
    // https://www.w3.org/TR/xpath-functions-31/#func-round
    registerFunction(FnRound.SIGNATURE);
    registerFunction(FnRound.SIGNATURE_WITH_PRECISION);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-round-half-to-even
    // https://www.w3.org/TR/xpath-functions-31/#func-seconds-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-seconds-from-duration
    // https://www.w3.org/TR/xpath-functions-31/#func-seconds-from-time
    // https://www.w3.org/TR/xpath-functions-31/#func-starts-with
    registerFunction(FnStartsWith.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-static-base-uri
    registerFunction(FnStaticBaseUri.SIGNATURE);
    // P0: https://www.w3.org/TR/xpath-functions-31/#func-string
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-string-join
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-string-length
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-subsequence
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-substring
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-substring-after
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-substring-before
    // https://www.w3.org/TR/xpath-functions-31/#func-sum
    registerFunction(FnSum.SIGNATURE_ONE_ARG);
    registerFunction(FnSum.SIGNATURE_TWO_ARG);
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-tail
    registerFunction(FnTail.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-timezone-from-date
    // https://www.w3.org/TR/xpath-functions-31/#func-timezone-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-timezone-from-time
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-tokenize
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-translate
    // https://www.w3.org/TR/xpath-functions-31/#func-true
    registerFunction(FnTrue.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-unparsed-text
    // https://www.w3.org/TR/xpath-functions-31/#func-unparsed-text-available
    // https://www.w3.org/TR/xpath-functions-31/#func-unparsed-text-lines
    // P1: https://www.w3.org/TR/xpath-functions-31/#func-upper-case
    // https://www.w3.org/TR/xpath-functions-31/#func-year-from-date
    // https://www.w3.org/TR/xpath-functions-31/#func-year-from-dateTime
    // https://www.w3.org/TR/xpath-functions-31/#func-years-from-duration
    // P2: https://www.w3.org/TR/xpath-functions-31/#func-zero-or-one

    // https://www.w3.org/TR/xpath-functions-31/#func-array-get
    registerFunction(ArrayGet.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-size
    registerFunction(ArraySize.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-put
    registerFunction(ArrayPut.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-append
    registerFunction(ArrayAppend.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-subarray
    registerFunction(ArraySubarray.SIGNATURE_TWO_ARG);
    registerFunction(ArraySubarray.SIGNATURE_THREE_ARG);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-remove
    registerFunction(ArrayRemove.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-insert-before
    registerFunction(ArrayInsertBefore.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-head
    registerFunction(ArrayHead.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-tail
    registerFunction(ArrayTail.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-reverse
    registerFunction(ArrayReverse.SIGNATURE);
    // https://www.w3.org/TR/xpath-functions-31/#func-array-join
    registerFunction(ArrayJoin.SIGNATURE);
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-for-each
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-filter
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-fold-left
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-fold-right
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-for-each-pair
    // P3: https://www.w3.org/TR/xpath-functions-31/#func-array-sort
    // https://www.w3.org/TR/xpath-functions-31/#func-array-flatten
    registerFunction(ArrayFlatten.SIGNATURE);

    // xpath casting functions
    registerFunction(
        CastFunction.signature(MetapathConstants.NS_XML_SCHEMA, "boolean", IBooleanItem.class, IBooleanItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "date", IDateItem.class, IDateItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "dateTime", IDateTimeItem.class, IDateTimeItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "decimal", IDecimalItem.class, IDecimalItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "duration", IDurationItem.class, IDurationItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "integer", IIntegerItem.class, IIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "NCName", INcNameItem.class, INcNameItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "nonNegativeInteger", INonNegativeIntegerItem.class,
        INonNegativeIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "positiveInteger", IPositiveIntegerItem.class,
        IPositiveIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_XML_SCHEMA, "string", IStringItem.class, IStringItem::cast));

    // metapath casting functions
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "boolean", IBooleanItem.class, IBooleanItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "date", IDateItem.class, IDateItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "date-time", IDateTimeItem.class, IDateTimeItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "decimal", IDecimalItem.class, IDecimalItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "duration", IDurationItem.class, IDurationItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "integer", IIntegerItem.class, IIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "ncname", INcNameItem.class, INcNameItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "non-negative-integer", INonNegativeIntegerItem.class,
        INonNegativeIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "positive-integer", IPositiveIntegerItem.class,
        IPositiveIntegerItem::cast));
    registerFunction(CastFunction.signature(
        MetapathConstants.NS_METAPATH, "string", IStringItem.class, IStringItem::cast));

    // extra functions
    registerFunction(MpRecurseDepth.SIGNATURE_ONE_ARG);
    registerFunction(MpRecurseDepth.SIGNATURE_TWO_ARG);
  }

}
