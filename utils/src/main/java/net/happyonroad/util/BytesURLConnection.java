/**
 * @author XiongJie, Date: 13-8-28
 */
package net.happyonroad.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BytesURLConnection extends URLConnection
{
    protected byte[] content;

    public BytesURLConnection( URL url, byte[] content )
    {
        super( url );
        this.content = content;
    }

    public void connect()
    {
    }

    public InputStream getInputStream()
    {
        return new ByteArrayInputStream( content );
    }

    @Override
    public long getLastModified() {
        return super.getLastModified();
    }
}