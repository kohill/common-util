package com.healthedge.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class XmlUtil {

    private static final Logger LOG = LogManager.getLogger(XmlUtil.class);

    public static ArrayList<String> returnValueFromXmlNode(String filePath, String xmlFileName, String tagName) throws IOException, SAXException {
        Document doc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            File inputFile = new File(filePath + xmlFileName);
            doc = docBuilder.parse(inputFile);
            doc.normalize();
        } catch (ParserConfigurationException e) {
            LOG.error("Failed to create document: {}", filePath + xmlFileName);
            LOG.error(e.getStackTrace());
        }

        if (doc == null) {
            throw new RuntimeException("Error while building XML document, document should not be null!");
        }

        NodeList nodeList = doc.getElementsByTagName(tagName);
        Node node;
        ArrayList<String> valuesFound = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                valuesFound.add(node.getFirstChild().getNodeValue());
            }
        }

        return valuesFound;
    }

    public static Document returnXmlDocument(String xmlString) {
        Document result;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            result = builder.parse(is);
        } catch (Exception e) {
            LOG.error("Can't parse xml: " + e.getMessage());
            LOG.error("XML contents: \n{}", xmlString);
            throw new XmlException(e.getMessage());
        }
        return result;
    }

    public static Object evaluateXPathExpression(Document document, String expression, QName resultType) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            return xPath.compile(expression).evaluate(document, resultType);
        } catch (XPathExpressionException e) {
            LOG.error("Can't evaluate xpath expression: {}", expression);
            throw new XmlException(e.getMessage());
        }
    }

    public static <T> T xmlToModel(String xmlContent, Class<T> modelClass) {
        return xmlToModel(xmlContent, modelClass, true);
    }

    public static <T> T xmlToModel(String xmlContent, Class<T> modelClass, boolean strictMatchBinding) {
        T model;
        StreamSource source = new StreamSource(new StringReader(xmlContent));

        LOG.debug(String.format("Getting \"%1$s\" object model from provided xml content%2$s.", modelClass.getSimpleName(), strictMatchBinding ? " with strict match binding" : ""));
        try {
            Unmarshaller jaxbUnmarshaller = getUnmarshaller(modelClass);
            if (strictMatchBinding) {
                jaxbUnmarshaller.setEventHandler(event -> false);
            }

            model = jaxbUnmarshaller.unmarshal(source, modelClass).getValue();
        } catch (JAXBException e) {
            LOG.error(e.getMessage());
            throw new XmlException(String.format("Unable to unmarshal xml content to model: \"%s\".", modelClass.getSimpleName()));
        }

        LOG.debug("Xml unmarshalling was successful.");
        return model;
    }

    /**
     * Create JAXB model class Using part of xml
     * @param xmlContent part of xml
     * @param modelClass jaxb model class
     * @return XML content in String format
     */
    public static <T> T xmlToModelByPartOfXml(String xmlContent, Class<T> modelClass)  {
        T model = null;
        XMLInputFactory xif = XMLInputFactory.newFactory();
        StreamSource source = new StreamSource(new StringReader(xmlContent));
        try {
            XMLStreamReader xsr = xif.createXMLStreamReader(source);
            Unmarshaller unmarshaller = getUnmarshaller(modelClass);
            JAXBElement<T> jb = unmarshaller.unmarshal(xsr, modelClass);
            xsr.close();
            model = jb.getValue();
        } catch (XMLStreamException | JAXBException e) {
            LOG.error("Error appears in attempt to unmarshal xml");
        }

        return model;
    }

    private static <T> Unmarshaller getUnmarshaller(Class<T> modelClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(modelClass);
        return jaxbContext.createUnmarshaller();
    }

    /**
     *
     * @param doc
     * @return string value of the document
     */
    public static String documentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        StringWriter writer = new StringWriter();
        StreamResult docToString = new StreamResult(writer);

        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), docToString);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    /**
     * Modify value in xml document by xpath
     * @param xmlXpath /soap/envelope/policylist
     * @param document
     * @param value to be set
     * @return
     */
    public static Document setXmlField(Document document, String xmlXpath, String value) {
        NodeList nodes = getNodeListByXPath(document, xmlXpath);

        LOG.info("Setting value: {}, by path: {}", value, xmlXpath);
        nodes.item(0).setTextContent(value);

        return document;
    }

    public static void removeElementFromXPath(Document document, String xPathString) {
        NodeList nlist = getNodeListByXPath(document, xPathString);
        for (int i = 0; i < nlist.getLength(); i++) {
            Node node = nlist.item(i);
            Node parent = node.getParentNode();
            if (parent != null) {
                parent.removeChild(node);
            }
        }

    }

    private static NodeList getNodeListByXPath(Document document, String xPathString) {
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        Object result = null;
        try {
            result = xpath.evaluate(xPathString, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return (NodeList) result;
    }

}
