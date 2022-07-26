
package gov.nist.secauto.metaschema.docsgen.explode;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IModelElementVisitor<RESULT, CONTEXT> {
  RESULT visitAssembly(@NonNull IAssemblyModelElement element, CONTEXT context);

  RESULT visitField(@NonNull IFieldModelElement element, CONTEXT context);

  RESULT visitFlag(@NonNull IFlagModelElement element, CONTEXT context);

  interface IModelElementVisitable {
    <RESULT, CONTEXT> RESULT accept(@NonNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context);
  }
}
