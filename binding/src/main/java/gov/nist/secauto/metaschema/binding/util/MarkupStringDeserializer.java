/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.binding.util;

/*
 * public class MarkupStringDeserializer extends StdDeserializer<MarkupString> { private static
 * final Logger logger = LogManager.getLogger(MarkupStringDeserializer.class);
 * 
 * private static final long serialVersionUID = 1L;
 * 
 * public MarkupStringDeserializer() { this(MarkupString.class); }
 * 
 * public MarkupStringDeserializer(Class<?> vc) { super(vc); }
 * 
 * @Override public MarkupString deserialize(JsonParser p, DeserializationContext ctxt) throws
 * IOException, JsonProcessingException {
 * 
 * MarkupString retval; if (p instanceof FromXmlParser) { logger.info("deserialize start"); // xml
 * mode XMLStreamReader2 reader = (XMLStreamReader2)((FromXmlParser)p).getStaxReader(); try { int
 * token; while ((token = reader.getEventType()) != XMLStreamReader2.END_ELEMENT) {
 * printToken("OUTER: ",reader); if (token == XMLStreamReader2.CHARACTERS) { // text between blocks
 * token = reader.next(); continue; } handleBlock(reader); } printToken("OUTER(after): ", reader); }
 * catch (XMLStreamException e) { e.printStackTrace(); throw JsonMappingException.from(p,
 * "parse error",e); } printToken("OUTER(end): ", reader); // logger.info("{}: ",p.nextToken()); p.
 * logger.info("{}: ",p.currentToken()); retval = MarkupString.fromMarkdown("unparsed"); } else { //
 * json mode retval = MarkupString.fromMarkdown(p.getText()); } return retval; }
 * 
 * private void handleBlock(XMLStreamReader2 reader) throws XMLStreamException {
 * printToken("START BLOCK", reader); int token = reader.getEventType();
 * 
 * // switch (token) { // case XMLStreamReader2.CHARACTERS: // printToken(reader); // break; // // }
 * 
 * 
 * while ((token = reader.next()) != XMLStreamReader2.END_ELEMENT) { printToken("IN BLOCK", reader);
 * 
 * if (token == XMLStreamReader2.START_ELEMENT) { handleStartObject(reader); } }
 * 
 * printToken("END BLOCK", reader); reader.next(); printToken("END BLOCK(next)", reader); }
 * 
 * private void handleStartObject(XMLStreamReader2 reader) throws XMLStreamException {
 * printToken("START OBJECT", reader);
 * 
 * int token; while ((token = reader.next()) != XMLStreamReader2.END_ELEMENT) {
 * printToken("IN OBJECT", reader);
 * 
 * if (token == XMLStreamReader2.START_ELEMENT) { handleStartObject(reader); } }
 * printToken("END OBJECT", reader); }
 * 
 * private void printToken(String context, XMLStreamReader2 reader) { switch (reader.getEventType())
 * { case XMLStreamReader2.START_ELEMENT: logger.info("{}: START_ELEMENT: {}", context,
 * reader.getName()); break; case XMLStreamReader2.END_ELEMENT: logger.info("{}: END_ELEMENT: {}",
 * context, reader.getName()); break; case XMLStreamReader2.ATTRIBUTE:
 * logger.info("{}: ATTRIBUTE: {}", context, reader.getName()); break; case
 * XMLStreamReader2.PROCESSING_INSTRUCTION: logger.info("{}: PROCESSING_INSTRUCTION", context);
 * break; case XMLStreamReader2.CHARACTERS: logger.info("{}: CHARACTERS: {}", context,
 * reader.getText()); break; case XMLStreamReader2.NAMESPACE: logger.info("{}: NAMESPACE", context);
 * break; case XMLStreamReader2.COMMENT: logger.info("{}: COMMENT: {}", context, reader.getText());
 * break; case XMLStreamReader2.SPACE: logger.info("{}: SPACE", context); break; case
 * XMLStreamReader2.END_DOCUMENT: logger.info("{}: END_DOCUMENT", context); case
 * XMLStreamReader2.ENTITY_REFERENCE: logger.info("{}: ENTITY_REFERENCE: {}", context,
 * reader.getText()); break; case XMLStreamReader2.CDATA: logger.info("{}: CDATA: {}", context,
 * reader.getText()); break; default: logger.info("{}: Unsupported token = {}", context,
 * reader.getEventType()); } }
 * 
 * 
 * }
 */