package com.makotogroup.sample.util;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Use this class to build a XRDS document in an object oriented way.
 * Instantiate the class, add one or more service elements, then call
 * toXmlString() to get a String representation of the document (for
 * sending back to the caller).
 *
 * @author J Steven Perry
 * @author Makoto Consulting Group, Inc.
 * @author Oscar Pearce - many thanks to Oscar for the guts of the code
 *  you see below to build the proper XRDS response document.
 *
 */
public class XrdsDocumentBuilder {

  private static final Logger log = Logger.getLogger(XrdsDocumentBuilder.class);

  private final Element baseElement;

  private static final String MASTER_XRDS_NAMESPACE = "xri://$xrd*($v*2.0)";

  public XrdsDocumentBuilder() {
    final Namespace xrdNS = Namespace.getNamespace(MASTER_XRDS_NAMESPACE);
    baseElement = new Element("XRD", xrdNS);
  }

  public void addServiceElement(final String type, final String uri, final String priority) {
    addServiceElement(type, uri, priority, null);
  }

  private void addServiceElement(final String type, final String uri, final String priority, final String delegate) {
    log.trace("addServiceElement() BEGIN...");
    log.debug("Creating service element of type '" + type + "' for uri '" + uri + "' of priority '" + priority + "' with delegate '" + delegate + "'");
    final Namespace xrdNS = Namespace.getNamespace(MASTER_XRDS_NAMESPACE);
    final Namespace openidNS = Namespace.getNamespace("openid", "http://openid.net/xmlns/1.0");
    final Element serviceElement = new Element("Service", xrdNS);
    final Element typeElement = new Element("Type", xrdNS);
    typeElement.addContent(type);
    final Element uriElement = new Element("URI", xrdNS);
    uriElement.addContent(uri);
    serviceElement.addContent(typeElement);
    serviceElement.addContent(uriElement);
    if (StringUtils.isNotEmpty(delegate)) {
      final Element delegateElement = new Element("Delegate", openidNS);
      delegateElement.addContent(delegate);
      serviceElement.addContent(delegateElement);
    }
    if (StringUtils.isNotEmpty(priority)) {
      serviceElement.setAttribute("priority", priority);
    }
    baseElement.addContent(serviceElement);
    log.trace("addServiceElement() END...");
  }

  public String toXmlString() {
    log.trace("toXmlString() BEGIN...");
    final Namespace xrdsNS = Namespace.getNamespace("xrds", "xri://$xrds");
    final Element rootElement = new Element("XRDS", xrdsNS);
    rootElement.addContent(baseElement);
    final Document doc = new Document(rootElement);
    final StringWriter w = new StringWriter();
    final XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
    try {
      o.output(doc, w);
      w.close();
    }
    catch(final IOException e) {
      log.warn("Caught an IOException while writing to StringWriter! This can't be happening!", e);
    }
    log.debug("XRD Response = " + w.toString());
    log.trace("toXmlString() END...");
    return w.toString();
  }

}
