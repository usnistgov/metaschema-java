/**
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

package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.Format;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

class XmlParsePlanTest {

  @Test
  void testBasicRootElement() throws BindingException {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<top-level\n"
        + "	xmlns=\"http://csrc.nist.gov/ns/metaschema/testing\" id=\"top-level-id\"/>\n";

    BindingContext context = BindingContext.newInstance();
    TopLevel topLevel = context.newDeserializer(Format.XML, TopLevel.class, null).deserialize(new StringReader(xml));
    Assertions.assertEquals("top-level-id", topLevel.id);
  }

  @Test
  void testWithStringChild() throws BindingException {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<top-level\n"
        + "	xmlns=\"http://csrc.nist.gov/ns/metaschema/testing\" id=\"top-level-id\">\n" + "  <child>value1</child>\n"
        + "  <child>value2</child>\n" + "  <children2>\n" + "    <child2>value3</child2>\n"
        + "    <child2>value4</child2>\n" + "  </children2>\n" + "  <child3 id=\"child3\">value5</child3>\n"
        + "</top-level>\n";

    BindingContext context = BindingContext.newInstance();
    TopLevel topLevel = context.newDeserializer(Format.XML, TopLevel.class, null).deserialize(new StringReader(xml));
    Assertions.assertEquals("top-level-id", topLevel.id);
    List<String> children = new ArrayList<>(2);
    children.add("value1");
    children.add("value2");
    Assertions.assertEquals(children, topLevel.child);
    children = new ArrayList<>(2);
    children.add("value3");
    children.add("value4");
    Assertions.assertEquals(children, topLevel.child2);
    Assertions.assertEquals("child3", topLevel.field3.id);
    Assertions.assertEquals("value5", topLevel.field3.value);
  }

  @gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper(name = "top-level",
      namespace = "http://csrc.nist.gov/ns/metaschema/testing")
  public static class TopLevel {
    @Flag(name = "id", required = false)
    private String id;

    @Field(name = "child", namespace = "http://csrc.nist.gov/ns/metaschema/testing", required = false)
    @GroupAs(name = "children", namespace = "http://csrc.nist.gov/ns/metaschema/testing", maxOccurs = -1,
        inXml = XmlGroupAsBehavior.UNGROUPED)
    private List<String> child;

    @Field(name = "child2", namespace = "http://csrc.nist.gov/ns/metaschema/testing", required = false)
    @GroupAs(name = "children2", namespace = "http://csrc.nist.gov/ns/metaschema/testing", maxOccurs = -1,
        inXml = XmlGroupAsBehavior.GROUPED)
    private List<String> child2;

    @Field(name = "child3", namespace = "http://csrc.nist.gov/ns/metaschema/testing", required = false)
    private Field3 field3;

    public TopLevel() {
    }

  }

  public static class Field3 {
    @Flag(name = "id", required = false)
    private String id;

    @FieldValue
    private String value;
  }
}
