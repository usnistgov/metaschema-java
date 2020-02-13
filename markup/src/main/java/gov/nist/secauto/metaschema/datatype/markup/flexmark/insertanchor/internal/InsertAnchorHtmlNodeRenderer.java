package gov.nist.secauto.metaschema.datatype.markup.flexmark.insertanchor.internal;

import java.util.Collections;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.vladsch.flexmark.html2md.converter.HtmlMarkdownWriter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeConverterContext;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererFactory;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererHandler;
import com.vladsch.flexmark.util.data.DataHolder;

import gov.nist.secauto.metaschema.datatype.markup.flexmark.insertanchor.InsertAnchorOptions;

public class InsertAnchorHtmlNodeRenderer implements HtmlNodeRenderer {
	private final InsertAnchorOptions options;

	public InsertAnchorHtmlNodeRenderer(DataHolder options) {
		this.options = new InsertAnchorOptions(options);
	}

	@Override
	public Set<HtmlNodeRendererHandler<?>> getHtmlNodeRendererHandlers() {
		return options.enableInlineInsertAnchors ? Collections.singleton(
				new HtmlNodeRendererHandler<>("insert",Element.class, this::processInsert)) : Collections.emptySet();
	}

	private void processInsert(@SuppressWarnings("unused") Element node, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        out.append("{{ ");
        out.append(context.getCurrentNode().attr("param-id"));
        out.append(" }}");
    }

	public static class Factory implements HtmlNodeRendererFactory {

		@Override
		public HtmlNodeRenderer apply(DataHolder options) {
			return new InsertAnchorHtmlNodeRenderer(options);
		}
	}

}
