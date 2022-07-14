package gov.nist.secauto.metaschema.docsgen.explode;

import org.jetbrains.annotations.NotNull;

interface IMutableModelElement extends IModelElement {

  void addFlag(@NotNull IModelElement flag);

  void addModelItem(@NotNull IModelElement modelItem);

}
