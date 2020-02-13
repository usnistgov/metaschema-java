package gov.nist.secauto.metaschema.datatype.markup.flexmark.insertanchor;

import com.vladsch.flexmark.util.data.DataHolder;

public class InsertAnchorOptions {
	public final boolean enableInlineInsertAnchors;
    public final boolean enableRendering;

    public InsertAnchorOptions(DataHolder options) {
    	enableInlineInsertAnchors = InsertAnchorExtension.ENABLE_INLINE_INSERT_ANCHORS.getFrom(options);
        enableRendering = InsertAnchorExtension.ENABLE_RENDERING.getFrom(options);
    }
}
