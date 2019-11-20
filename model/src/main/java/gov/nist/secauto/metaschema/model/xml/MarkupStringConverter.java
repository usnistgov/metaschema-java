package gov.nist.secauto.metaschema.model.xml;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;

import gov.nist.csrc.ns.oscal.metaschema.x10.MarkupContentType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public class MarkupStringConverter {
	private static final Logger logger = LogManager.getLogger(MarkupStringConverter.class);

	private MarkupStringConverter() {
		// disable construction
	}

	public static MarkupString toMarkupString(MarkupContentType content) {
		XmlOptions options = new XmlOptions();
		options.setSaveInner();
		options.setSaveUseOpenFrag();
		StringWriter writer = new StringWriter();
		try {
			content.save(writer, options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String retval = writer.toString().replaceFirst("^<frag\\:fragment[^>]+>", "").replaceFirst("</frag\\:fragment>$", "");
		logger.info(retval);
		return MarkupString.fromHTML(retval);
	}
}