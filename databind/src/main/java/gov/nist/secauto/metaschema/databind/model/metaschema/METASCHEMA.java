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
package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.datatype.adapter.TokenAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.UriAdapter;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLineAdapter;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValue;
import gov.nist.secauto.metaschema.databind.model.annotations.AllowedValues;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A declaration of the Metaschema module.
 */
@MetaschemaAssembly(
    formalName = "Metaschema Module",
    description = "A declaration of the Metaschema module.",
    name = "METASCHEMA",
    moduleClass = MetaschemaModule.class,
    rootName = "METASCHEMA"
)
public class METASCHEMA {
  /**
   * "Determines if the Metaschema module is abstract (&lsquo;yes&rsquo;) or not (&lsquo;no&rsquo;)."
   */
  @BoundFlag(
      formalName = "Is Abstract?",
      description = "Determines if the Metaschema module is abstract ('yes') or not ('no').",
      useName = "abstract",
      defaultValue = "no",
      typeAdapter = TokenAdapter.class,
      valueConstraints = @ValueConstraints(allowedValues = @AllowedValues(level = IConstraint.Level.ERROR, values = {@AllowedValue(value = "yes", description = ""), @AllowedValue(value = "no", description = "")}))
  )
  private String _abstract;

  @BoundField(
      formalName = "Module Name",
      description = "The name of the information model represented by this Metaschema definition.",
      useName = "schema-name",
      minOccurs = 1,
      typeAdapter = MarkupLineAdapter.class
  )
  private MarkupLine _schemaName;

  @BoundField(
      description = "A version string used to distinguish between multiple revisions of the same Metaschema module.",
      useName = "schema-version",
      minOccurs = 1
  )
  private String _schemaVersion;

  @BoundField(
      formalName = "Module Short Name",
      description = "A short (code) name to be used for the Metaschema module. This name may be used as a constituent of names assigned to derived artifacts, such as schemas and conversion utilities.",
      useName = "short-name",
      minOccurs = 1,
      typeAdapter = MarkupLineAdapter.class
  )
  private MarkupLine _shortName;

  @BoundField(
      formalName = "Module Collection Namespace",
      description = "The namespace for the collection of Metaschema module this Metaschema module belongs to. This value is also used as the XML namespace governing the names of elements in XML documents. By using this namespace, documents and document fragments used in mixed-format environments may be distinguished from neighbor XML formats using another namespaces. This value is not reflected in Metaschema JSON.",
      useName = "namespace",
      minOccurs = 1,
      typeAdapter = UriAdapter.class
  )
  private URI _namespace;

  @BoundField(
      formalName = "JSON Base URI",
      description = "The JSON Base URI is the nominal base URI assigned to a JSON Schema instance expressing the model defined by this Metaschema module.",
      useName = "json-base-uri",
      minOccurs = 1,
      typeAdapter = UriAdapter.class
  )
  private URI _jsonBaseUri;

  @BoundField(
      formalName = "Remarks",
      description = "Any explanatory or helpful information to be provided about the remarks parent.",
      useName = "remarks"
  )
  private Remarks _remarks;

  @BoundAssembly(
      formalName = "Module Import",
      description = "Imports a set of Metaschema modules contained in another resource. Imports support the reuse of common information structures.",
      useName = "import",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "imports", inJson = JsonGroupAsBehavior.LIST)
  )
  private List<Import> _imports;

  @BoundChoiceGroup(
      maxOccurs = -1,
      assemblies = {
          @BoundGroupedAssembly(formalName = "Global Assembly Definition", description = "In XML, an element with structured element content. In JSON, an object with properties. Defined globally, an assembly can be assigned to appear in the `model` of any assembly (another assembly type, or itself), by `assembly` reference.", useName = "define-assembly", binding = GlobalDefineAssembly.class),
          @BoundGroupedAssembly(formalName = "Global Field Definition", useName = "define-field", binding = GlobalDefineField.class),
          @BoundGroupedAssembly(formalName = "Global Flag Definition", useName = "define-flag", binding = GlobalDefineFlag.class)
      },
      groupAs = @GroupAs(name = "definitions", inJson = JsonGroupAsBehavior.LIST)
  )
  private List<Object> _definitions;

  public METASCHEMA() {
  }

  public String getAbstract() {
    return _abstract;
  }

  public void setAbstract(String value) {
    _abstract = value;
  }

  public MarkupLine getSchemaName() {
    return _schemaName;
  }

  public void setSchemaName(MarkupLine value) {
    _schemaName = value;
  }

  public String getSchemaVersion() {
    return _schemaVersion;
  }

  public void setSchemaVersion(String value) {
    _schemaVersion = value;
  }

  public MarkupLine getShortName() {
    return _shortName;
  }

  public void setShortName(MarkupLine value) {
    _shortName = value;
  }

  public URI getNamespace() {
    return _namespace;
  }

  public void setNamespace(URI value) {
    _namespace = value;
  }

  public URI getJsonBaseUri() {
    return _jsonBaseUri;
  }

  public void setJsonBaseUri(URI value) {
    _jsonBaseUri = value;
  }

  public Remarks getRemarks() {
    return _remarks;
  }

  public void setRemarks(Remarks value) {
    _remarks = value;
  }

  public List<Import> getImports() {
    return _imports;
  }

  public void setImports(List<Import> value) {
    _imports = value;
  }

  /**
   * Add a new {@link Import} item to the underlying collection.
   * @param item the item to add
   * @return {@code true}
   */
  public boolean addImport(Import item) {
    Import value = ObjectUtils.requireNonNull(item,"item cannot be null");
    if (_imports == null) {
      _imports = new LinkedList<>();
    }
    return _imports.add(value);
  }

  /**
   * Remove the first matching {@link Import} item from the underlying collection.
   * @param item the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeImport(Import item) {
    Import value = ObjectUtils.requireNonNull(item,"item cannot be null");
    return _imports == null ? false : _imports.remove(value);
  }

  public List<Object> getDefinitions() {
    return _definitions;
  }

  public void setDefinitions(List<Object> value) {
    _definitions = value;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }

  /**
   * Imports a set of Metaschema modules contained in another resource. Imports support the reuse of common information structures.
   */
  @MetaschemaAssembly(
      formalName = "Module Import",
      description = "Imports a set of Metaschema modules contained in another resource. Imports support the reuse of common information structures.",
      name = "import",
      moduleClass = MetaschemaModule.class
  )
  public static class Import {
    public Import() {
    }

    @Override
    public String toString() {
      return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
    }
  }
}
