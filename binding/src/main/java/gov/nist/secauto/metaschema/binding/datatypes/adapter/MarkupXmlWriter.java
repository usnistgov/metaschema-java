package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.codehaus.stax2.evt.XMLEventFactory2;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.superscript.Superscript;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitorBase;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchorNode;

public class MarkupXmlWriter {
	private final XMLEventFactory2 eventFactory;
	private final String namespace;

	private static final Map<String, String> entityMap;
	static {
		entityMap = new HashMap<>();
		entityMap.put("&amp;", "&");
		entityMap.put("&lt;", "<");
		entityMap.put("&gt;", ">");
		entityMap.put("&lsquo;", "‘");
		entityMap.put("&rsquo;", "’");
		entityMap.put("&hellip;", "…");
		entityMap.put("&mdash;", "—");
		entityMap.put("&ndash;", "–");
		entityMap.put("&ldquo;", "“");
		entityMap.put("&rdquo;", "\u201D");
		entityMap.put("&laquo;", "«");
		entityMap.put("&raquo;", "»");

	}

	public MarkupXmlWriter(String namespace, XMLEventFactory2 eventFactory) {
		super();
		Objects.requireNonNull(namespace, "namespace");
		Objects.requireNonNull(eventFactory, "eventFactory");
		this.namespace = namespace;
		this.eventFactory = eventFactory;
	}

	protected XMLEventFactory2 getEventFactory() {
		return eventFactory;
	}

	protected String getNamespace() {
		return namespace;
	}

	public void process(Document document, XMLEventWriter writer, boolean handleBlockElements) throws BindingException {
		Visitor visitor = new Visitor(writer, handleBlockElements);
		for (Node node : document.getChildren()) {
			try {
				visitor.visit(node);
			} catch (Exception ex) {
				throw new BindingException(ex);
			}
		}
	}

	private class Visitor extends NodeVisitorBase {
		private final XMLEventWriter writer;
		private final boolean handleBlockElements;

		public Visitor(XMLEventWriter writer, boolean handleBlockElements) {
			Objects.requireNonNull(writer, "writer");

			this.writer = writer;
			this.handleBlockElements = handleBlockElements;
		}

		protected XMLEventWriter getWriter() {
			return writer;
		}

		@Override
		protected void visit(Node node) {
			boolean handled;
			try {
				handled = handleInlineElements(node);
				if (!handled && node instanceof Block) {
					if (handleBlockElements) {
						handled = handleBlockElements(node);
					} else {
						super.visitChildren(node);
						handled = true;
					}
				}
			} catch (XMLStreamException ex) {
				throw new RuntimeException(ex);
			}

			if (!handled) {
				throw new UnsupportedOperationException(String.format("Node '%s' not handled.", node.getNodeName()));
			}
		}

		protected boolean handleInlineElements(Node node) throws XMLStreamException {
			boolean retval = false;
			if (node instanceof Text) {
				handleText(node.getChars().toString());
				retval = true;
			} else if (node instanceof EscapedCharacter) {
				handleEscapedCharacter((EscapedCharacter) node);
				retval = true;
			} else if (node instanceof TypographicSmarts) {
				handleTypographicSmarts((TypographicSmarts) node);
				retval = true;
			} else if (node instanceof TypographicQuotes) {
				handleTypographicSmarts((TypographicQuotes) node);
				retval = true;
			} else if (node instanceof StrongEmphasis) {
				handleBasicElement(node, "strong");
				retval = true;
			} else if (node instanceof Emphasis) {
				handleBasicElement(node, "em");
				retval = true;
			} else if (node instanceof ListItem) {
				handleBasicElement(node, "li");
				retval = true;
			} else if (node instanceof Link) {
				handleLink((Link) node);
				retval = true;
			} else if (node instanceof TextBase) {
				// ignore these, but process their children
				super.visitChildren(node);
				retval = true;
			} else if (node instanceof Subscript) {
				handleBasicElement(node, "sub");
				retval = true;
			} else if (node instanceof Superscript) {
				handleBasicElement(node, "sup");
				retval = true;
			} else if (node instanceof Image) {
				handleImage((Image) node);
				retval = true;
			} else if (node instanceof InsertAnchorNode) {
				handleInsertAnchor((InsertAnchorNode) node);
				retval = true;
			}
			return retval;
		}

		protected boolean handleBlockElements(Node node) throws XMLStreamException {
			boolean retval = false;
			if (node instanceof Paragraph) {
				handleBasicElement(node, "p");
				retval = true;
			} else if (node instanceof Heading) {
				handleHeading((Heading) node);
				retval = true;
			} else if (node instanceof OrderedList) {
				handleBasicElement(node, "ol");
				retval = true;
			} else if (node instanceof BulletList) {
				handleBasicElement(node, "ul");
				retval = true;
			} else if (node instanceof TableBlock) {
				handleTable((TableBlock) node);
				retval = true;
			}
			return retval;
		}

		private void handleImage(Image node) throws XMLStreamException {
			QName name = new QName(getNamespace(), "img");

			List<Attribute> attributes = new LinkedList<>();
			if (node.getUrl() != null) {
				attributes.add(eventFactory.createAttribute("src", node.getUrl().toString()));
			}

			if (node.getUrl() != null) {
				attributes.add(eventFactory.createAttribute("alt", node.getText().toString()));
			}

			StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
			getWriter().add(start);

			EndElement end = eventFactory.createEndElement(name, null);
			getWriter().add(end);
		}

		private void handleTable(TableBlock node) throws XMLStreamException {
			QName name = new QName(getNamespace(), "table");

			StartElement start = eventFactory.createStartElement(name, null, null);
			getWriter().add(start);

			// TODO: handle head and body
			TableHead head = (TableHead) node.getChildOfType(TableHead.class);

			TableBody body = (TableBody) node.getChildOfType(TableBody.class);

			EndElement end = eventFactory.createEndElement(name, null);
			getWriter().add(end);
		}

		private void handleInsertAnchor(InsertAnchorNode node) throws XMLStreamException {
			QName name = new QName(getNamespace(), "insert");

			List<Attribute> attributes = new LinkedList<>();
			if (node.getName() != null) {
				attributes.add(eventFactory.createAttribute("param-id", node.getName().toString()));
			}

			StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
			getWriter().add(start);

			super.visitChildren(node);

			EndElement end = eventFactory.createEndElement(name, null);
			getWriter().add(end);
		}

		private void handleHeading(Heading node) throws XMLStreamException {
			int level = node.getLevel();

			handleBasicElement(node, String.format("h%d", level));
		}

		private void handleText(String text) throws XMLStreamException {
			getWriter().add(eventFactory.createCharacters(text));
		}

		private void handleEscapedCharacter(EscapedCharacter node) throws XMLStreamException {
			getWriter().add(eventFactory.createCharacters(node.getChars().unescape()));
		}

		private void handleTypographicSmarts(TypographicQuotes node) throws XMLStreamException {
			if (node.getTypographicOpening() != null && !node.getTypographicOpening().isEmpty()) {
				getWriter().add(eventFactory.createCharacters(mapEntity(node.getTypographicOpening())));
			}
			super.visitChildren(node);
			if (node.getTypographicClosing() != null && !node.getTypographicClosing().isEmpty()) {
				getWriter().add(eventFactory.createCharacters(mapEntity(node.getTypographicClosing().replaceFirst("&", "\\&"))));
			}
		}

		private String mapEntity(String entity) {
			String replacement = entityMap.get(entity);
			return replacement != null ? replacement : entity;
		}

		private void handleTypographicSmarts(TypographicSmarts node) throws XMLStreamException {
			getWriter().add(eventFactory.createCharacters(mapEntity(node.getTypographicText())));
		}

		private void handleBasicElement(Node node, String localName) throws XMLStreamException {
			QName name = new QName(getNamespace(), localName);
			StartElement start = eventFactory.createStartElement(name, null, null);
			getWriter().add(start);
			super.visitChildren(node);
			EndElement end = eventFactory.createEndElement(name, null);
			getWriter().add(end);
		}

		private void handleLink(Link node) throws XMLStreamException {
			QName name = new QName(getNamespace(), "a");

			List<Attribute> attributes = new LinkedList<>();
			if (node.getUrl() != null) {
				attributes.add(eventFactory.createAttribute("href", node.getUrl().toString()));
			}

			StartElement start = eventFactory.createStartElement(name, attributes.iterator(), null);
			getWriter().add(start);

			super.visitChildren(node);

			EndElement end = eventFactory.createEndElement(name, null);
			getWriter().add(end);
		}
	}
}
