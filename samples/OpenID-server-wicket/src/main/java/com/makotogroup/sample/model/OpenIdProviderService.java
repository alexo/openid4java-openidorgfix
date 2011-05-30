package com.makotogroup.sample.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.RequestUtils;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

import com.makotogroup.sample.util.XrdsDocumentBuilder;
import com.makotogroup.sample.wicket.OpenIdLoginPage;

/**
 * This class contains the logic to use openid4java to create a simple
 * OpenID Provider, or OP. The logic is encapsulated here in case you want
 * to use, say, a servlet to front your OP. I like Wicket, so I use that as
 * my web client. I've tried as much as possible to separate the two, with
 * the goal being that you can take the code here and use it in your projects
 * regardless of your Java-based web architecture (Struts, JSF, Servlets, etc).
 *
 * Some of the  URLs are hardcoded and work for a local deployment
 * so you can get the code up and running and play around with it. I have tried
 * to note where you will need to change the URLs to match your particular
 * deployment scenario.
 *
 * @author J Steven Perry
 * @author Makoto Consulting Group, Inc.
 *
 */
public class OpenIdProviderService {
  private static final Logger log = Logger.getLogger(OpenIdProviderService.class);

  private static ServerManager serverManager;
  public static ServerManager getServerManager() {
    log.trace("geterverManager() BEGIN...");
    if (serverManager == null) {
      serverManager = new ServerManager();
      serverManager.setOPEndpointUrl(getOpEndpointUrl());
      serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
      serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
    }
    log.debug("Returning ServerManager =>" + serverManager.toString() + "<=");
    log.trace("geterverManager() END...");
    return serverManager;
  }

  /**
   * This is your OP endpoint. Change the URL that is returned below to match
   * your deployment scenario.
   */
  public static String getOpEndpointUrl() {
    log.trace("getOpEndpointUrl() BEGIN...");
    log.trace("getOpEndpointUrl() END...");
    return RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(OpenIdLoginPage.class, PageParameters.NULL).toString());
  }

  /**
   *
   * @param response
   * @param request
   * @throws IOException
   */
  public static void processAssociationRequest(final Response response, final ParameterList request) throws IOException {
    log.trace("processAssociationRequest() BEGIN...");
    final Message message = getServerManager().associationResponse(request);
    sendPlainTextResponse(response, message);
    log.trace("processAssociationRequest() END...");
  }

  /**
   * This is a helpful method to enable if you want to see what is being
   * sent across. Disable this in production.
   *
   * @param request
   */
  @SuppressWarnings("unchecked")
  public static void logRequestParameters(final ParameterList request) {
    log.trace("logRequestParameters() BEGIN...");
    if (log.isDebugEnabled()) {
      log.debug("Dumping request parameters:");
      final List<Parameter> paramList = request.getParameters();
      for (final Parameter parameter : paramList) {
        log.debug(parameter.getKey() + ":" + parameter.getValue());
      }
    }
    log.trace("logRequestParameters() END...");
  }

  public static void sendDiscoveryResponse(final Response response) throws IOException {
    log.trace("sendDiscoveryResponse() BEGIN...");
    //
    response.setContentType("application/xrds+xml");
    final OutputStream outputStream = response.getOutputStream();
    final String xrdsResponse = OpenIdProviderService.createXrdsResponse();
    //
    if (log.isDebugEnabled()) {
      log.debug("Sending XRDS response:");
      log.debug(xrdsResponse);
    }
    outputStream.write(xrdsResponse.getBytes());
    outputStream.close();
    log.trace("sendDiscoveryResponse() END...");
  }

  /**
   * TODO: document this
   * @return
   */
  public static String createXrdsResponse() {
    log.trace("createXrdsResponse() BEGIN...");
    final XrdsDocumentBuilder documentBuilder = new XrdsDocumentBuilder();
    documentBuilder.addServiceElement("http://specs.openid.net/auth/2.0/server", OpenIdProviderService.getOpEndpointUrl(), "10");
    documentBuilder.addServiceElement("http://specs.openid.net/auth/2.0/signon", OpenIdProviderService.getOpEndpointUrl(), "20");
    documentBuilder.addServiceElement(AxMessage.OPENID_NS_AX, OpenIdProviderService.getOpEndpointUrl(), "30");
    documentBuilder.addServiceElement(SRegMessage.OPENID_NS_SREG, OpenIdProviderService.getOpEndpointUrl(), "40");
    log.trace("createXrdsResponse() BEGIN...");
    return documentBuilder.toXmlString();
  }

  public static Message createAuthResponse(final ParameterList requestParameters, final String userSelectedId, final String userSelectedClaimedId,
      final boolean authenticatedAndApproved) {
    log.trace("createAuthResponse() BEGIN...");
    log.debug("Creating Auth Response. UserSelectedId =>" + userSelectedId + "<= userSelectedClaimedId =>" + userSelectedClaimedId + "<=");
    final Message authResponse = getServerManager().authResponse(requestParameters, userSelectedId, userSelectedClaimedId, authenticatedAndApproved);
    log.trace("createAuthResponse() END...");
    return authResponse;
  }

  private static void sendPlainTextResponse(final Response response, final Message message) throws IOException {
    log.trace("sendPlainTextResponse() BEGIN...");
    response.setContentType("text/plain");
    final OutputStream os = response.getOutputStream();
    os.write(message.keyValueFormEncoding().getBytes());
    os.close();
    log.trace("sendPlainTextResponse() END...");
  }

  /**
   * This is where the bulk of the action happens. Once the handshaking is
   * done and the OP is ready to send the response back to the requester, this
   * method is used to pack it all up and get it ready to ship back.
   *
   * See comments below for more details.
   *
   * @param requestParameters
   * @param userSelectedId
   * @param userSelectedClaimedId
   * @param registrationModel
   * @return
   */
  public static Message buildAuthResponse(final ParameterList requestParameters, final String userSelectedId, final String userSelectedClaimedId, final RegistrationModel registrationModel) {
    log.trace("buildAuthResponse() BEGIN...");
    final Message authResponse = OpenIdProviderService.createAuthResponse(
        requestParameters,
        userSelectedId,
        userSelectedClaimedId,
        true // authenticated... yep
        );
    // Check for and process any extensions the RP has asked for
    AuthRequest authRequest = null;
    try {
      authRequest = AuthRequest.createAuthRequest(requestParameters, getServerManager().getRealmVerifier());
      // Process all of the extensions we care about...
      // Simple Registration
      if (authRequest.hasExtension(SRegMessage.OPENID_NS_SREG)) {
        log.debug("Processing Simple Registration Extension request...");
        final MessageExtension extensionRequestObject = authRequest.getExtension(SRegMessage.OPENID_NS_SREG);
        if (extensionRequestObject instanceof SRegRequest) {
          final SRegRequest sRegRequest = (SRegRequest)extensionRequestObject;
          // Send back everything we have (which you'll notice is not everything)
          /// required or not. We're friendly...
          final Map<String, String> registrationData = new HashMap<String, String>();
          registrationData.put("email", registrationModel.getEmailAddress());
          registrationData.put("fullname", registrationModel.getFullName());
          registrationData.put("dob", Long.toString(registrationModel.getDateOfBirth().getTime()));
          registrationData.put("postcode", registrationModel.getZipCode());
          final SRegResponse sRegResponse = SRegResponse.createSRegResponse(sRegRequest, registrationData);
          // Add the information to the AuthResponse message
          authResponse.addExtension(sRegResponse);
        } else {
          log.error("Cannot continue processing Simple Registration Extension. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + SRegRequest.class.getName() + " as expected.");
        }
      }
      if (authRequest.hasExtension(AxMessage.OPENID_NS_AX)) {
        log.debug("Processing Attribute Exchange request...");
        final MessageExtension extensionRequestObject = authRequest.getExtension(AxMessage.OPENID_NS_AX);
        FetchResponse fetchResponse = null;
        final Map<String, String> axData = new HashMap<String, String>();
        if (extensionRequestObject instanceof FetchRequest) {
          final FetchRequest axRequest = (FetchRequest)extensionRequestObject;
          final ParameterList parameters = axRequest.getParameters();
          fetchResponse = FetchResponse.createFetchResponse(axRequest, axData);
          if (parameters.hasParameter("type.favoriteColor")) {
            axData.put("favoriteColor", registrationModel.getFavoriteColor());
            fetchResponse.addAttribute("favoriteColor", "http://makotogroup.com/schema/1.0/favoriteColor", registrationModel.getFavoriteColor());
          }
          authResponse.addExtension(fetchResponse);
        } else {
          log.error("Cannot continue processing Attribute Exchange (AX) request. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + AxMessage.class.getName() + " as expected.");
        }
      }
      getServerManager().sign((AuthSuccess)authResponse);
      // TODO: Add Custom extension where we send back Favorite Color
    } catch (final Exception e) {
      log.error("Error occurred creating AuthRequest object:", e);
    }
    log.trace("buildAuthResponse() End...");
    return authResponse;
  }

}
