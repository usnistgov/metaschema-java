package gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.internal;

import java.util.Collections;
import java.util.Set;

import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.util.data.DataHolder;

import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.InsertAnchorNode;
import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.InsertAnchorOptions;

public class InsertAnchorFormatter implements NodeFormatter  {
	private final InsertAnchorOptions options;
	
	public InsertAnchorFormatter(DataHolder options) {
		this.options = new InsertAnchorOptions(options);
	}

	@Override
	public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
		return options.enableInlineInsertAnchors ? Collections.singleton(
				new NodeFormattingHandler<InsertAnchorNode>(InsertAnchorNode.class, this::render)) : Collections.emptySet();
	}

	protected void render(InsertAnchorNode node, @SuppressWarnings("unused") NodeFormatterContext context, MarkdownWriter markdown) {
		if (options.enableRendering) {
			markdown.append("{{ ");
			markdown.append(node.getName());
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
