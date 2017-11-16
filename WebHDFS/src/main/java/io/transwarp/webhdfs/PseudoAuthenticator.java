package io.transwarp.webhdfs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hadoop.security.authentication.client.Authenticator;
import org.apache.hadoop.security.authentication.client.ConnectionConfigurator;

public class PseudoAuthenticator implements Authenticator{
    // Name of the additional parameter that carries the 'user.name' value.
    public static final String USER_NAME = "user.name";

    private static final String USER_NAME_EQ = USER_NAME + "=";

    /*
     * Performs simple authentication against the specified URL.
     * <p/>
     * If a token is given it does a NOP and returns the given token.
     * <p/>
     * If no token is given, it will perform an HTTP <code>OPTIONS</code>
     * request injecting an additional parameter {@link #USER_NAME} in the query
     * string with the value returned by the {@link #getUserName()} method.
     * <p/>
     * If the response is successful it will update the authentication token.
     *
     * @param url
     *            the URl to authenticate against.
     * @param token
     *            the authencation token being used for the user.
     *
     * @throws IOException
     *             if an IO error occurred.
     * @throws AuthenticationException
     *             if an authentication error occurred.
     */
    public void authenticate(URL url, AuthenticatedURL.Token token)
            throws IOException, AuthenticationException {
        String strUrl = url.toString();
        String paramSeparator = (strUrl.contains("?")) ? "&" : "?";
        strUrl += paramSeparator + USER_NAME_EQ + getUserName();
        url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("OPTIONS");
        conn.connect();
        AuthenticatedURL.extractToken(conn, token);
    }

    protected String getUserName() {
        return username != null ? username : System.getProperty("user.name");
    }

    private String username = null;

    public PseudoAuthenticator(String username) {
        this.username = username;
    }

    public PseudoAuthenticator() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setConnectionConfigurator(ConnectionConfigurator arg0) {
        // TODO Auto-generated method stub
    }
}
