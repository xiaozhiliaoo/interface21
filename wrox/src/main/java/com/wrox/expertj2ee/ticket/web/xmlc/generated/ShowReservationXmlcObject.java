/*
 ************************************
 * XMLC GENERATED CODE, DO NOT EDIT *
 ************************************
 */
package com.wrox.expertj2ee.ticket.web.xmlc.generated;

import org.w3c.dom.*;

/**
 * XMLC Document class, generated from
 * war/WEB-INF/xmlc/showReservation.html
 */
public class ShowReservationXmlcObject extends org.enhydra.xml.xmlc.html.HTMLObjectImpl implements org.enhydra.xml.xmlc.XMLObject, org.enhydra.xml.xmlc.html.HTMLObject {
    private int $elementId_bookingFee = 53;

    private int $elementId_date = 14;

    private int $elementId_minutesHeld = 36;

    private int $elementId_nonAdjacentWarning = 59;

    private int $elementId_performanceName = 9;

    private int $elementId_seat = 42;

    private int $elementId_seatType = 31;

    private int $elementId_seatingPlanImg = 92;

    private int $elementId_seatsRequested = 26;

    private int $elementId_time = 19;

    private int $elementId_title = 3;

    private int $elementId_totalPrice = 48;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_BookingFee;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_Date;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_MinutesHeld;

    private org.enhydra.xml.lazydom.html.HTMLDivElementImpl $element_NonAdjacentWarning;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_PerformanceName;

    private org.enhydra.xml.lazydom.html.HTMLLIElementImpl $element_Seat;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_SeatType;

    private org.enhydra.xml.lazydom.html.HTMLImageElementImpl $element_SeatingPlanImg;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_SeatsRequested;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_Time;

    private org.enhydra.xml.lazydom.html.HTMLTitleElementImpl $element_Title;

    private org.enhydra.xml.lazydom.html.LazyHTMLElement $element_TotalPrice;

    /**
     * Field that is used to identify this as the XMLC generated class
     * in an inheritance chain. Contains a reference to the class object.
     */
    public static final Class XMLC_GENERATED_CLASS = ShowReservationXmlcObject.class;

    /**
     * Field containing CLASSPATH relative name of the source file
     * that this class can be regenerated from.
     */
    public static final String XMLC_SOURCE_FILE = "com/wrox/expertj2ee/ticket/web/xmlc/generated/showReservation.html";

    /**
     * XMLC DOM factory associated with this class.
     */
    private static final org.enhydra.xml.xmlc.dom.XMLCDomFactory fDOMFactory = org.enhydra.xml.xmlc.dom.XMLCDomFactoryCache.getFactory(org.enhydra.xml.xmlc.dom.lazydom.LazyHTMLDomFactory.class);

    /**
     * Lazy DOM document
     */
    private org.enhydra.xml.lazydom.LazyDocument fLazyDocument;

    /**
     * Options used to preformat the document when compiled
     */
    private static final org.enhydra.xml.io.OutputOptions fPreFormatOutputOptions;

    /**
     * Template document shared by all instances.
     */
    private static final org.enhydra.xml.lazydom.TemplateDOM fTemplateDocument;

    /*
     * Class initializer.
     */
    static {
        org.enhydra.xml.lazydom.html.LazyHTMLDocument document = buildTemplateSubDocument();

        fTemplateDocument = new org.enhydra.xml.lazydom.TemplateDOM(document);

        fPreFormatOutputOptions = new org.enhydra.xml.io.OutputOptions();

        fPreFormatOutputOptions.setFormat(org.enhydra.xml.io.OutputOptions.FORMAT_AUTO);

        fPreFormatOutputOptions.setEncoding("ISO-8859-1");

        fPreFormatOutputOptions.setPrettyPrinting(false);

        fPreFormatOutputOptions.setIndentSize(4);

        fPreFormatOutputOptions.setPreserveSpace(true);

        fPreFormatOutputOptions.setOmitXMLHeader(false);

        fPreFormatOutputOptions.setOmitDocType(false);

        fPreFormatOutputOptions.setOmitEncoding(false);

        fPreFormatOutputOptions.setDropHtmlSpanIds(true);

        fPreFormatOutputOptions.setOmitAttributeCharEntityRefs(true);

        fPreFormatOutputOptions.setPublicId(null);

        fPreFormatOutputOptions.setSystemId(null);

        fPreFormatOutputOptions.setMIMEType(null);

        fPreFormatOutputOptions.markReadOnly();

    }

    /**
     * Default constructor.
     */
    public ShowReservationXmlcObject() {
        buildDocument();

    }

    /**
     * Constructor with optional building of the DOM.
     */
    public ShowReservationXmlcObject(boolean buildDOM) {
        if (buildDOM) {

            buildDocument();

        }

    }

    /**
     * Copy constructor.
     */
    public ShowReservationXmlcObject(ShowReservationXmlcObject src) {
        setDocument((Document)src.getDocument().cloneNode(true), src.getMIMEType(), src.getEncoding());

        syncAccessMethods();

    }

    /**
     * Create document as a DOM and initialize accessor method fields.
     */
    public void buildDocument() {
        fLazyDocument = (org.enhydra.xml.lazydom.html.LazyHTMLDocument)((org.enhydra.xml.xmlc.dom.lazydom.LazyDomFactory)fDOMFactory).createDocument(fTemplateDocument);

        fLazyDocument.setPreFormatOutputOptions(fPreFormatOutputOptions);

        setDocument(fLazyDocument, "text/html", "ISO-8859-1");

    }

    /**
     * Create a subtree of the document.
     */
    private static org.enhydra.xml.lazydom.html.LazyHTMLDocument buildTemplateSubDocument() {
        Node $node0, $node1, $node2, $node3, $node4, $node5, $node6, $node7, $node8, $node9, $node10;

        Element $elem0, $elem1, $elem2, $elem3, $elem4, $elem5, $elem6, $elem7, $elem8, $elem9;

        Attr $attr0, $attr1, $attr2, $attr3, $attr4, $attr5, $attr6, $attr7, $attr8, $attr9;

        

        org.enhydra.xml.lazydom.html.LazyHTMLDocument document = (org.enhydra.xml.lazydom.html.LazyHTMLDocument)fDOMFactory.createDocument(null, "HTML", null);

        document.makeTemplateNode(0);

        $elem1 = document.getDocumentElement();

        ((org.enhydra.xml.lazydom.LazyElement)$elem1).makeTemplateNode(1);

        ((org.enhydra.xml.lazydom.LazyElement)$elem1).setPreFormattedText("<HTML>");

        

        $elem2 = document.createTemplateElement("HEAD", 2, "<HEAD>");

        $elem1.appendChild($elem2);

        

        $elem3 = document.createTemplateElement("TITLE", 3, "<TITLE id=\"title\">");

        $elem2.appendChild($elem3);

        

        $attr3 = document.createTemplateAttribute("id", 4);

        $elem3.setAttributeNode($attr3);

        $node4 = document.createTemplateTextNode("title", 5, "title");

        $attr3.appendChild($node4);

        

        $node4 = document.createTemplateTextNode("Seats reserved for", 6, "Seats reserved for");

        $elem3.appendChild($node4);

        

        $elem2 = document.createTemplateElement("BODY", 7, "<BODY>");

        $elem1.appendChild($elem2);

        

        $elem3 = document.createTemplateElement("B", 8, "<B>");

        $elem2.appendChild($elem3);

        

        $elem4 = document.createTemplateElement("SPAN", 9, "<SPAN>");

        $elem3.appendChild($elem4);

        

        $attr4 = document.createTemplateAttribute("id", 10);

        $elem4.setAttributeNode($attr4);

        $node5 = document.createTemplateTextNode("performanceName", 11, "performanceName");

        $attr4.appendChild($node5);

        

        $node5 = document.createTemplateTextNode("Hamlet", 12, "Hamlet");

        $elem4.appendChild($node5);

        

        $node4 = document.createTemplateTextNode(": ", 13, ": ");

        $elem3.appendChild($node4);

        

        $elem4 = document.createTemplateElement("SPAN", 14, "<SPAN>");

        $elem3.appendChild($elem4);

        

        $attr4 = document.createTemplateAttribute("id", 15);

        $elem4.setAttributeNode($attr4);

        $node5 = document.createTemplateTextNode("date", 16, "date");

        $attr4.appendChild($node5);

        

        $node5 = document.createTemplateTextNode("January 1, 1983", 17, "January 1, 1983");

        $elem4.appendChild($node5);

        

        $node4 = document.createTemplateTextNode(" at ", 18, " at ");

        $elem3.appendChild($node4);

        

        $elem4 = document.createTemplateElement("SPAN", 19, "<SPAN>");

        $elem3.appendChild($elem4);

        

        $attr4 = document.createTemplateAttribute("id", 20);

        $elem4.setAttributeNode($attr4);

        $node5 = document.createTemplateTextNode("time", 21, "time");

        $attr4.appendChild($node5);

        

        $node5 = document.createTemplateTextNode("7:30 pm", 22, "7:30 pm");

        $elem4.appendChild($node5);

        

        $node3 = document.createTemplateTextNode(" ", 23, " ");

        $elem2.appendChild($node3);

        

        $elem3 = document.createTemplateElement("BR", 24, "<BR>");

        $elem2.appendChild($elem3);

        

        $elem4 = document.createTemplateElement("P", 25, "<P>");

        $elem3.appendChild($elem4);

        

        $elem5 = document.createTemplateElement("SPAN", 26, "<SPAN>");

        $elem4.appendChild($elem5);

        

        $attr5 = document.createTemplateAttribute("id", 27);

        $elem5.setAttributeNode($attr5);

        $node6 = document.createTemplateTextNode("seatsRequested", 28, "seatsRequested");

        $attr5.appendChild($node6);

        

        $node6 = document.createTemplateTextNode("2", 29, "2");

        $elem5.appendChild($node6);

        

        $node5 = document.createTemplateTextNode(" seats in ", 30, " seats in ");

        $elem4.appendChild($node5);

        

        $elem5 = document.createTemplateElement("SPAN", 31, "<SPAN>");

        $elem4.appendChild($elem5);

        

        $attr5 = document.createTemplateAttribute("id", 32);

        $elem5.setAttributeNode($attr5);

        $node6 = document.createTemplateTextNode("seatType", 33, "seatType");

        $attr5.appendChild($node6);

        

        $node6 = document.createTemplateTextNode("Seat Type", 34, "Seat Type");

        $elem5.appendChild($node6);

        

        $node5 = document.createTemplateTextNode(" have been reserved for you for ", 35, " have been reserved for you for ");

        $elem4.appendChild($node5);

        

        $elem5 = document.createTemplateElement("SPAN", 36, "<SPAN>");

        $elem4.appendChild($elem5);

        

        $attr5 = document.createTemplateAttribute("id", 37);

        $elem5.setAttributeNode($attr5);

        $node6 = document.createTemplateTextNode("minutesHeld", 38, "minutesHeld");

        $attr5.appendChild($node6);

        

        $node6 = document.createTemplateTextNode("5", 39, "5");

        $elem5.appendChild($node6);

        

        $node5 = document.createTemplateTextNode(" minutes to give you time to complete your purchase. The seat numbers are: ", 40, " minutes to give you time to complete your purchase. The seat numbers are: ");

        $elem4.appendChild($node5);

        

        $elem5 = document.createTemplateElement("UL", 41, "<UL>");

        $elem4.appendChild($elem5);

        

        $elem6 = document.createTemplateElement("LI", 42, "<LI id=\"seat\">");

        $elem5.appendChild($elem6);

        

        $attr6 = document.createTemplateAttribute("id", 43);

        $elem6.setAttributeNode($attr6);

        $node7 = document.createTemplateTextNode("seat", 44, "seat");

        $attr6.appendChild($node7);

        

        $node7 = document.createTemplateTextNode("Z1", 45, "Z1");

        $elem6.appendChild($node7);

        

        $elem5 = document.createTemplateElement("P", 46, "<P>");

        $elem4.appendChild($elem5);

        

        $node6 = document.createTemplateTextNode("The total cost of these tickets will be ", 47, "The total cost of these tickets will be ");

        $elem5.appendChild($node6);

        

        $elem6 = document.createTemplateElement("SPAN", 48, "<SPAN>");

        $elem5.appendChild($elem6);

        

        $attr6 = document.createTemplateAttribute("id", 49);

        $elem6.setAttributeNode($attr6);

        $node7 = document.createTemplateTextNode("totalPrice", 50, "totalPrice");

        $attr6.appendChild($node7);

        

        $node7 = document.createTemplateTextNode("totalPrice", 51, "totalPrice");

        $elem6.appendChild($node7);

        

        $node6 = document.createTemplateTextNode(". This includes a booking fee of ", 52, ". This includes a booking fee of ");

        $elem5.appendChild($node6);

        

        $elem6 = document.createTemplateElement("SPAN", 53, "<SPAN>");

        $elem5.appendChild($elem6);

        

        $attr6 = document.createTemplateAttribute("id", 54);

        $elem6.setAttributeNode($attr6);

        $node7 = document.createTemplateTextNode("bookingFee", 55, "bookingFee");

        $attr6.appendChild($node7);

        

        $node7 = document.createTemplateTextNode("bookingFee", 56, "bookingFee");

        $elem6.appendChild($node7);

        

        $node6 = document.createTemplateTextNode(" . ", 57, " . ");

        $elem5.appendChild($node6);

        

        $elem6 = document.createTemplateElement("P", 58, "<P>");

        $elem5.appendChild($elem6);

        

        $elem7 = document.createTemplateElement("DIV", 59, "<DIV id=\"nonAdjacentWarning\">");

        $elem6.appendChild($elem7);

        

        $attr7 = document.createTemplateAttribute("id", 60);

        $elem7.setAttributeNode($attr7);

        $node8 = document.createTemplateTextNode("nonAdjacentWarning", 61, "nonAdjacentWarning");

        $attr7.appendChild($node8);

        

        $elem8 = document.createTemplateElement("B", 62, "<B>");

        $elem7.appendChild($elem8);

        

        $node9 = document.createTemplateTextNode("Please note that due to lack of availability, some of the seats offered are not adjacent.", 63, "Please note that due to lack of availability, some of the seats offered are not adjacent.");

        $elem8.appendChild($node9);

        

        $node8 = document.createTemplateTextNode(" ", 64, " ");

        $elem7.appendChild($node8);

        

        $elem8 = document.createTemplateElement("FORM", 65, "<FORM action=\"otherDate.html\" method=\"GET\">");

        $elem7.appendChild($elem8);

        

        $attr8 = document.createTemplateAttribute("action", 66);

        $elem8.setAttributeNode($attr8);

        $node9 = document.createTemplateTextNode("otherDate.html", 67, "otherDate.html");

        $attr8.appendChild($node9);

        

        $attr8 = document.createTemplateAttribute("method", 68);

        $elem8.setAttributeNode($attr8);

        $node9 = document.createTemplateTextNode("GET", 69, "GET");

        $attr8.appendChild($node9);

        

        $elem9 = document.createTemplateElement("INPUT", 70, "<INPUT type=\"submit\" value=\"Try another date\">");

        $elem8.appendChild($elem9);

        

        $attr9 = document.createTemplateAttribute("type", 71);

        $elem9.setAttributeNode($attr9);

        $node10 = document.createTemplateTextNode("submit", 72, "submit");

        $attr9.appendChild($node10);

        

        $attr9 = document.createTemplateAttribute("value", 73);

        $elem9.setAttributeNode($attr9);

        $node10 = document.createTemplateTextNode("Try another date", 74, "Try another date");

        $attr9.appendChild($node10);

        

        $node9 = document.createTemplateTextNode(" ", 75, " ");

        $elem8.appendChild($node9);

        

        $node9 = document.createTemplateComment(" Input elements with / upset XMLC ", 76);

        $elem8.appendChild($node9);

        

        $node7 = document.createTemplateComment(" Form without any parameters: just needs to look like a form to the user ", 77);

        $elem6.appendChild($node7);

        

        $elem7 = document.createTemplateElement("FORM", 78, "<FORM action=\"payment.html\" method=\"GET\">");

        $elem6.appendChild($elem7);

        

        $attr7 = document.createTemplateAttribute("action", 79);

        $elem7.setAttributeNode($attr7);

        $node8 = document.createTemplateTextNode("payment.html", 80, "payment.html");

        $attr7.appendChild($node8);

        

        $attr7 = document.createTemplateAttribute("method", 81);

        $elem7.setAttributeNode($attr7);

        $node8 = document.createTemplateTextNode("GET", 82, "GET");

        $attr7.appendChild($node8);

        

        $elem8 = document.createTemplateElement("INPUT", 83, "<INPUT type=\"submit\" value=\"Proceed\">");

        $elem7.appendChild($elem8);

        

        $attr8 = document.createTemplateAttribute("type", 84);

        $elem8.setAttributeNode($attr8);

        $node9 = document.createTemplateTextNode("submit", 85, "submit");

        $attr8.appendChild($node9);

        

        $attr8 = document.createTemplateAttribute("value", 86);

        $elem8.setAttributeNode($attr8);

        $node9 = document.createTemplateTextNode("Proceed", 87, "Proceed");

        $attr8.appendChild($node9);

        

        $node7 = document.createTemplateTextNode("Please check the seating plan below for the location of the reserved seats. ", 88, "Please check the seating plan below for the location of the reserved seats. ");

        $elem6.appendChild($node7);

        

        $elem7 = document.createTemplateElement("BR", 89, "<BR>");

        $elem6.appendChild($elem7);

        

        $elem8 = document.createTemplateElement("BR", 90, "<BR>");

        $elem7.appendChild($elem8);

        

        $node9 = document.createTemplateTextNode(" ", 91, " ");

        $elem8.appendChild($node9);

        

        $elem9 = document.createTemplateElement("IMG", 92, "<IMG alt=\"Seating plan\" id=\"seatingPlanImg\" src=\"static/seatingplans/${performance.show.seatingPlanId}.jpg\">");

        $elem8.appendChild($elem9);

        

        $attr9 = document.createTemplateAttribute("alt", 93);

        $elem9.setAttributeNode($attr9);

        $node10 = document.createTemplateTextNode("Seating plan", 94, "Seating plan");

        $attr9.appendChild($node10);

        

        $attr9 = document.createTemplateAttribute("id", 95);

        $elem9.setAttributeNode($attr9);

        $node10 = document.createTemplateTextNode("seatingPlanImg", 96, "seatingPlanImg");

        $attr9.appendChild($node10);

        

        $attr9 = document.createTemplateAttribute("src", 97);

        $elem9.setAttributeNode($attr9);

        $node10 = document.createTemplateTextNode("static/seatingplans/${performance.show.seatingPlanId}.jpg", 98, "static/seatingplans/${performance.show.seatingPlanId}.jpg");

        $attr9.appendChild($node10);

        

        $node10 = document.createTemplateTextNode("", 99, "");

        $elem9.appendChild($node10);

        

        return document;

    }

    /**
     * Clone the document.
     */
    public Node cloneNode(boolean deep) {
        cloneDeepCheck(deep);

        return new ShowReservationXmlcObject(this);

    }

    /**
     * Get the XMLC DOM factory associated with the class.
     */
    protected final org.enhydra.xml.xmlc.dom.XMLCDomFactory getDomFactory() {
        return fDOMFactory;

    }

    /**
     * Get the element with id <CODE>bookingFee</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementBookingFee() {
        if (($element_BookingFee == null) && ($elementId_bookingFee >= 0)) {

            $element_BookingFee = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_bookingFee);

        }

        return $element_BookingFee;

    }

    /**
     * Get the element with id <CODE>date</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementDate() {
        if (($element_Date == null) && ($elementId_date >= 0)) {

            $element_Date = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_date);

        }

        return $element_Date;

    }

    /**
     * Get the element with id <CODE>minutesHeld</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementMinutesHeld() {
        if (($element_MinutesHeld == null) && ($elementId_minutesHeld >= 0)) {

            $element_MinutesHeld = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_minutesHeld);

        }

        return $element_MinutesHeld;

    }

    /**
     * Get the element with id <CODE>nonAdjacentWarning</CODE>.
     * @see org.w3c.dom.html.HTMLDivElement
     */
    public org.w3c.dom.html.HTMLDivElement getElementNonAdjacentWarning() {
        if (($element_NonAdjacentWarning == null) && ($elementId_nonAdjacentWarning >= 0)) {

            $element_NonAdjacentWarning = (org.enhydra.xml.lazydom.html.HTMLDivElementImpl)fLazyDocument.getNodeById($elementId_nonAdjacentWarning);

        }

        return $element_NonAdjacentWarning;

    }

    /**
     * Get the element with id <CODE>performanceName</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementPerformanceName() {
        if (($element_PerformanceName == null) && ($elementId_performanceName >= 0)) {

            $element_PerformanceName = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_performanceName);

        }

        return $element_PerformanceName;

    }

    /**
     * Get the element with id <CODE>seat</CODE>.
     * @see org.w3c.dom.html.HTMLLIElement
     */
    public org.w3c.dom.html.HTMLLIElement getElementSeat() {
        if (($element_Seat == null) && ($elementId_seat >= 0)) {

            $element_Seat = (org.enhydra.xml.lazydom.html.HTMLLIElementImpl)fLazyDocument.getNodeById($elementId_seat);

        }

        return $element_Seat;

    }

    /**
     * Get the element with id <CODE>seatType</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementSeatType() {
        if (($element_SeatType == null) && ($elementId_seatType >= 0)) {

            $element_SeatType = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_seatType);

        }

        return $element_SeatType;

    }

    /**
     * Get the element with id <CODE>seatingPlanImg</CODE>.
     * @see org.w3c.dom.html.HTMLImageElement
     */
    public org.w3c.dom.html.HTMLImageElement getElementSeatingPlanImg() {
        if (($element_SeatingPlanImg == null) && ($elementId_seatingPlanImg >= 0)) {

            $element_SeatingPlanImg = (org.enhydra.xml.lazydom.html.HTMLImageElementImpl)fLazyDocument.getNodeById($elementId_seatingPlanImg);

        }

        return $element_SeatingPlanImg;

    }

    /**
     * Get the element with id <CODE>seatsRequested</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementSeatsRequested() {
        if (($element_SeatsRequested == null) && ($elementId_seatsRequested >= 0)) {

            $element_SeatsRequested = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_seatsRequested);

        }

        return $element_SeatsRequested;

    }

    /**
     * Get the element with id <CODE>time</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementTime() {
        if (($element_Time == null) && ($elementId_time >= 0)) {

            $element_Time = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_time);

        }

        return $element_Time;

    }

    /**
     * Get the element with id <CODE>title</CODE>.
     * @see org.w3c.dom.html.HTMLTitleElement
     */
    public org.w3c.dom.html.HTMLTitleElement getElementTitle() {
        if (($element_Title == null) && ($elementId_title >= 0)) {

            $element_Title = (org.enhydra.xml.lazydom.html.HTMLTitleElementImpl)fLazyDocument.getNodeById($elementId_title);

        }

        return $element_Title;

    }

    /**
     * Get the element with id <CODE>totalPrice</CODE>.
     * @see org.w3c.dom.html.HTMLElement
     */
    public org.w3c.dom.html.HTMLElement getElementTotalPrice() {
        if (($element_TotalPrice == null) && ($elementId_totalPrice >= 0)) {

            $element_TotalPrice = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_totalPrice);

        }

        return $element_TotalPrice;

    }

    /**
     * Get the value of text child of element <CODE>bookingFee</CODE>.
     * @see Text
     */
    public void setTextBookingFee(String text) {
        if (($element_BookingFee == null) && ($elementId_bookingFee >= 0)) {

            $element_BookingFee = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_bookingFee);

        }

        doSetText($element_BookingFee, text);

    }

    /**
     * Get the value of text child of element <CODE>date</CODE>.
     * @see Text
     */
    public void setTextDate(String text) {
        if (($element_Date == null) && ($elementId_date >= 0)) {

            $element_Date = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_date);

        }

        doSetText($element_Date, text);

    }

    /**
     * Get the value of text child of element <CODE>minutesHeld</CODE>.
     * @see Text
     */
    public void setTextMinutesHeld(String text) {
        if (($element_MinutesHeld == null) && ($elementId_minutesHeld >= 0)) {

            $element_MinutesHeld = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_minutesHeld);

        }

        doSetText($element_MinutesHeld, text);

    }

    /**
     * Get the value of text child of element <CODE>nonAdjacentWarning</CODE>.
     * @see Text
     */
    public void setTextNonAdjacentWarning(String text) {
        if (($element_NonAdjacentWarning == null) && ($elementId_nonAdjacentWarning >= 0)) {

            $element_NonAdjacentWarning = (org.enhydra.xml.lazydom.html.HTMLDivElementImpl)fLazyDocument.getNodeById($elementId_nonAdjacentWarning);

        }

        doSetText($element_NonAdjacentWarning, text);

    }

    /**
     * Get the value of text child of element <CODE>performanceName</CODE>.
     * @see Text
     */
    public void setTextPerformanceName(String text) {
        if (($element_PerformanceName == null) && ($elementId_performanceName >= 0)) {

            $element_PerformanceName = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_performanceName);

        }

        doSetText($element_PerformanceName, text);

    }

    /**
     * Get the value of text child of element <CODE>seat</CODE>.
     * @see Text
     */
    public void setTextSeat(String text) {
        if (($element_Seat == null) && ($elementId_seat >= 0)) {

            $element_Seat = (org.enhydra.xml.lazydom.html.HTMLLIElementImpl)fLazyDocument.getNodeById($elementId_seat);

        }

        doSetText($element_Seat, text);

    }

    /**
     * Get the value of text child of element <CODE>seatType</CODE>.
     * @see Text
     */
    public void setTextSeatType(String text) {
        if (($element_SeatType == null) && ($elementId_seatType >= 0)) {

            $element_SeatType = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_seatType);

        }

        doSetText($element_SeatType, text);

    }

    /**
     * Get the value of text child of element <CODE>seatsRequested</CODE>.
     * @see Text
     */
    public void setTextSeatsRequested(String text) {
        if (($element_SeatsRequested == null) && ($elementId_seatsRequested >= 0)) {

            $element_SeatsRequested = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_seatsRequested);

        }

        doSetText($element_SeatsRequested, text);

    }

    /**
     * Get the value of text child of element <CODE>time</CODE>.
     * @see Text
     */
    public void setTextTime(String text) {
        if (($element_Time == null) && ($elementId_time >= 0)) {

            $element_Time = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_time);

        }

        doSetText($element_Time, text);

    }

    /**
     * Get the value of text child of element <CODE>title</CODE>.
     * @see Text
     */
    public void setTextTitle(String text) {
        if (($element_Title == null) && ($elementId_title >= 0)) {

            $element_Title = (org.enhydra.xml.lazydom.html.HTMLTitleElementImpl)fLazyDocument.getNodeById($elementId_title);

        }

        doSetText($element_Title, text);

    }

    /**
     * Get the value of text child of element <CODE>totalPrice</CODE>.
     * @see Text
     */
    public void setTextTotalPrice(String text) {
        if (($element_TotalPrice == null) && ($elementId_totalPrice >= 0)) {

            $element_TotalPrice = (org.enhydra.xml.lazydom.html.LazyHTMLElement)fLazyDocument.getNodeById($elementId_totalPrice);

        }

        doSetText($element_TotalPrice, text);

    }

    /**
     * Recursive function to do set access method fields from the DOM.
     * Missing ids have fields set to null.
     */
    protected void syncWithDocument(Node node) {
        if (node instanceof Element) {

            String id = ((Element)node).getAttribute("id");

            if (id.length() == 0) {

            } else if (id.equals("bookingFee")) {

                $elementId_bookingFee = 53;

                $element_BookingFee = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("date")) {

                $elementId_date = 14;

                $element_Date = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("minutesHeld")) {

                $elementId_minutesHeld = 36;

                $element_MinutesHeld = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("nonAdjacentWarning")) {

                $elementId_nonAdjacentWarning = 59;

                $element_NonAdjacentWarning = (org.enhydra.xml.lazydom.html.HTMLDivElementImpl)node;

            } else if (id.equals("performanceName")) {

                $elementId_performanceName = 9;

                $element_PerformanceName = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("seat")) {

                $elementId_seat = 42;

                $element_Seat = (org.enhydra.xml.lazydom.html.HTMLLIElementImpl)node;

            } else if (id.equals("seatType")) {

                $elementId_seatType = 31;

                $element_SeatType = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("seatingPlanImg")) {

                $elementId_seatingPlanImg = 92;

                $element_SeatingPlanImg = (org.enhydra.xml.lazydom.html.HTMLImageElementImpl)node;

            } else if (id.equals("seatsRequested")) {

                $elementId_seatsRequested = 26;

                $element_SeatsRequested = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("time")) {

                $elementId_time = 19;

                $element_Time = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            } else if (id.equals("title")) {

                $elementId_title = 3;

                $element_Title = (org.enhydra.xml.lazydom.html.HTMLTitleElementImpl)node;

            } else if (id.equals("totalPrice")) {

                $elementId_totalPrice = 48;

                $element_TotalPrice = (org.enhydra.xml.lazydom.html.LazyHTMLElement)node;

            }

        }

        Node child = node.getFirstChild();

        while (child != null) {

            syncWithDocument(child);

            child = child.getNextSibling();

        }

    }

}
