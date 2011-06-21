package fr.ippon.wip.ltpa.providers;

import javax.portlet.PortletRequest;

/**
 * The interface CredentialProvider must be implemented 
 * by the external class that supply the credential.
 */
public interface CredentialProvider {

	/**
	 * This method returns the user credential. The "request" parameter 
	 * is used to send data needed by the implementing method (ex: a user ID)
	 * @param request PortletRequest object to transmit data
	 * @return a String tuple {domain, ltpaSecret} 
	 */
	public String getCredentials(PortletRequest request);
	
}
