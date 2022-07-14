
package gov.nist.secauto.metaschema.docsgen.explode;

import org.jetbrains.annotations.NotNull;

public interface IModelElementVisitor<RESULT, CONTEXT> {
  RESULT visitAssembly(@NotNull IAssemblyModelElement element, CONTEXT context);

  RESULT visitField(@NotNull IFieldModelElement element, CONTEXT context);

  RESULT visitFlag(@NotNull IFlagModelElement element, CONTEXT context);

  interface IModelElementVisitable {
    <RESULT, CONTEXT> RESULT accept(@NotNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context);
  }
}
