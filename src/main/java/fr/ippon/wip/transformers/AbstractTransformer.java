package fr.ippon.wip.transformers;

import fr.ippon.wip.http.UrlFactory;
import org.xml.sax.SAXException;

import javax.portlet.PortletRequest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 04/06/12
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTransformer implements WIPTransformer {
    protected UrlFactory urlFactory;

    public AbstractTransformer (PortletRequest portletRequest) {
        urlFactory = new UrlFactory(portletRequest);
    }

    abstract public String transform(String input) throws SAXException, IOException, TransformerException;

    protected int extractGroup(Matcher matcher) {
        int matchingGroup = -1;
        for(int i = 1; i <= matcher.groupCount(); i++) {
            if(matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        return matchingGroup;
    }

}
