package gov.nist.secauto.metaschema.markup.flexmark.insertanchor.internal;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchorNode;

public class InsertAnchorInlineParser implements InlineParserExtension {
	private static final Pattern PATTERN = Pattern.compile("\\{\\{\\s*([^\\s]+)\\s*\\}\\}");

	public InsertAnchorInlineParser(@SuppressWarnings("unused") LightInlineParser inlineParser) {
	}

	@Override
	public void finalizeDocument(InlineParser inlineParser) {
	}

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
	}

	@Override
	public boolean parse(LightInlineParser inlineParser) {
		if (inlineParser.peek() == '{') {
			BasedSequence input = inlineParser.getInput();
			Matcher matcher = inlineParser.matcher(PATTERN);
			if (matcher != null) {
				BasedSequence insert = input.subSequence(matcher.start(1), matcher.end(1));
				inlineParser.appendNode(new InsertAnchorNode(insert));
				return true;
			}
		}
		return false;
	}

	public static class Factory implements InlineParserExtensionFactory {
		@Override
		public Set<Class<? extends InlineParserExtensionFactory>> getAfterDependents() {
			return null;
		}

		@Override
		public CharSequence getCharacters() {
			return "{";
		}

		@Override
		public Set<Class<? extends InlineParserExtensionFactory>> getBeforeDependents() {
			return null;
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
