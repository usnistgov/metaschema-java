
package gov.nist.secauto.metaschema.metapath;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import src.main.antlr4.metapath10Lexer;
import src.main.antlr4.metapath10Parser;
import src.main.antlr4.metapath10Visitor;

public class Test {
  public static void main(String[] args) {
    metapath10Lexer lexer = new metapath10Lexer(CharStreams.fromString("@flag"));
    
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    metapath10Parser parser = new metapath10Parser(tokens);
//    ParseTree tree = parser.compilationUnit();
    ParseTree tree = parser.expr();
    System.out.println(Trees.toStringTree(tree));

    metapath10Visitor<Node> visitor = new BuildAstVisitor();
    visitor.visit(tree);
  }
}
