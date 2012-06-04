package fr.ippon.wip.http.hc;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 02/06/12
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class TransformedEntity extends HttpEntityWrapper {

    private String transformedContent = null;
    /**
     * Creates a new entity wrapper.
     *
     * @param wrapped the entity to wrap, not null
     * @throws IllegalArgumentException if wrapped is null
     */
    public TransformedEntity(HttpEntity wrapped, String transformedContent) {
        super(wrapped);
        this.transformedContent = transformedContent;
    }

    @Override
    public long getContentLength() {
        return transformedContent.length();
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public InputStream getContent() throws IOException {
        return super.getContent();    //To change body of overridden methods use File | Settings | File Templates.
    }

}
