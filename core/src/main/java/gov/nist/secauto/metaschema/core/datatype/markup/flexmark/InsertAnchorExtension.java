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

package gov.nist.secauto.metaschema.core.datatype.markup.flexmark; // NOPMD AST processor

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
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
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public class InsertAnchorExtension
    implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
    Formatter.FormatterExtension, FlexmarkHtmlConverter.HtmlConverterExtension {
  public static final DataKey<Boolean> ENABLE_INLINE_INSERT_ANCHORS
      = new DataKey<>("ENABLE_INLINE_INSERT_ANCHORS", true);
  public static final DataKey<Boolean> ENABLE_RENDERING = new DataKey<>("ENABLE_RENDERING", true);

  public static Extension create() {
    return new InsertAnchorExtension();
  }

  @Override
  public void parserOptions(MutableDataHolder options) {
    // do nothing
  }

  @Override
  public void rendererOptions(MutableDataHolder options) {
    // do nothing
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
    rendererBuilder.nodeRendererFactory(new InsertAnchorNodeRenderer.Factory());
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    if (ENABLE_INLINE_INSERT_ANCHORS.get(parserBuilder)) {
      parserBuilder.customInlineParserExtensionFactory(new InsertAnchorInlineParser.Factory());
    }
  }

  @Override
  public void extend(Formatter.Builder builder) {
    builder.nodeFormatterFactory(new InsertAnchorFormatter.Factory());
  }

  @Override
  public void extend(FlexmarkHtmlConverter.Builder builder) {
    builder.htmlNodeRendererFactory(new InsertAnchorHtmlNodeRenderer.Factory());
  }

  private static class InsertAnchorOptions {
    public final boolean enableInlineInsertAnchors;
    public final boolean enableRendering;

    public InsertAnchorOptions(DataHolder options) {
      enableInlineInsertAnchors = ENABLE_INLINE_INSERT_ANCHORS.get(options);
      enableRendering = ENABLE_RENDERING.get(options);
    }
  }

  private static class InsertAnchorNodeRenderer implements NodeRenderer {
    private final InsertAnchorOptions options;

    public InsertAnchorNodeRenderer(DataHolder options) {
      this.options = new InsertAnchorOptions(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      return Collections.singleton(new NodeRenderingHandler<>(InsertAnchorNode.class, this::render));
    }

    @SuppressWarnings("unused")
    protected void render(InsertAnchorNode node, NodeRendererContext context, HtmlWriter html) {
      if (options.enableRendering) {
        html.attr("type", node.getType()).attr("id-ref", node.getIdReference()).withAttr().tagVoid("insert");
      }
    }

    // @Override
    // public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    // HashSet<NodeRenderingHandler<?>> set = new
    // HashSet<NodeRenderingHandler<?>>();
    // set.add(new NodeRenderingHandler<Macro>(Macro.class, new
    // CustomNodeRenderer<Macro>() {
    // @Override
    // public void render(Macro node, NodeRendererContext context, HtmlWriter html)
    // {
    // MacroNodeRenderer.this.render(node, context, html); }
    // }));
    public static class Factory implements NodeRendererFactory {

      @Override
      public NodeRenderer apply(DataHolder options) {
        return new InsertAnchorNodeRenderer(options);
      }

    }

  }

  private static class InsertAnchorInlineParser implements InlineParserExtension {
    private static final Pattern PATTERN = Pattern.compile("\\{\\{\\s*insert:\\s*([^\\s]+),\\s*([^\\s]+)\\s*\\}\\}");

    public InsertAnchorInlineParser(@SuppressWarnings("unused") LightInlineParser inlineParser) {
      // do nothing
    }

    @Override
    public void finalizeDocument(InlineParser inlineParser) {
      // do nothing
    }

    @Override
    public void finalizeBlock(InlineParser inlineParser) {
      // do nothing
    }

    @Override
    public boolean parse(LightInlineParser inlineParser) {
      if (inlineParser.peek() == '{') {
        BasedSequence input = inlineParser.getInput();
        Matcher matcher = inlineParser.matcher(PATTERN);
        if (matcher != null) {
          BasedSequence type = input.subSequence(matcher.start(1), matcher.end(1));
          BasedSequence idReference = input.subSequence(matcher.start(2), matcher.end(2));
          assert type != null;
          assert idReference != null;
          inlineParser.appendNode(new InsertAnchorNode(type, idReference));
          return true; // NOPMD - readability
        }
      }
      return false;
    }

    public static class Factory implements InlineParserExtensionFactory {
      @Override
      public Set<Class<?>> getAfterDependents() {
        return Collections.emptySet();
      }

      @Override
      public CharSequence getCharacters() {
        return "{";
      }

      @Override
      public Set<Class<?>> getBeforeDependents() {
        return Collections.emptySet();
      }

      @Override
      public InlineParserExtension apply(LightInlineParser lightInlineParser) {
        return new InsertAnchorInlineParser(lightInlineParser);
      }

      @Override
      public boolean affectsGlobalScope() {
        return false;
      }
    }
  }

  private static class InsertAnchorFormatter implements NodeFormatter {
    private final InsertAnchorOptions options;

    public InsertAnchorFormatter(DataHolder options) {
      this.options = new InsertAnchorOptions(options);
    }

    @Override
    public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
      return options.enableInlineInsertAnchors
          ? Collections.singleton(new NodeFormattingHandler<>(InsertAnchorNode.class, this::render))
          : Collections.emptySet();
    }

    @SuppressWarnings("unused")
    protected void render(InsertAnchorNode node, NodeFormatterContext context, MarkdownWriter markdown) {
      if (options.enableRendering) {
        markdown.append("{{ insert: ");
        markdown.append(node.getType());
        markdown.append(", ");
        markdown.append(node.getIdReference());
        markdown.append(" }}");
      }
    }

    @Override
    public Set<Class<?>> getNodeClasses() {
      return Collections.singleton(InsertAnchorNode.class);
    }

    public static class Factory implements NodeFormatterFactory {

      @Override
      public NodeFormatter create(DataHolder options) {
        return new InsertAnchorFormatter(options);
      }

    }
  }

  private static class InsertAnchorHtmlNodeRenderer implements HtmlNodeRenderer {
    private final InsertAnchorOptions options;

    public InsertAnchorHtmlNodeRenderer(DataHolder options) {
      this.options = new InsertAnchorOptions(options);
    }

    @Override
    public Set<HtmlNodeRendererHandler<?>> getHtmlNodeRendererHandlers() {
      return options.enableInlineInsertAnchors
          ? Collections.singleton(new HtmlNodeRendererHandler<>("insert", Element.class, this::processInsert))
          : Collections.emptySet();
    }

    private void processInsert( // NOPMD used as lambda
        Element node,
        @SuppressWarnings("unused") HtmlNodeConverterContext context,
        HtmlMarkdownWriter out) {

      String type = node.attr("type");
      String idRef = node.attr("id-ref");

      out.append("{{ insert: ");
      out.append(type);
      out.append(", ");
      out.append(idRef);
      out.append(" }}");
    }

    public static class Factory implements HtmlNodeRendererFactory {

      @Override
      public HtmlNodeRenderer apply(DataHolder options) {
        return new InsertAnchorHtmlNodeRenderer(options);
      }
    }
  }

  public static class InsertAnchorNode
      extends Node {

    @NonNull
    private BasedSequence type;
    @NonNull
    private BasedSequence idReference;

    @SuppressWarnings("null")
    public InsertAnchorNode(@NonNull String type, @NonNull String idReference) {
      this(CharSubSequence.of(type), CharSubSequence.of(idReference));
    }

    public InsertAnchorNode(@NonNull BasedSequence type, @NonNull BasedSequence idReference) {
      this.type = type;
      this.idReference = idReference;
    }

    @NonNull
    public BasedSequence getType() {
      return type;
    }

    @NonNull
    public BasedSequence getIdReference() {
      return idReference;
    }

    public void setIdReference(@NonNull BasedSequence value) {
      this.idReference = value;
    }

    @Override
    @NonNull
    public BasedSequence[] getSegments() {
      @NonNull BasedSequence[] retval = { getType(), getIdReference() };
      return retval;
    }

    @Override
    public void getAstExtra(StringBuilder out) {
      segmentSpanChars(out, getType(), "type");
      segmentSpanChars(out, getIdReference(), "id-ref");
    }
  }
}
