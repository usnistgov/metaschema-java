package gov.nist.secauto.metaschema.datatype.flexmark.insertanchor;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class InsertAnchorNode extends Node {
	private BasedSequence name;

	public InsertAnchorNode(BasedSequence name) {
		super(name);
		this.name = name;
	}

	public BasedSequence getName() {
		return name;
	}

	@Override
	public BasedSequence[] getSegments() {
		return new BasedSequence[] { getName() };
	}

	@Override
    public void getAstExtra(StringBuilder out) {
        segmentSpanChars(out, name, "name");
    }
}
