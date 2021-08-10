package gov.nist.secauto.metaschema.metapath;

import src.main.antlr4.metapath10BaseVisitor;
import src.main.antlr4.metapath10Parser.*;

public class BuildAstVisitor extends metapath10BaseVisitor<Node> {

  @Override
  public Node visitAttributename(AttributenameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAttributename(ctx);
  }

  @Override
  public Node visitMetapath(MetapathContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMetapath(ctx);
  }

  @Override
  public Node visitParamlist(ParamlistContext ctx) {
    // TODO Auto-generated method stub
    return super.visitParamlist(ctx);
  }

  @Override
  public Node visitParam(ParamContext ctx) {
    // TODO Auto-generated method stub
    return super.visitParam(ctx);
  }

  @Override
  public Node visitFunctionbody(FunctionbodyContext ctx) {
    // TODO Auto-generated method stub
    return super.visitFunctionbody(ctx);
  }

  @Override
  public Node visitEnclosedexpr(EnclosedexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitEnclosedexpr(ctx);
  }

  @Override
  public Node visitExpr(ExprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitExpr(ctx);
  }

  @Override
  public Node visitExprsingle(ExprsingleContext ctx) {
    // TODO Auto-generated method stub
    return super.visitExprsingle(ctx);
  }

  @Override
  public Node visitForexpr(ForexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitForexpr(ctx);
  }

  @Override
  public Node visitSimpleforclause(SimpleforclauseContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimpleforclause(ctx);
  }

  @Override
  public Node visitSimpleforbinding(SimpleforbindingContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimpleforbinding(ctx);
  }

  @Override
  public Node visitLetexpr(LetexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLetexpr(ctx);
  }

  @Override
  public Node visitSimpleletclause(SimpleletclauseContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimpleletclause(ctx);
  }

  @Override
  public Node visitSimpleletbinding(SimpleletbindingContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimpleletbinding(ctx);
  }

  @Override
  public Node visitQuantifiedexpr(QuantifiedexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitQuantifiedexpr(ctx);
  }

  @Override
  public Node visitIfexpr(IfexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitIfexpr(ctx);
  }

  @Override
  public Node visitOrexpr(OrexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitOrexpr(ctx);
  }

  @Override
  public Node visitAndexpr(AndexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAndexpr(ctx);
  }

  @Override
  public Node visitComparisonexpr(ComparisonexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitComparisonexpr(ctx);
  }

  @Override
  public Node visitStringconcatexpr(StringconcatexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitStringconcatexpr(ctx);
  }

  @Override
  public Node visitRangeexpr(RangeexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitRangeexpr(ctx);
  }

  @Override
  public Node visitAdditiveexpr(AdditiveexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAdditiveexpr(ctx);
  }

  @Override
  public Node visitMultiplicativeexpr(MultiplicativeexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMultiplicativeexpr(ctx);
  }

  @Override
  public Node visitUnionexpr(UnionexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitUnionexpr(ctx);
  }

  @Override
  public Node visitIntersectexceptexpr(IntersectexceptexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitIntersectexceptexpr(ctx);
  }

  @Override
  public Node visitInstanceofexpr(InstanceofexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitInstanceofexpr(ctx);
  }

  @Override
  public Node visitTreatexpr(TreatexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTreatexpr(ctx);
  }

  @Override
  public Node visitCastableexpr(CastableexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitCastableexpr(ctx);
  }

  @Override
  public Node visitCastexpr(CastexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitCastexpr(ctx);
  }

  @Override
  public Node visitArrowexpr(ArrowexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArrowexpr(ctx);
  }

  @Override
  public Node visitUnaryexpr(UnaryexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitUnaryexpr(ctx);
  }

  @Override
  public Node visitValueexpr(ValueexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitValueexpr(ctx);
  }

  @Override
  public Node visitGeneralcomp(GeneralcompContext ctx) {
    // TODO Auto-generated method stub
    return super.visitGeneralcomp(ctx);
  }

  @Override
  public Node visitValuecomp(ValuecompContext ctx) {
    // TODO Auto-generated method stub
    return super.visitValuecomp(ctx);
  }

  @Override
  public Node visitNodecomp(NodecompContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNodecomp(ctx);
  }

  @Override
  public Node visitSimplemapexpr(SimplemapexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimplemapexpr(ctx);
  }

  @Override
  public Node visitPathexpr(PathexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPathexpr(ctx);
  }

  @Override
  public Node visitRelativepathexpr(RelativepathexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitRelativepathexpr(ctx);
  }

  @Override
  public Node visitStepexpr(StepexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitStepexpr(ctx);
  }

  @Override
  public Node visitAxisstep(AxisstepContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAxisstep(ctx);
  }

  @Override
  public Node visitForwardstep(ForwardstepContext ctx) {
    // TODO Auto-generated method stub
    return super.visitForwardstep(ctx);
  }

  @Override
  public Node visitForwardaxis(ForwardaxisContext ctx) {
    // TODO Auto-generated method stub
    return super.visitForwardaxis(ctx);
  }

  @Override
  public Node visitAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAbbrevforwardstep(ctx);
  }

  @Override
  public Node visitReversestep(ReversestepContext ctx) {
    // TODO Auto-generated method stub
    return super.visitReversestep(ctx);
  }

  @Override
  public Node visitReverseaxis(ReverseaxisContext ctx) {
    // TODO Auto-generated method stub
    return super.visitReverseaxis(ctx);
  }

  @Override
  public Node visitAbbrevreversestep(AbbrevreversestepContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAbbrevreversestep(ctx);
  }

  @Override
  public Node visitNodetest(NodetestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNodetest(ctx);
  }

  @Override
  public Node visitNametest(NametestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNametest(ctx);
  }

  @Override
  public Node visitWildcard(WildcardContext ctx) {
    // TODO Auto-generated method stub
    return super.visitWildcard(ctx);
  }

  @Override
  public Node visitPostfixexpr(PostfixexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPostfixexpr(ctx);
  }

  @Override
  public Node visitArgumentlist(ArgumentlistContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArgumentlist(ctx);
  }

  @Override
  public Node visitPredicatelist(PredicatelistContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPredicatelist(ctx);
  }

  @Override
  public Node visitPredicate(PredicateContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPredicate(ctx);
  }

  @Override
  public Node visitLookup(LookupContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLookup(ctx);
  }

  @Override
  public Node visitKeyspecifier(KeyspecifierContext ctx) {
    // TODO Auto-generated method stub
    return super.visitKeyspecifier(ctx);
  }

  @Override
  public Node visitArrowfunctionspecifier(ArrowfunctionspecifierContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArrowfunctionspecifier(ctx);
  }

  @Override
  public Node visitPrimaryexpr(PrimaryexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPrimaryexpr(ctx);
  }

  @Override
  public Node visitLiteral(LiteralContext ctx) {
    // TODO Auto-generated method stub
    return super.visitLiteral(ctx);
  }

  @Override
  public Node visitNumericliteral(NumericliteralContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNumericliteral(ctx);
  }

  @Override
  public Node visitVarref(VarrefContext ctx) {
    // TODO Auto-generated method stub
    return super.visitVarref(ctx);
  }

  @Override
  public Node visitVarname(VarnameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitVarname(ctx);
  }

  @Override
  public Node visitParenthesizedexpr(ParenthesizedexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitParenthesizedexpr(ctx);
  }

  @Override
  public Node visitContextitemexpr(ContextitemexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitContextitemexpr(ctx);
  }

  @Override
  public Node visitFunctioncall(FunctioncallContext ctx) {
    // TODO Auto-generated method stub
    return super.visitFunctioncall(ctx);
  }

  @Override
  public Node visitArgument(ArgumentContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArgument(ctx);
  }

  @Override
  public Node visitArgumentplaceholder(ArgumentplaceholderContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArgumentplaceholder(ctx);
  }

  @Override
  public Node visitFunctionitemexpr(FunctionitemexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitFunctionitemexpr(ctx);
  }

  @Override
  public Node visitNamedfunctionref(NamedfunctionrefContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNamedfunctionref(ctx);
  }

  @Override
  public Node visitInlinefunctionexpr(InlinefunctionexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitInlinefunctionexpr(ctx);
  }

  @Override
  public Node visitMapconstructor(MapconstructorContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMapconstructor(ctx);
  }

  @Override
  public Node visitMapconstructorentry(MapconstructorentryContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMapconstructorentry(ctx);
  }

  @Override
  public Node visitMapkeyexpr(MapkeyexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMapkeyexpr(ctx);
  }

  @Override
  public Node visitMapvalueexpr(MapvalueexprContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMapvalueexpr(ctx);
  }

  @Override
  public Node visitArrayconstructor(ArrayconstructorContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArrayconstructor(ctx);
  }

  @Override
  public Node visitSquarearrayconstructor(SquarearrayconstructorContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSquarearrayconstructor(ctx);
  }

  @Override
  public Node visitCurlyarrayconstructor(CurlyarrayconstructorContext ctx) {
    // TODO Auto-generated method stub
    return super.visitCurlyarrayconstructor(ctx);
  }

  @Override
  public Node visitUnarylookup(UnarylookupContext ctx) {
    // TODO Auto-generated method stub
    return super.visitUnarylookup(ctx);
  }

  @Override
  public Node visitSingletype(SingletypeContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSingletype(ctx);
  }

  @Override
  public Node visitTypedeclaration(TypedeclarationContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTypedeclaration(ctx);
  }

  @Override
  public Node visitSequencetype(SequencetypeContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSequencetype(ctx);
  }

  @Override
  public Node visitOccurrenceindicator(OccurrenceindicatorContext ctx) {
    // TODO Auto-generated method stub
    return super.visitOccurrenceindicator(ctx);
  }

  @Override
  public Node visitItemtype(ItemtypeContext ctx) {
    // TODO Auto-generated method stub
    return super.visitItemtype(ctx);
  }

  @Override
  public Node visitAtomicoruniontype(AtomicoruniontypeContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAtomicoruniontype(ctx);
  }

  @Override
  public Node visitKindtest(KindtestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitKindtest(ctx);
  }

  @Override
  public Node visitAnykindtest(AnykindtestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAnykindtest(ctx);
  }

  @Override
  public Node visitDocumenttest(DocumenttestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitDocumenttest(ctx);
  }

  @Override
  public Node visitTexttest(TexttestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTexttest(ctx);
  }

  @Override
  public Node visitCommenttest(CommenttestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitCommenttest(ctx);
  }

  @Override
  public Node visitNamespacenodetest(NamespacenodetestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitNamespacenodetest(ctx);
  }

  @Override
  public Node visitPitest(PitestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitPitest(ctx);
  }

  @Override
  public Node visitAttributetest(AttributetestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAttributetest(ctx);
  }

  @Override
  public Node visitAttribnameorwildcard(AttribnameorwildcardContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAttribnameorwildcard(ctx);
  }

  @Override
  public Node visitSchemaattributetest(SchemaattributetestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSchemaattributetest(ctx);
  }

  @Override
  public Node visitAttributedeclaration(AttributedeclarationContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAttributedeclaration(ctx);
  }

  @Override
  public Node visitElementtest(ElementtestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitElementtest(ctx);
  }

  @Override
  public Node visitElementnameorwildcard(ElementnameorwildcardContext ctx) {
    // TODO Auto-generated method stub
    return super.visitElementnameorwildcard(ctx);
  }

  @Override
  public Node visitSchemaelementtest(SchemaelementtestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSchemaelementtest(ctx);
  }

  @Override
  public Node visitElementdeclaration(ElementdeclarationContext ctx) {
    // TODO Auto-generated method stub
    return super.visitElementdeclaration(ctx);
  }

  @Override
  public Node visitElementname(ElementnameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitElementname(ctx);
  }

  @Override
  public Node visitSimpletypename(SimpletypenameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitSimpletypename(ctx);
  }

  @Override
  public Node visitTypename(TypenameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTypename(ctx);
  }

  @Override
  public Node visitFunctiontest(FunctiontestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitFunctiontest(ctx);
  }

  @Override
  public Node visitAnyfunctiontest(AnyfunctiontestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAnyfunctiontest(ctx);
  }

  @Override
  public Node visitTypedfunctiontest(TypedfunctiontestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTypedfunctiontest(ctx);
  }

  @Override
  public Node visitMaptest(MaptestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMaptest(ctx);
  }

  @Override
  public Node visitAnymaptest(AnymaptestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAnymaptest(ctx);
  }

  @Override
  public Node visitTypedmaptest(TypedmaptestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTypedmaptest(ctx);
  }

  @Override
  public Node visitArraytest(ArraytestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitArraytest(ctx);
  }

  @Override
  public Node visitAnyarraytest(AnyarraytestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAnyarraytest(ctx);
  }

  @Override
  public Node visitTypedarraytest(TypedarraytestContext ctx) {
    // TODO Auto-generated method stub
    return super.visitTypedarraytest(ctx);
  }

  @Override
  public Node visitParenthesizeditemtype(ParenthesizeditemtypeContext ctx) {
    // TODO Auto-generated method stub
    return super.visitParenthesizeditemtype(ctx);
  }

  @Override
  public Node visitEqname(EqnameContext ctx) {
    // TODO Auto-generated method stub
    return super.visitEqname(ctx);
  }

  @Override
  public Node visitAuxilary(AuxilaryContext ctx) {
    // TODO Auto-generated method stub
    return super.visitAuxilary(ctx);
  }

}
