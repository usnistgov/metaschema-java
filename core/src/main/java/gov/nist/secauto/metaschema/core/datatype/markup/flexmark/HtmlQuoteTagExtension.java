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

package gov.nist.secauto.metaschema.core.datatype.markup.flexmark;

import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.html2md.converter.HtmlMarkdownWriter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeConverterContext;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererFactory;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class HtmlQuoteTagExtension
    implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
    FlexmarkHtmlConverter.HtmlConverterExtension {

  /**
   * Construct a new extension instance.
   *
   * @return the instance
   */
  public static HtmlQuoteTagExtension newInstance() {
    return new HtmlQuoteTagExtension();
  }

  @Override
  public void rendererOptions(MutableDataHolder options) {
    // do nothing
  }

  @Override
  public void parserOptions(MutableDataHolder options) {
    // do nothing
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
    rendererBuilder.nodeRendererFactory(new QTagNodeRenderer.Factory());
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.postProcessorFactory(new QuoteReplacingPostProcessor.Factory());
  }

  @Override
  public void extend(FlexmarkHtmlConverter.Builder builder) {
    builder.htmlNodeRendererFactory(new QTagHtmlNodeRenderer.Factory());
  }

  static class QTagNodeRenderer implements NodeRenderer {

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      return Collections.singleton(
          new NodeRenderingHandler<>(DoubleQuoteNode.class, this::render));
    }

    protected void render(@NonNull DoubleQuoteNode node, @NonNull NodeRendererContext context,
        @NonNull HtmlWriter html) {
      html.withAttr().tag("q");
      context.renderChildren(node);
      html.tag("/q");
    }

    public static class Factory implements NodeRendererFactory {

      @Override
      public NodeRenderer apply(DataHolder options) {
        return new QTagNodeRenderer();
      }

    }
  }

  static class QuoteReplacingPostProcessor
      extends NodePostProcessor {

    @Override
    public void process(NodeTracker state, Node node) {
      if (node instanceof TypographicQuotes) {
        TypographicQuotes typographicQuotes = (TypographicQuotes) node;
        if (typographicQuotes.getOpeningMarker().matchChars("\"")) {
          DoubleQuoteNode quoteNode = new DoubleQuoteNode(typographicQuotes);
          node.insertAfter(quoteNode);
          state.nodeAdded(quoteNode);
          node.unlink();
          state.nodeRemoved(node);
        }
      }
    }

    public static class Factory
        extends NodePostProcessorFactory {
      public Factory() {
        super(false);
        addNodeWithExclusions(TypographicQuotes.class, DoNotDecorate.class);
      }

      @NonNull
      @Override
      public NodePostProcessor apply(Document document) {
        return new QuoteReplacingPostProcessor();
      }
    }
  }

  static class QTagHtmlNodeRenderer implements HtmlNodeRenderer {

    @Override
    public Set<HtmlNodeRendererHandler<?>> getHtmlNodeRendererHandlers() {
      return Collections.singleton(new HtmlNodeRendererHandler<>("q", Element.class, this::renderMarkdown));
    }

    protected void renderMarkdown(Element element, HtmlNodeConverterContext context,
        @SuppressWarnings("unused") HtmlMarkdownWriter out) {
      context.wrapTextNodes(element, "\"", element.nextElementSibling() != null);
    }

    public static class Factory implements HtmlNodeRendererFactory {

      @Override
      public HtmlNodeRenderer apply(DataHolder options) {
        return new QTagHtmlNodeRenderer();
      }
    }

  }

  public static class DoubleQuoteNode
      extends TypographicQuotes {

    /**
     * Construct a new double quote node.
     *
     * @param node
     *          the typographic information pertaining to a double quote
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DoubleQuoteNode(TypographicQuotes node) {
      super(node.getOpeningMarker(), node.getText(), node.getClosingMarker());
      setTypographicOpening(node.getTypographicOpening());
      setTypographicClosing(node.getTypographicClosing());
      for (Node child : node.getChildren()) {
        appendChild(child);
      }
    }
  }
}
