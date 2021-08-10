
package gov.nist.secauto.metaschema.metapath;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import src.main.antlr4.metapath10Parser;

public class ASTPrinter {
  private boolean ignoringWrappers = true;

  public void setIgnoringWrappers(boolean ignoringWrappers) {
      this.ignoringWrappers = ignoringWrappers;
  }

  public void print(RuleContext ctx) {
    explore(ctx, 0);
  }

  private void explore(RuleContext ctx, int indentation) {
    boolean toBeIgnored = ignoringWrappers
            && ctx.getChildCount() == 1
            && ctx.getChild(0) instanceof ParserRuleContext;
    if (!toBeIgnored) {
        String ruleName = metapath10Parser.ruleNames[ctx.getRuleIndex()];
        for (int i = 0; i < indentation; i++) {
            System.out.print("  ");
        }
        System.out.println(ruleName);
    }
    for (int i=0;i<ctx.getChildCount();i++) {
        ParseTree element = ctx.getChild(i);
        if (element instanceof RuleContext) {
            explore((RuleContext)element, indentation + (toBeIgnored ? 0 : 1));
        }
    }
}
}
