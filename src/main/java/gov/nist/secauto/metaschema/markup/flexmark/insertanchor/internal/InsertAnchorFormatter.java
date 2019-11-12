package gov.nist.secauto.metaschema.markup.flexmark.insertanchor.internal;

import java.util.Collections;
import java.util.Set;

import com.vladsch.flexmark.formatter.CustomNodeFormatter;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;

import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchor;

public class InsertAnchorFormatter implements NodeFormatter  {

	public InsertAnchorFormatter(DataHolder options) {
	}

	@Override
	public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
		return Collections.singleton(
				new NodeFormattingHandler<InsertAnchor>(InsertAnchor.class, new CustomNodeFormatter<InsertAnchor>() {

					@Override
					public void render(InsertAnchor node, NodeFormatterContext context, MarkdownWriter markdown) {
						InsertAnchorFormatter.this.render(node,context, markdown);
					}

				}));
	}

	protected void render(InsertAnchor node, NodeFormatterContext context, MarkdownWriter markdown) {
		markdown.append("{{ ");
		markdown.append(node.getName());
		markdown.append(" }}");
	}

	@Override
	public Set<Class<?>> getNodeClasses() {
		return Collections.singleton(InsertAnchor.class);
	}

	public static class Factory implements NodeFormatterFactory {

		@Override
		public NodeFormatter create(DataHolder options) {
			return new InsertAnchorFormatter(options);
		}

	}

}
