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

import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.Escaping;
import com.vladsch.flexmark.util.sequence.Range;
import com.vladsch.flexmark.util.sequence.SequenceUtils;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class HtmlCodeRenderExtension
    implements HtmlRenderer.HtmlRendererExtension {
  private static final Pattern EOL_PATTERN = Pattern.compile("\r\n|\r|\n");

  /**
   * Construct a new extension instance.
   *
   * @return the instance
   */
  public static HtmlCodeRenderExtension newInstance() {
    return new HtmlCodeRenderExtension();
  }

  @Override
  public void rendererOptions(MutableDataHolder options) {
    // do nothing
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
    rendererBuilder.nodeRendererFactory(new CodeNodeHtmlRenderer.Factory());
  }

  static final class CodeNodeHtmlRenderer implements NodeRenderer {
    private final boolean codeSoftLineBreaks;

    private CodeNodeHtmlRenderer(DataHolder options) {
      codeSoftLineBreaks = Parser.CODE_SOFT_LINE_BREAKS.get(options);
    }

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      return Collections.singleton(
          new NodeRenderingHandler<>(Code.class, this::render));
    }

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "false positive")
    private void render( // NOPMD actually used in lambda
        @NonNull Code node,
        @NonNull NodeRendererContext context,
        @NonNull HtmlWriter html) {
      HtmlRendererOptions htmlOptions = context.getHtmlOptions();

      boolean customTag = htmlOptions.codeStyleHtmlOpen != null || htmlOptions.codeStyleHtmlClose != null;
      if (customTag) {
        html.raw(ObjectUtils.notNull(htmlOptions.codeStyleHtmlOpen));
      } else {
        if (context.getHtmlOptions().sourcePositionParagraphLines) {
          html.withAttr().tag("code");
        } else {
          html.srcPos(node.getText()).withAttr().tag("code");
        }
      }

      if (codeSoftLineBreaks && !htmlOptions.isSoftBreakAllSpaces) {
        for (Node child : node.getChildren()) {
          if (child instanceof Text) {
            html.text(Escaping.collapseWhitespace(child.getChars(), false));
          } else {
            context.render(child);
          }
        }
      } else {
        String text = EOL_PATTERN.matcher(node.getText()).replaceAll(" ");
        if (!text.isBlank() && SequenceUtils.startsWithWhitespace(text) && SequenceUtils.endsWithWhitespace(text)) {
          html.text(SequenceUtils.subSequence(text, Range.of(1, text.length() - 1)));
        } else {
          html.raw(Escaping.escapeHtml(text, false));
        }
        // html.text(Escaping.collapseWhitespace(node.getText(), true));
      }

      if (customTag) {
        html.raw(ObjectUtils.notNull(htmlOptions.codeStyleHtmlClose));
      } else {
        html.tag("/code");
      }
    }

    public static class Factory implements NodeRendererFactory {

      @Override
      public NodeRenderer apply(DataHolder options) {
        return new CodeNodeHtmlRenderer(options);
      }

    }
  }
}
