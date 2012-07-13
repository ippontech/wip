/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.transformers;

import fr.ippon.wip.http.UrlFactory;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;

import java.io.IOException;

/**
 * This transformer class must not be used when processing HTTP request/response. It transforms
 * Response content processed in ACTION phase (with temporary URL) for printing in
 * RENDER phase (with portal URL)
 */
public class ActionToRenderTransformer extends AbstractTransformer {
    private final MimeResponse mimeResponse;

    public ActionToRenderTransformer(PortletRequest portletRequest, MimeResponse mimeResponse, String actualUrl) {
        super(portletRequest, actualUrl);
        this.mimeResponse = mimeResponse;
    }

    @Override
    public String transform(String input) throws IOException {
        int startIndex;
        int endIndex;
        int previousLast = 0;
        StringBuilder content = new StringBuilder();
        while ((startIndex = input.indexOf(UrlFactory.TEMP_URL_ENCODED_SEPARATOR, previousLast)) > 0) {
            endIndex = input.indexOf(UrlFactory.TEMP_URL_ENCODED_SEPARATOR, startIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length());
            if (endIndex < 0) {
                throw new RuntimeException("No end token for temp URL");
            }
            String tmpUrl = input.substring(startIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length(), endIndex);
            content.append(input, previousLast, startIndex);
            content.append(urlFactory.convertTempToPortalUrl(tmpUrl, mimeResponse));
            previousLast = endIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length();
        }
        content.append(input, previousLast, input.length());

        return content.toString();
    }
}
