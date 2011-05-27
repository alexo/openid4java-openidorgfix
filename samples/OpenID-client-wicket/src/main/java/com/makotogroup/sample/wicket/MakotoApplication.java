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

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.PackageName;
/**
 * Quick and dirty little class that serves as the requisite Application
 * class for this Wicket application.
 * 
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 */
public class MakotoApplication extends WebApplication {
	
	public MakotoApplication() {
		super();
	}

  public Session newSession(Request request, Response response) {
		return new MakotoOpenIdAwareSession(request);
	}
  
  @Override
  public void init() {
  	super.init();
  	//
  	// Mount the sample package. It makes the URLs so much cleaner-looking
  	//
  	mount("sample", PackageName.forClass(OpenIdRegistrationPage.class));
  }

  /**
   * Return the "Home" page used by the application. Wicket will redirect
   * here if you don't explicitly supply a Page destination.
   */
	public Class<? extends WebPage> getHomePage() {
		return OpenIdRegistrationPage.class;
	}
	
}
  
