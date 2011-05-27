package com.makotogroup.sample.wicket;

import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openid4java.discovery.DiscoveryInformation;

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
public class OpenIdRegistrationSavePage extends WebPage {
  /**
   * Default Constructor
   */
  public OpenIdRegistrationSavePage() {
    this(new PageParameters());
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
  public OpenIdRegistrationSavePage(final PageParameters pageParameters) {
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
    }
    add(new OpenIdRegistrationInformationDisplayForm("form", registrationModel));
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
    @SuppressWarnings("serial")
    public OpenIdRegistrationInformationDisplayForm(final String id, final RegistrationModel registrationModel) {
      super(id, new CompoundPropertyModel(registrationModel));
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
      final Button saveButton = new Button("saveButton") {
        public void onSubmit() {
          // Store registration in the DB
          if (saveRegistrationInfo()) {
            info("Registration Info saved.");
          } else {
            error("Registration Info could not be saved!");
          }
        }
      };
      add(saveButton);
    }
    /**
     * This is a hook where you would place code to save the registration
     * information.
     *
     * @return returns true if the information was successfully saved, false
     *  otherwise.
     */
    private boolean saveRegistrationInfo() {
      // TODO: Fill in implementation to save code to the DB
      return true;
    }
  }
}
