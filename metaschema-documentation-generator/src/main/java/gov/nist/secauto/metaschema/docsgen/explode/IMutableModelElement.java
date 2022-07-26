package gov.nist.secauto.metaschema.docsgen.explode;

import edu.umd.cs.findbugs.annotations.NonNull;

interface IMutableModelElement extends IModelElement {

  void addFlag(@NonNull IModelElement flag);

  void addModelItem(@NonNull IModelElement modelItem);

}
