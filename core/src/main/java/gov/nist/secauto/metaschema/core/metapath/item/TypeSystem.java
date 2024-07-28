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

package gov.nist.secauto.metaschema.core.metapath.item;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyUriItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBase64BinaryItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IEmailAddressItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IHostnameItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIPv4AddressItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIPv6AddressItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INcNameItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INonNegativeIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IPositiveIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.ITokenItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUriReferenceItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUuidItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("removal")
public final class TypeSystem {
  private static final Map<Class<? extends IItem>, QName> ITEM_CLASS_TO_QNAME_MAP;

  static {
    ITEM_CLASS_TO_QNAME_MAP = Collections.unmodifiableMap(Map.ofEntries(
        register(IItem.class, "item"),
        register(INodeItem.class, "node"),
        register(IDocumentNodeItem.class, "document-node"),
        register(IAssemblyNodeItem.class, "assembly-node"),
        register(IFieldNodeItem.class, "field-node"),
        register(IFlagNodeItem.class, "flag-node"),
        register(IArrayItem.class, "array"),
        register(IMapItem.class, "map"),
        register(IAnyAtomicItem.class, "any-atomic-type"),
        register(INumericItem.class, "numeric"),
        register(IDurationItem.class, "duration"),
        register(IBase64BinaryItem.class, MetaschemaDataTypeProvider.BASE64),
        register(IBooleanItem.class, MetaschemaDataTypeProvider.BOOLEAN),
        register(IDateItem.class, MetaschemaDataTypeProvider.DATE),
        // register(IDate.class, MetaschemaDataTypeProvider.DATE_WITH_TZ),
        register(IDateTimeItem.class, MetaschemaDataTypeProvider.DATE_TIME),
        // register(IBooleanItem.class, MetaschemaDataTypeProvider.DATE_TIME_WITH_TZ),
        register(IIPv4AddressItem.class, MetaschemaDataTypeProvider.IP_V4_ADDRESS),
        register(IIPv6AddressItem.class, MetaschemaDataTypeProvider.IP_V6_ADDRESS),
        register(IAnyUriItem.class, MetaschemaDataTypeProvider.URI),
        register(IUriReferenceItem.class, MetaschemaDataTypeProvider.URI_REFERENCE),
        register(IUuidItem.class, MetaschemaDataTypeProvider.UUID),
        register(IDayTimeDurationItem.class, MetaschemaDataTypeProvider.DAY_TIME_DURATION),
        register(IYearMonthDurationItem.class, MetaschemaDataTypeProvider.YEAR_MONTH_DURATION),
        register(IDecimalItem.class, MetaschemaDataTypeProvider.DECIMAL),
        register(IIntegerItem.class, MetaschemaDataTypeProvider.INTEGER),
        register(INonNegativeIntegerItem.class, MetaschemaDataTypeProvider.NON_NEGATIVE_INTEGER),
        register(IPositiveIntegerItem.class, MetaschemaDataTypeProvider.POSITIVE_INTEGER),
        register(IEmailAddressItem.class, MetaschemaDataTypeProvider.EMAIL_ADDRESS),
        register(IHostnameItem.class, MetaschemaDataTypeProvider.HOSTNAME),
        register(INcNameItem.class, MetaschemaDataTypeProvider.NCNAME),
        register(IStringItem.class, MetaschemaDataTypeProvider.STRING),
        register(ITokenItem.class, MetaschemaDataTypeProvider.TOKEN)));
  }

  private static Map.Entry<Class<? extends IItem>, QName> register(
      @NonNull Class<? extends IItem> clazz,
      @NonNull IDataTypeAdapter<?> adapter) {
    return Map.entry(clazz, adapter.getPreferredName());
  }

  private static Map.Entry<Class<? extends IItem>, QName> register(
      @NonNull Class<? extends IItem> clazz,
      @NonNull String typeName) {
    return Map.entry(clazz, new QName(MetapathConstants.NS_METAPATH.toASCIIString(), typeName));
  }

  @NonNull
  private static Stream<Class<? extends IItem>> getItemInterfaces(@NonNull Class<?> clazz) {
    @SuppressWarnings("unchecked") Stream<Class<? extends IItem>> retval = IItem.class.isAssignableFrom(clazz)
        ? Stream.of((Class<? extends IItem>) clazz)
        : Stream.empty();

    Class<?>[] interfaces = clazz.getInterfaces();
    if (interfaces.length > 0) {
      retval = Stream.concat(retval, Arrays.stream(interfaces).flatMap(TypeSystem::getItemInterfaces));
    }

    return ObjectUtils.notNull(retval);
  }

  /**
   * Get the human-friendly data type name for the provided Metapath item class.
   *
   * @param clazz
   *          the Metapath item class to get the name for
   * @return the name or {@code null} if no name is registered for the item class
   */
  public static String getName(@NonNull Class<? extends IItem> clazz) {
    Class<? extends IItem> itemClass = getItemInterfaces(clazz).findFirst().orElse(null);

    QName qname = ITEM_CLASS_TO_QNAME_MAP.get(itemClass);
    return qname == null ? clazz.getName() : asPrefixedName(qname);
  }

  private static String asPrefixedName(@NonNull QName qname) {
    String namespace = qname.getNamespaceURI();
    String prefix = namespace.isEmpty() ? null : StaticContext.getWellKnownPrefixForUri(namespace);
    return prefix == null ? qname.toString() : prefix + ":" + qname.getLocalPart();
  }

  private TypeSystem() {
    // disable construction
  }
}
