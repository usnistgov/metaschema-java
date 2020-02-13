package gov.nist.secauto.metaschema.datatype.markup.flexmark.insertanchor;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.CharSubSequence;

public class InsertAnchorNode extends Node {
	private BasedSequence name;

    public InsertAnchorNode(String chars) {
        this(CharSubSequence.of(chars));
    }

	public InsertAnchorNode(BasedSequence chars) {
		super();
		this.name = chars;
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
