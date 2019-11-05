package com.andersen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Handler {
    private static final String DELETE_ALL_ENTRIES = "DELETE FROM test;";
    private static final String INSERT_ENTRIES = "INSERT INTO test (FIELD) VALUES (?);";
    private static final String SELECT_ENTRIES = "SELECT FIELD FROM test";
    private static final String FIELD = "FIELD";
    private static final String INITIAL_XML = "1.xml";
    private static final String CORRECTED_XML = "2.xml";
    private static final String ROOT_ELEMENT = "entries";
    private static final String ENTRY_ELEMENT = "entry";
    private static final String FIELD_ELEMENT = "field";
    private static final String XSL_FILE = "xslt.xsl";
    private List<Integer> valuesFromDB = new ArrayList<>();
    private String url;
    private String username;
    private String pass;
    private int number;

    public Handler() {
    }

    public Handler(String url, String username, String pass, int number) {
        this.url = url;
        this.username = username;
        this.pass = pass;
        this.number = number;
        populateDB();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    private void populateDB() {
        System.out.println("start populate");
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(DELETE_ALL_ENTRIES);
            }
            System.out.println("end delete");
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ENTRIES)) {
                for (int i = 1; i <= number; i++) {
                    preparedStatement.clearParameters();
                    preparedStatement.setInt(1, i);
                    preparedStatement.addBatch();
                }
                System.out.println("start db");
                preparedStatement.clearParameters();
                int[] results = preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("end populate");
    }


    public void generateXML() {
        System.out.println("start generate");
        readFromDB();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(ROOT_ELEMENT);
            doc.appendChild(rootElement);
            for (Integer digit : valuesFromDB) {
                Element entry = doc.createElement(ENTRY_ELEMENT);
                rootElement.appendChild(entry);
                Element field = doc.createElement(FIELD_ELEMENT);
                field.appendChild(doc.createTextNode(digit.toString()));
                entry.appendChild(field);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(INITIAL_XML));
            transformer.transform(source, result);
            StreamSource styleSource = new StreamSource(XSL_FILE);
            StreamResult correctedXML = new StreamResult(new File(CORRECTED_XML));
            Transformer correctedTransformer = transformerFactory.newTransformer(styleSource);
            correctedTransformer.transform(source, correctedXML);
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
        System.out.println("end generate");
    }


    private void readFromDB() {
        valuesFromDB.clear();
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(SELECT_ENTRIES)) {
                while (rs.next()) {
                    valuesFromDB.add(rs.getInt(FIELD));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long calculateSum() {
        System.out.println("start calculate");
        long sum = 0;
        try {
            File inputFile = new File(CORRECTED_XML);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();

            String expression = "//entry/@field";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                sum += Integer.parseInt(nNode.getNodeValue());
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        System.out.println("end calculate");
        return sum;
    }
}
