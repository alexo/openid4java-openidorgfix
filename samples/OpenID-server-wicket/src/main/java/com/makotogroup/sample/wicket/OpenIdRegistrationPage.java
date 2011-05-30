package com.makotogroup.sample.wicket;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;

import com.makotogroup.sample.model.OpenIdProviderService;
import com.makotogroup.sample.model.RegistrationModel;
import com.makotogroup.sample.model.RegistrationService;

/**
 * This class represents the OpenIdRegistrationSavePage, which
 * receives the authentication response from the OpenID Provider (OP)
 * and verifies the response with openid4java. It also provides a way to save
 * the information retrieved from the OP somewhere (well, a hook for that has
 * been provided).
 *
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 *
 */
public class OpenIdRegistrationPage extends WebPage {
  private static final Logger log = Logger.getLogger(OpenIdRegistrationPage.class);
  /**
   * Default Constructor
   */
  public OpenIdRegistrationPage() {
    this(new PageParameters());
    log.trace("OpenIdRegistrationPage() BEGIN...");
    log.trace("OpenIdRegistrationPage() END...");
  }


  public static PageParameters createParams(final boolean isReturn) {
    final PageParameters params = new PageParameters();
    params.add("is_return", String.valueOf(isReturn));
    return params;
  }

  /**
   * Constructor called by Wicket with an auth response (since the response
   * has parameters associated with it... LOTS of them!). And, by the way,
   * the auth response is the Request for this classl (not to be confusing).
   *
   * @param pageParameters The request parameters (which are the response
   *  parameters from the OP).
   */
  public OpenIdRegistrationPage(final PageParameters pageParameters) {
    log.trace("OpenIdRegistrationPage(constructor) BEGIN...");
    RegistrationModel registrationModel = new RegistrationModel();
    if (!pageParameters.isEmpty()) {
      //
      // If this is a return trip (the OP will redirect here once authentication
      /// is compelete), then verify the response. If it looks good, send the
      /// user to the RegistrationSuccessPage. Otherwise, display a message.
      //
      final String isReturn = pageParameters.getString("is_return");
      if (isReturn.equals("true")) {
        //
        // Grab the session object so we can let openid4java do verification.
        //
        final MakotoOpenIdAwareSession session = (MakotoOpenIdAwareSession)getSession();
        final DiscoveryInformation discoveryInformation = session.getDiscoveryInformation();
        //
        // Delegate to the Service object to do verification. It will return
        /// the RegistrationModel to use to display the information that was
        /// retrieved from the OP about the User-Supplied identifier. The
        /// RegistrationModel reference will be null if there was a problem
        /// (check the logs for more information if this happens).
        //
        registrationModel = RegistrationService.processReturn(discoveryInformation, pageParameters, RegistrationService.getReturnToUrl());
        if (registrationModel == null) {
          //
          // Oops, something went wrong. Display a message on the screen.
          /// Check the logs for more information.
          //
          error("Open ID Confirmation Failed. No information was retrieved from the OpenID Provider. You will have to enter all information by hand into the text fields provided.");
        }
      }
    } else {
      log.debug("Received a request for this resource. It is protected, so we need to check with the OP...");
      // Need to authenticate the user before proceeding
      final DiscoveryInformation discoveryInformation = RegistrationService.performDiscoveryOnUserSuppliedIdentifier(OpenIdProviderService.getOpEndpointUrl());
      // Store the disovery results in session.
      final MakotoOpenIdAwareSession session = (MakotoOpenIdAwareSession)getSession();
      session.setDiscoveryInformation(discoveryInformation, true);
      // Create the AuthRequest
      final AuthRequest authRequest = RegistrationService.createOpenIdAuthRequest(discoveryInformation, RegistrationService.getReturnToUrl());
      // Now take the AuthRequest and forward it on to the OP
      getRequestCycle().setRedirect(false);
      getResponse().redirect(authRequest.getDestinationUrl(true));
    }
    add(new OpenIdRegistrationInformationDisplayForm("form", registrationModel));
    log.trace("OpenIdRegistrationPage(constructor) END...");
  }

  /**
   * The Form subclass for this page.
   *
   * @author J Steven Perry
   * @author http://makotoconsulting.com
   */
	public static class OpenIdRegistrationInformationDisplayForm extends Form {
    /**
     *
     */
    private static final long serialVersionUID = -1045594133856989168L;

    /**
     * Constructor, takes the wicket:id value (probably "form") and the
     * RegistrationModel object to be used as the model for the form.
     *
     * @param id
     * @param registrationModel
     */
    public OpenIdRegistrationInformationDisplayForm(final String id, final RegistrationModel registrationModel) {
      super(id, new CompoundPropertyModel(registrationModel));
      log.trace("OpenIdRegistrationInformationDisplayForm() BEGIN...");
      //
      final TextField openId = new TextField("openId");
      openId.setEnabled(false);
      add(openId);
      //
      final TextField fullName = new RequiredTextField("fullName");
      add(fullName);
      //
      final TextField emailAddress = new RequiredTextField("emailAddress");
      add(emailAddress);
      //
      final TextField zipCode = new TextField("zipCode");
      add(zipCode);
      //
      final TextField dateOfBirth = new RequiredTextField("dateOfBirth", Date.class);
      add(dateOfBirth);
      //
      final TextField favoriteColor = new TextField("favoriteColor");
      add(favoriteColor);
      // If you want to save the registration information to your application
      /// database (something an RP would want to do, I guess), use code like
      /// that you see below.
//      Button saveButton = new Button("saveButton") {
//        public void onSubmit() {
//          // Store registration in the DB
//          if (saveRegistrationInfo()) {
//            info("Registration Info saved.");
//          } else {
//            error("Registration Info could not be saved!");
//          }
//        }
//      };
//      add(saveButton);
      log.trace("OpenIdRegistrationInformationDisplayForm() END...");
    }
//    /**
//     * This is a hook where you would place code to save the registration
//     * information.
//     *
//     * @return returns true if the information was successfully saved, false
//     *  otherwise.
//     */
//    private boolean saveRegistrationInfo() {
//      // TODO: Fill in implementation to save code to the DB
//      log.trace("saveRegistrationInfo() BEGIN...");
//      log.trace("saveRegistrationInfo() END...");
//      return true;
//    }
  }
}
