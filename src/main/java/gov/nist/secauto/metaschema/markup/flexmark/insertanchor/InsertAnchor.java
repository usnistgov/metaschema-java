package gov.nist.secauto.metaschema.markup.flexmark.insertanchor;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class InsertAnchor extends Node {
	private BasedSequence name;

	public InsertAnchor(BasedSequence name) {
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
