/**********************************************************************************************
 *
 * Asprise OCR Java API
 * Copyright (C) 1998-2015. Asprise Inc. <asprise.com>
 *
 * This file is licensed under the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * You should have received a copy of the GNU Affero General Public License.  If not, please
 * visit <http://www.gnu.org/licenses/agpl-3.0.html>.
 *
 **********************************************************************************************/
package com.asprise.ocr.sample.util.prefs;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;


class XmlSupport {

    private static final String PREFS_DTD_URI =
        "http://java.sun.com/dtd/preferences.dtd";


    private static final String PREFS_DTD =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +

        "<!-- DTD for preferences -->"               +

        "<!ELEMENT preferences (root) >"             +
        "<!ATTLIST preferences"                      +
        " EXTERNAL_XML_VERSION CDATA \"0.0\"  >"     +

        "<!ELEMENT root (map, node*) >"              +
        "<!ATTLIST root"                             +
        "          type (system|user) #REQUIRED >"   +

        "<!ELEMENT node (map, node*) >"              +
        "<!ATTLIST node"                             +
        "          name CDATA #REQUIRED >"           +

        "<!ELEMENT map (entry*) >"                   +
        "<!ATTLIST map"                              +
        "  MAP_XML_VERSION CDATA \"0.0\"  >"         +
        "<!ELEMENT entry EMPTY >"                    +
        "<!ATTLIST entry"                            +
        "          key CDATA #REQUIRED"              +
        "          value CDATA #REQUIRED >"          ;
    
    private static final String EXTERNAL_XML_VERSION = "1.0";


    private static final String MAP_XML_VERSION = "1.0";

    
    static void export(OutputStream os, final Preferences p, boolean subTree)
        throws IOException, BackingStoreException {
        if (((FileSystemPreferences)p).isRemoved()) 
            throw new IllegalStateException("Node has been removed");
        Document doc = createPrefsDoc("preferences");
        Element preferences =  doc.getDocumentElement() ;
        preferences.setAttribute("EXTERNAL_XML_VERSION", EXTERNAL_XML_VERSION);
        Element xmlRoot =  (Element)
        preferences.appendChild(doc.createElement("root"));
        xmlRoot.setAttribute("type", (p.isUserNode() ? "user" : "system"));

        List ancestors = new ArrayList();

        for (Preferences kid = p, dad = kid.parent(); dad != null;
                                   kid = dad, dad = kid.parent()) {
            ancestors.add(kid);
        }
        Element e = xmlRoot;
        for (int i=ancestors.size()-1; i >= 0; i--) {
            e.appendChild(doc.createElement("map"));
            e = (Element) e.appendChild(doc.createElement("node"));
            e.setAttribute("name", ((Preferences)ancestors.get(i)).name());
        }
        putPreferencesInXml(e, doc, p, subTree);

        writeDoc(doc, os);
    }

    
    private static void putPreferencesInXml(Element elt, Document doc,
               Preferences prefs, boolean subTree) throws BackingStoreException
    {
        Preferences[] kidsCopy = null;
        String[] kidNames = null;

        synchronized (((FileSystemPreferences)prefs).getLock()) { 
            if (((FileSystemPreferences)prefs).isRemoved()) { 
                elt.getParentNode().removeChild(elt);
                return;
            }

            String[] keys = prefs.keys();
            Element map = (Element) elt.appendChild(doc.createElement("map"));
            for (int i=0; i<keys.length; i++) {
                Element entry = (Element)
                    map.appendChild(doc.createElement("entry"));
                entry.setAttribute("key", keys[i]);

                entry.setAttribute("value", prefs.get(keys[i], null));
            }

            if (subTree) {

                kidNames = prefs.childrenNames();
                kidsCopy = new Preferences[kidNames.length];
                for (int i = 0; i <  kidNames.length; i++)
                    kidsCopy[i] = prefs.node(kidNames[i]);
            }

        }

        if (subTree) {
            for (int i=0; i < kidNames.length; i++) {
                Element xmlKid = (Element)
                    elt.appendChild(doc.createElement("node"));
                xmlKid.setAttribute("name", kidNames[i]);
                putPreferencesInXml(xmlKid, doc, kidsCopy[i], subTree);
            }
        }
    }

    
    static void importPreferences(InputStream is)
        throws IOException, InvalidPreferencesFormatException
    {
        try {
            Document doc = loadPrefsDoc(is);
            String xmlVersion =
                doc.getDocumentElement().getAttribute("EXTERNAL_XML_VERSION");
            if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
                throw new InvalidPreferencesFormatException(
                "Exported preferences file format version " + xmlVersion +
                " is not supported. This java installation can read" +
                " versions " + EXTERNAL_XML_VERSION + " or older. You may need" +
                " to install a newer version of JDK.");

            Element xmlRoot = (Element) doc.getDocumentElement().
                                               getChildNodes().item(0);
            Preferences prefsRoot =
                (xmlRoot.getAttribute("type").equals("user") ?
                            Preferences.userRoot() : Preferences.systemRoot());
            ImportSubtree(prefsRoot, xmlRoot);
        } catch(SAXException e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    
    private static Document createPrefsDoc( String qname ) {
        try {
            DOMImplementation di = DocumentBuilderFactory.newInstance().
                newDocumentBuilder().getDOMImplementation();
            DocumentType dt = di.createDocumentType(qname, null, PREFS_DTD_URI);
            return di.createDocument(null, qname, dt);
        } catch(ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    
    private static Document loadPrefsDoc(InputStream in)
        throws SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            return db.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    
    private static final void writeDoc(Document doc, OutputStream out)
        throws IOException
    {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("indent-number", new Integer(2));
            } catch (IllegalArgumentException iae) {

            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            t.transform(new DOMSource(doc),
                        new StreamResult(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
        } catch(TransformerException e) {
            throw new AssertionError(e);
        }
    }

    
    private static void ImportSubtree(Preferences prefsNode, Element xmlNode) {
        NodeList xmlKids = xmlNode.getChildNodes();
        int numXmlKids = xmlKids.getLength();

        Preferences[] prefsKids;

        synchronized (((FileSystemPreferences)prefsNode).getLock()) { 
            if (((FileSystemPreferences)prefsNode).isRemoved()) 
                return;

            Element firstXmlKid = (Element) xmlKids.item(0);
            ImportPrefs(prefsNode, firstXmlKid);
            prefsKids = new Preferences[numXmlKids - 1];

            for (int i=1; i < numXmlKids; i++) {
                Element xmlKid = (Element) xmlKids.item(i);
                prefsKids[i-1] = prefsNode.node(xmlKid.getAttribute("name"));
            }
        } 
        for (int i=1; i < numXmlKids; i++)
            ImportSubtree(prefsKids[i-1], (Element)xmlKids.item(i));
    }

    
    private static void ImportPrefs(Preferences prefsNode, Element map) {
        NodeList entries = map.getChildNodes();
        for (int i=0, numEntries = entries.getLength(); i < numEntries; i++) {
            Element entry = (Element) entries.item(i);
            prefsNode.put(entry.getAttribute("key"),
                          entry.getAttribute("value"));
        }
    }

    
    static void exportMap(OutputStream os, Map map) throws IOException {
        Document doc = createPrefsDoc("map");
        Element xmlMap = doc.getDocumentElement( ) ;
        xmlMap.setAttribute("MAP_XML_VERSION", MAP_XML_VERSION);

        for (Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Element xe = (Element)
                xmlMap.appendChild(doc.createElement("entry"));
            xe.setAttribute("key",   (String) e.getKey());
            xe.setAttribute("value", (String) e.getValue());
        }

        writeDoc(doc, os);
    }

    
    static void importMap(InputStream is, Map m)
        throws IOException, InvalidPreferencesFormatException
    {
        try {
            Document doc = loadPrefsDoc(is);
            Element xmlMap = doc.getDocumentElement();

            String mapVersion = xmlMap.getAttribute("MAP_XML_VERSION");
            if (mapVersion.compareTo(MAP_XML_VERSION) > 0)
                throw new InvalidPreferencesFormatException(
                "Preferences map file format version " + mapVersion +
                " is not supported. This java installation can read" +
                " versions " + MAP_XML_VERSION + " or older. You may need" +
                " to install a newer version of JDK.");

            NodeList entries = xmlMap.getChildNodes();
            for (int i=0, numEntries=entries.getLength(); i<numEntries; i++) {
                Element entry = (Element) entries.item(i);
                m.put(entry.getAttribute("key"), entry.getAttribute("value"));
            }
        } catch(SAXException e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    private static class Resolver implements EntityResolver {
        public InputSource resolveEntity(String pid, String sid)
            throws SAXException
        {
            if (sid.equals(PREFS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PREFS_DTD));
                is.setSystemId(PREFS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
}
