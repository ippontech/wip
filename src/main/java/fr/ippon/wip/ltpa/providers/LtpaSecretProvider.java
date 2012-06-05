package fr.ippon.wip.ltpa.providers;

import javax.portlet.PortletRequest;

/**
 * The interface LtpaSecretProvider must be implemented
 * by the external class that supply the LTPA secret.
 */
public interface LtpaSecretProvider {

    /**
     * This method returns a tuple containing the domain name and
     * the LTPA secret. The "request" parameter is used to send
     * data needed by the implementing method (ex: a domain name)
     *
     * @param request PortletRequest object to transmit data
     * @return a String tuple {domain, ltpaSecret}
     */
    public String[] getLtpaSecret(PortletRequest request);

}
