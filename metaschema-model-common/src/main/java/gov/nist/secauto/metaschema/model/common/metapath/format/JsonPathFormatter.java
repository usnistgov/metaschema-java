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

package gov.nist.secauto.metaschema.model.common.metapath.format;

// class JsonPathFormatter // implements IPathFormatter {
//
// public JsonPathFormatter() {
// }
//
// @NonNull
// protected String getEffectiveName(IDefinitionPathSegment segment) {
// return segment.getInstance().getJsonName();
// }
//
// @SuppressWarnings("null")
// @Override
// public String format(IPathSegment segment) {
// return segment.getPathStream().map(pathSegment -> {
// return pathSegment.format(this);
// }).collect(Collectors.joining("."));
// }
//
// @Override
// public @NonNull String formatPathSegment(@NonNull IDocumentPathSegment segment) {
// return "$";
// }
//
// @Override
// public String formatPathSegment(IFlagPathSegment segment) {
// return getEffectiveName(segment);
// }
//
// @Override
// public String formatPathSegment(IFieldPathSegment segment) {
// IFieldInstance fieldInstance = segment.getInstance();
//
// String retval;
// switch (fieldInstance.getJsonGroupAsBehavior()) {
// case KEYED:
// // use the identifier to index the map
// INodeItem node = segment.getNodeItem();
// retval = "*";
// if (node != null) {
// IFieldDefinition fieldDefinition = fieldInstance.getDefinition();
// IFlagInstance jsonKeyFlagInstance = fieldDefinition.getJsonKeyFlagInstance();
// if (jsonKeyFlagInstance != null) {
// String keyFlagName = jsonKeyFlagInstance.getEffectiveName();
// IRequiredValueFlagNodeItem flagNode = node.getFlagByName(keyFlagName);
// if (flagNode != null) {
// retval = flagNode.toAtomicItem().asString();
// }
// }
// }
// break;
// case LIST:
// break;
// case NONE:
// break;
// case SINGLETON_OR_LIST:
// break;
// default:
// break;
// }
// return formatModelPathSegment(segment);
// }
//
// @SuppressWarnings("null")
// @Override
// public String formatPathSegment(IAssemblyPathSegment segment) {
// String retval;
// if (segment instanceof IRootAssemblyPathSegment) {
// StringBuilder builder = new StringBuilder();
// builder.append(getEffectiveName(segment));
// retval = builder.toString();
// } else {
// // TODO: does it make sense to use this for an intermediate that has no parent?
// retval = formatModelPathSegment(segment);
// }
// return retval;
// }
//
// @SuppressWarnings("null")
// @NonNull
// protected String formatModelPathSegment(IModelPositionalPathSegment segment) {
// StringBuilder builder = new StringBuilder(getEffectiveName(segment));
// builder.append('[');
// builder.append(segment.getPosition());
// builder.append(']');
// return builder.toString();
// }
// }
