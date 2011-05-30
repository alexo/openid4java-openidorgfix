package com.makotogroup.sample.wicket;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.joda.time.YearMonthDay;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;

import com.makotogroup.sample.model.OpenIdProviderService;
import com.makotogroup.sample.model.RegistrationModel;

/**
 * This page represents the entry point on the server for handling requests
 * for the OP. You could also use a servlet to handle this, but there is
 * already an example of that on the openid4java website at googlecode.
 * I like Wicket, so I decided to use it for this purpose.
 * 
 * @author J Steven Perry
 * @author Makoto Consulting Group, Inc.
 *
 */
@SuppressWarnings("serial")
public class OpenIdLoginPage extends WebPage {
  private static final long serialVersionUID = 1L;
  
  private static final Logger log = Logger.getLogger(OpenIdLoginPage.class);

  public OpenIdLoginPage() throws IOException {
    this(new PageParameters());
  }
  
  public OpenIdLoginPage(PageParameters parameters) throws IOException {
    super(parameters);
    log.trace("OpenIdLoginPage(constructor) BEGIN...");
    //
    if (parameters.isEmpty()) {
      // Empty request. Assume discovery request...
      log.debug("Processing empty request. Assuming discovery request...");
      OpenIdProviderService.sendDiscoveryResponse(getResponse());
    } else {
      requestParameters = new ParameterList(parameters);
      OpenIdProviderService.logRequestParameters(requestParameters);
      String mode = requestParameters.hasParameter("openid.mode") ? requestParameters.getParameterValue("openid.mode") : null;
      log.info("Processing OpenID request '" + mode + "'...");
      // Save off the return_to value so when the user logs in successfully,
      /// we can redirect the browser there...
      // Crack the Request mode and process it accordingly...
      if ("associate".equals(mode)) {
        OpenIdProviderService.processAssociationRequest(getResponse(), requestParameters);
      }
      else if ("checkid_immediate".equals(mode)
    		  ||
    		   "checkid_setup".equals(mode)
    		  ||
    		   "check_authentication".equals(mode)) {
        // Check Session. If information is there, we're done. No need to login again.
        if (((MakotoOpenIdAwareSession)getSession()).isLoggedIn()) {
          // Create AuthResponse from session variables...
          log.info("********************************");
          log.info("* User is already logged in... *");
          log.info("********************************");
          sendSuccessfulResponse();
        }
      }
      else {
        log.error("Unknown request mode '" + mode + "'... Forcing login...");
      }
    }
    add(new OpenIdLoginForm("form"));
    log.trace("OpenIdLoginPage(constructor) END...");
  }

  /**
   * Build an AuthResponse to send back to the requester.
   * Use the OpenIdProviderService to encapsulate this logic.
   */
  private void sendSuccessfulResponse() {
    log.trace("sendSuccessfulResponse() BEGIN...");
    MakotoOpenIdAwareSession session = (MakotoOpenIdAwareSession)getSession();
    Message authResponse = OpenIdProviderService.buildAuthResponse(
        requestParameters, 
        session.getUserSelectedId(),
        session.getUserSelectedClaimedId(),
        session.getRegistrationModel());
    getRequestCycle().setRedirect(false);// avoid the dreaded "Already redirecting" message...
    getResponse().redirect(authResponse.getDestinationUrl(true));
    log.trace("sendSuccessfulResponse() END...");
  }
  private ParameterList requestParameters;

  /**
   * Form to allow the user to login (if necessary)
   */
  public class OpenIdLoginForm extends Form {
    private static final long serialVersionUID = 1L;

    public OpenIdLoginForm(String id) {
      super(id);
      log.trace("OpenIdLoginForm(constructor) BEGIN...");
      add(new TextField("userId", new PropertyModel(this, "userId")));
      add(new PasswordTextField("password", new PropertyModel(this, "password")));
      add(new Button("loginButton") {
        public void onSubmit() {
          log.trace("onSubmit() BEGIN...");
          // Validate login.
          boolean authenticatedAndApproved = validateLogin(getUserId(), getPassword());
          //If invalid, display a message
          if (!authenticatedAndApproved) {
            error("Login failed!");
          } else {
            // Otherwise, create auth response and redirect
            String userSelectedId = mapUserIdToUserSelectedId(getUserId());
            String userSelectedClaimedId = mapSelectedIdToClaimedId(getUserId());
            RegistrationModel registrationModel = getRegistrationModel(userSelectedId);
            // Create the AuthResponse
            Message authResponse = OpenIdProviderService.buildAuthResponse(
                requestParameters, 
                userSelectedId, 
                userSelectedClaimedId, 
                registrationModel);
            MakotoOpenIdAwareSession session = (MakotoOpenIdAwareSession)getSession();
            session.setLoggedIn(true);
            session.setUserSelectedId(userSelectedId);
            session.setUserSelectedClaimedId(userSelectedClaimedId);
            session.setRegistrationModel(registrationModel);
            getRequestCycle().setRedirect(false);// avoid the dreaded "Already redirecting" message...
            getResponse().redirect(authResponse.getDestinationUrl(true));
          }
          log.trace("onSubmit() END...");
        }
      });
      log.trace("OpenIdLoginForm(constructor) END...");
    }
    
    private String userId;
    private String password;

    public String getUserId() {
      return userId;
    }
    public void setUserId(String userId) {
      this.userId = userId;
    }
    public String getPassword() {
      return password;
    }
    public void setPassword(String password) {
      this.password = password;
    }
    
    private static final String correctUserId = "steve";
    private static final String correctPassword = "asdf";// not very creative, huh?
    
    protected boolean validateLogin(String userId, String password) {
      log.trace("validateLogin() BEGIN...");
      // Ideally, you would read this from a DB, or LDAP, or somewhere,
      log.trace("validateLogin() END...");
      return correctUserId.equals(userId) && correctPassword.equals(password);
    }
    protected String mapUserIdToUserSelectedId(String userId) {
      log.trace("mapUserIdToUserSelectedId() BEGIN...");
      log.trace("mapUserIdToUserSelectedId() END...");
      return OpenIdProviderService.getOpEndpointUrl();
    }
    protected String mapSelectedIdToClaimedId(String userId) {
      log.trace("mapSelectedIdToClaimedId() BEGIN...");
      // Ideally, you would read this from a DB, or LDAP, or somewhere,
      /// but this is sample code... just return the userid passed in...
      log.trace("mapSelectedIdToClaimedId() END...");
      return OpenIdProviderService.getOpEndpointUrl();
    }
    /**
     * This method serves as the "database" for the sample app.
     */
    protected RegistrationModel getRegistrationModel(String userSelectedId) {
      log.trace("getRegistrationModel() BEGIN...");
      // Ideally, you would read this from a DB. Hardcode for the sample app...
      RegistrationModel ret = new RegistrationModel();
      ret.setDateOfBirth(new YearMonthDay(1967, 9, 10).toDateMidnight().toDate());
      ret.setEmailAddress("steve@makotoconsulting.com");
      ret.setFavoriteColor("blue");
      ret.setFullName("James Steven Perry");
      ret.setOpenId(OpenIdProviderService.getOpEndpointUrl());
      ret.setZipCode("72207");
      log.trace("getRegistrationModel() END...");
      return ret;
    }

  }

}
