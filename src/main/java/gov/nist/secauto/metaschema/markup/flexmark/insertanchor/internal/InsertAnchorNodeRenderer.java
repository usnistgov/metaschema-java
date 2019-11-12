package gov.nist.secauto.metaschema.markup.flexmark.insertanchor.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;

import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchor;
import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchorOptions;

public class InsertAnchorNodeRenderer implements NodeRenderer {
	private final InsertAnchorOptions options;

	public InsertAnchorNodeRenderer(DataHolder options) {
		this.options = new InsertAnchorOptions(options);
	}

	@Override
	public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
		return Collections.singleton(
				new NodeRenderingHandler<InsertAnchor>(InsertAnchor.class, new CustomNodeRenderer<InsertAnchor>() {
					@Override
					public void render(InsertAnchor node, NodeRendererContext context, HtmlWriter html) {
						InsertAnchorNodeRenderer.this.render(node, context, html);
					}

				}));
	}

	protected void render(InsertAnchor node, NodeRendererContext context, HtmlWriter html) {
		if (options.enableRendering) {
			html.attr("param-id", node.getName()).withAttr().tag("insert", true);
		}
	}

//	 @Override
//    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
//        HashSet<NodeRenderingHandler<?>> set = new HashSet<NodeRenderingHandler<?>>();
//        set.add(new NodeRenderingHandler<Macro>(Macro.class, new CustomNodeRenderer<Macro>() {
//            @Override
//            public void render(Macro node, NodeRendererContext context, HtmlWriter html) { MacroNodeRenderer.this.render(node, context, html); }
//        }));
	public static class Factory implements NodeRendererFactory {

		@Override
		public NodeRenderer apply(DataHolder options) {
			return new InsertAnchorNodeRenderer(options);
		}

	}

}
