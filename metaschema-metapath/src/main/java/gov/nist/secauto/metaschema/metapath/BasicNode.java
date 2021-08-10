package gov.nist.secauto.metaschema.metapath;

import java.util.LinkedList;
import java.util.List;

public class BasicNode implements Node {
  private final String type;
  private List<Node> children;

  public BasicNode(String type) {
    this.type = type;
    this.children = new LinkedList<>();
  }

  public void addChild(Node node) {
    this.children.add(node);
  }
}
