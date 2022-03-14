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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO: rethink the inner anonymous classes
public interface IInstanceSet {
  public static final IInstanceSet EMPTY_INSTANCE_SET = new DefaultInstanceSet(Collections.emptyList());

  Collection<? extends IInstance> getInstances();

  public static IInstanceSet newInstanceSet(@NotNull IAssemblyDefinition definition) {
    return new IInstanceSet() {

      @Override
      public List<? extends IInstance> getInstances() {
        return List.of(new IAssemblyInstance() {

          @Override
          public String getName() {
            return getDefinition().getName();
          }

          @Override
          public String getUseName() {
            return getDefinition().getUseName();
          }

          @SuppressWarnings("null")
          @Override
          public String getXmlNamespace() {
            return getDefinition().getContainingMetaschema().getXmlNamespace().toASCIIString();
          }

          @Override
          public String toCoordinates() {
            return getDefinition().toCoordinates();
          }

          @Override
          public MarkupMultiline getRemarks() {
            return getDefinition().getRemarks();
          }

          @Override
          public IAssemblyDefinition getContainingDefinition() {
            return null;
          }

          @Override
          public int getMinOccurs() {
            return 1;
          }

          @Override
          public int getMaxOccurs() {
            return 1;
          }

          @Override
          public String getGroupAsName() {
            return null;
          }

          @Override
          public String getGroupAsXmlNamespace() {
            return null;
          }

          @Override
          public JsonGroupAsBehavior getJsonGroupAsBehavior() {
            return JsonGroupAsBehavior.NONE;
          }

          @Override
          public XmlGroupAsBehavior getXmlGroupAsBehavior() {
            return XmlGroupAsBehavior.UNGROUPED;
          }

          @Override
          public IAssemblyDefinition getDefinition() {
            return definition;
          }

          @Override
          public IMetaschema getContainingMetaschema() {
            return getDefinition().getContainingMetaschema();
          }
        });
      }

    };
  }

  public static IInstanceSet newInstanceSet(@NotNull IFieldDefinition definition) {
    return new IInstanceSet() {

      @Override
      public List<? extends IInstance> getInstances() {
        return List.of(new IFieldInstance() {

          @Override
          public String getName() {
            return getDefinition().getName();
          }

          @Override
          public String getUseName() {
            return getDefinition().getUseName();
          }

          @Override
          public String toCoordinates() {
            return getDefinition().toCoordinates();
          }

          @Override
          public MarkupMultiline getRemarks() {
            return getDefinition().getRemarks();
          }

          @Override
          public IAssemblyDefinition getContainingDefinition() {
            return null;
          }

          @Override
          public int getMinOccurs() {
            return 1;
          }

          @Override
          public int getMaxOccurs() {
            return 1;
          }

          @Override
          public String getGroupAsName() {
            return null;
          }

          @Override
          public String getGroupAsXmlNamespace() {
            return null;
          }

          @Override
          public JsonGroupAsBehavior getJsonGroupAsBehavior() {
            return JsonGroupAsBehavior.NONE;
          }

          @Override
          public XmlGroupAsBehavior getXmlGroupAsBehavior() {
            return XmlGroupAsBehavior.UNGROUPED;
          }

          @Override
          public IFieldDefinition getDefinition() {
            return definition;
          }

          @Override
          public boolean isInXmlWrapped() {
            return true;
          }

          @Override
          public IMetaschema getContainingMetaschema() {
            return getDefinition().getContainingMetaschema();
          }

        });
      }

    };
  }

  public static IInstanceSet newInstanceSet(@NotNull IFlagDefinition definition) {
    return new IInstanceSet() {

      @Override
      public List<? extends IInstance> getInstances() {
        return List.of(new IFlagInstance() {

          @Override
          public String getName() {
            return getDefinition().getName();
          }

          @Override
          public String getUseName() {
            return getDefinition().getUseName();
          }

          @Override
          public String toCoordinates() {
            return getDefinition().toCoordinates();
          }

          @Override
          public MarkupMultiline getRemarks() {
            return getDefinition().getRemarks();
          }

          @Override
          public IAssemblyDefinition getContainingDefinition() {
            return null;
          }

          @Override
          public IFlagDefinition getDefinition() {
            return definition;
          }

          @Override
          public boolean isRequired() {
            return false;
          }

          @Override
          public IMetaschema getContainingMetaschema() {
            return getDefinition().getContainingMetaschema();
          }
        });
      }

    };
  }

  public static IInstanceSet newInstanceSet(Collection<? extends IInstance> instances) {
    IInstanceSet retval;
    if (instances.isEmpty()) {
      retval = EMPTY_INSTANCE_SET;
    } else {
      retval = new IInstanceSet() {
        @Override
        public Collection<? extends IInstance> getInstances() {
          return instances;
        }
      };
    }
    return retval;
  }
}
