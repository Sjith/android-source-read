/*
 This Java source file was generated by test-to-java.xsl
 and is a derived work from the source document.
 The source document contained the following notice:


 Copyright (c) 2001-2004 World Wide Web Consortium,
 (Massachusetts Institute of Technology, Institut National de
 Recherche en Informatique et en Automatique, Keio University). All
 Rights Reserved. This program is distributed under the W3C's Software
 Intellectual Property License. This program is distributed in the
 hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.
 See W3C License http://www.w3.org/Consortium/Legal/ for more details.

 */

package tests.org.w3c.dom;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;

import javax.xml.parsers.DocumentBuilder;

/**
 * Attempt to insert an element into an attribute list, should raise a
 * HIERARCHY_REQUEST_ERR.
 * 
 * @author Curt Arnold
 * @see <a
 *      href="http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#xpointer(id('ID-258A00AF')/constant[@name='HIERARCHY_REQUEST_ERR'])">http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#xpointer(id('ID-258A00AF')/constant[@name='HIERARCHY_REQUEST_ERR'])</a>
 * @see <a
 *      href="http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#ID-1025163788">http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#ID-1025163788</a>
 * @see <a
 *      href="http://www.w3.org/2000/11/DOM-Level-2-errata#core-4">http://www.w3.org/2000/11/DOM-Level-2-errata#core-4</a>
 */
@TestTargetClass(NamedNodeMap.class) 
public final class HCNamedNodeMapInvalidType extends DOMTestCase {

    DOMDocumentBuilderFactory factory;

    DocumentBuilder builder;

    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration1());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }

    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }

    /**
     * Runs the test case.
     * 
     * @throws Throwable
     *             Any uncaught exception causes test to fail
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItem method throws DOMException with HIERARCHY_REQUEST_ERR code.",
        method = "setNamedItem",
        args = {org.w3c.dom.Node.class}
    )
    public void testNamedNodeMapInvalidType() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Element docElem;
        Element newElem;

        doc = (Document) load("hc_staff", builder);
        docElem = doc.getDocumentElement();
        attributes = docElem.getAttributes();
        newElem = doc.createElement("html");

        {
            boolean success = false;
            try {
                attributes.setNamedItem(newElem);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
            }
            assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
        }
    }

}
