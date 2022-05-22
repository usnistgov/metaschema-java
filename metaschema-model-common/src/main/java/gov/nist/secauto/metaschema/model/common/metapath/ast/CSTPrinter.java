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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;

public class CSTPrinter {
  @NotNull
  private final PrintStream outputStream;
  private boolean ignoringWrappers = true;

  /**
   * Construct a new concrete syntax tree (CST) printer.
   * 
   * @param outputStream
   *          the stream to print to
   */
  public CSTPrinter(@NotNull PrintStream outputStream) {
    this.outputStream = ObjectUtils.requireNonNull(outputStream, "outputStream");
  }

  /**
   * Set the behavior for handling wrapper nodes in the CST hierarchy.
   * 
   * @param ignoringWrappers
   *          {@code true} if wrappers should be ignored or {@code false} otherwise
   */
  public void setIgnoringWrappers(boolean ignoringWrappers) {
    this.ignoringWrappers = ignoringWrappers;
  }

  /**
   * Print a given CST {@link RuleContext} node.
   * 
   * @param ctx
   *          the CST node
   */
  public void print(@NotNull RuleContext ctx) {
    explore(ctx, 0);
  }

  /**
   * Print a given CST {@link ParseTree} using the provided {@code ruleNames}.
   * 
   * @param tree
   *          the CST parse tree
   * @param ruleNames
   *          the list of rule names to use for human readability
   */
  public void print(ParseTree tree, List<String> ruleNames) {
    explore((RuleContext) tree.getPayload(), 0);
  }

  private void explore(RuleContext ctx, int indentation) {
    boolean toBeIgnored = ignoringWrappers && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
    String ruleName = metapath10Parser.ruleNames[ctx.getRuleIndex()];
    for (int i = 0; i < indentation; i++) {
      outputStream.print("  ");
    }
    outputStream.print(ruleName);
    if (toBeIgnored) {
      outputStream.print("(ignored)");
    }
    outputStream.print(": ");
    outputStream.print(ctx.getText());
    outputStream.println();

    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree element = ctx.getChild(i);
      if (element instanceof RuleContext) {
        explore((RuleContext) element, indentation + 1);
      }
    }
  }
}
