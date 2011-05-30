package com.makotogroup.sample.wicket;

import org.apache.log4j.Logger;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.PageLink;

/**
 * This class represents a page that you might place one or more protected
 * resources onto and secure them using OpenID.
 * 
 * @author J Steven Perry
 * @author Makoto Consulting Group, Inc.
 *
 */
@SuppressWarnings("serial")
public class OleMainPage extends WebPage {
  private static final Logger log = Logger.getLogger(OleMainPage.class);
  
  private static final long serialVersionUID = 1L;
  public OleMainPage() {
    add(new OleMainForm("form"));
  }
  
  public class OleMainForm extends Form {
    private static final long serialVersionUID = 1L;

    public OleMainForm(String id) {
      super(id);
      log.trace("OleMainForm (constructor) BEGIN...");
      log.debug("Creating OleMainForm using id =>" + id + "<=");
      // This is the protected resource - the registration page.
      /// Yeah, lame, I know, but should serve to give you a sense
      /// of what this is all about.
      add(new PageLink("openIdRegistrationPage", new IPageLink() {
        public Page getPage() {
          return new OpenIdRegistrationPage();
        }
        public Class<? extends WebPage> getPageIdentity() {
          return OpenIdRegistrationPage.class;
        }
      }));
      log.trace("OleMainForm (constructor) END...");
    }
  }
}
