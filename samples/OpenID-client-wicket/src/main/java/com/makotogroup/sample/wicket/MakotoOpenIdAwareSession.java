/** BEGIN COPYRIGHT
 *
 * Copyright(c) 2007-09, Makoto Consulting Group, Inc.
 * All rights reserved.
 *
 * WARNING: This file contains CONFIDENTIAL and PROPRIETARY information and
 * INTELLECTUAL PROPERTY of Makoto Consulting Group, Inc., and is protected by
 * copyright law and international treaties. Unauthorized reproduction or
 * distribution may result in severe civil and criminal penalties, and will
 * be prosecuted to the maximum extent possible under the law.
 *
 * END COPYRIGHT */
package com.makotogroup.sample.wicket;

import org.apache.log4j.Logger;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.openid4java.discovery.DiscoveryInformation;

/**
 * Shows how to write a WebSession class (very easy, btw). This particular
 * subclass has a method to store a DiscoveryInformation object in session
 * (optionally storing it in this class instance or actually in the User's
 * Session).
 *  
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 */
public class MakotoOpenIdAwareSession extends WebSession {
	
	private DiscoveryInformation discoveryInformation;
	private boolean discoveryInformationStoredInSession;
	public static final String DISCOVERY_INFORMATION = "openid-disc";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6377515121943369723L;
	
	public MakotoOpenIdAwareSession(Request request) {
		super(request);
		log.info("New Session created...");
	}
	
	public void setDiscoveryInformation(DiscoveryInformation discoveryInformation) {
		setDiscoveryInformation(discoveryInformation, false);
	}
	public void setDiscoveryInformation(DiscoveryInformation discoveryInformation, boolean storeInSession) {
		this.discoveryInformation = discoveryInformation;
		if (storeInSession) {
			setAttribute(DISCOVERY_INFORMATION, discoveryInformation);
			discoveryInformationStoredInSession = true;
		}
	}
	public DiscoveryInformation getDiscoveryInformation() {
		DiscoveryInformation ret = discoveryInformation;
		if (discoveryInformationStoredInSession) {
			ret = (DiscoveryInformation)getAttribute(DISCOVERY_INFORMATION);
		}
		return ret;
	}

	private static final Logger log = Logger.getLogger(MakotoOpenIdAwareSession.class);
}
