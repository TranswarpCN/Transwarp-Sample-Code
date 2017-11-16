package io.transwarp.webhdfs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hadoop.security.authentication.client.Authenticator;
import org.apache.hadoop.security.authentication.client.ConnectionConfigurator;
import org.apache.hadoop.security.authentication.client.PseudoAuthenticator;
import org.apache.hadoop.security.authentication.util.KerberosUtil;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public class KerberosAuthenticator2 implements Authenticator {
    // HTTP header used by the SPNEGO server endpoint during an authentication sequence.
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    // HTTP header used by the SPNEGO client endpoint during an authentication sequence.
    public static final String AUTHORIZATION = "Authorization";
    // HTTP header prefix used by the SPNEGO client/server endpoints during an authentication sequence.
    public static final String NEGOTIATE = "Negotiate";
    private static final String AUTH_HTTP_METHOD = "OPTIONS";
    private boolean debug = true;
    private URL url;
    private HttpURLConnection conn;
    private Base64 base64;
    private String username;
    private String password;
    private String servicePrincipal;

    public KerberosAuthenticator2(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public KerberosAuthenticator2(String username, String password, String servicePrincipal) {
        super();
        this.username = username;
        this.password = password;
        this.servicePrincipal = servicePrincipal;
    }

    /*
     * Performs SPNEGO authentication against the specified URL.
     * <p/>
     * If a token is given it does a NOP and returns the given token.
     * <p/>
     * If no token is given, it will perform the SPNEGO authentication sequence
     * using an HTTP <code>OPTIONS</code> request.
     *
     * @param url
     *            the URl to authenticate against.
     * @param token
     *            the authentication token being used for the user.
     *
     * @throws IOException
     *             if an IO error occurred.
     * @throws AuthenticationException
     *             if an authentication error occurred.
     */
    public void authenticate(URL url, AuthenticatedURL.Token token)
            throws IOException, AuthenticationException {
        if (!token.isSet()) {
            this.url = url;
            base64 = new Base64(0);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(AUTH_HTTP_METHOD);
            conn.connect();
            if (isNegotiate()) {
                doSpnegoSequence(token);
            } else {
                getFallBackAuthenticator().authenticate(url, token);
            }
        }
    }

    /*
     * If the specified URL does not support SPNEGO authentication, a fallback
     * {@link Authenticator} will be used.
     * <p/>
     * This implementation returns a {@link PseudoAuthenticator}.
     *
     * @return the fallback {@link Authenticator}.
     */
    protected Authenticator getFallBackAuthenticator() {
        return new PseudoAuthenticator();
    }

    /*
     * Indicates if the response is starting a SPNEGO negotiation.
     */
    private boolean isNegotiate() throws IOException {
        boolean negotiate = false;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
            negotiate = authHeader != null
                    && authHeader.trim().startsWith(NEGOTIATE);
        }
        return negotiate;
    }

    /*
     * Implements the SPNEGO authentication sequence interaction using the
     * current default principal in the Kerberos cache (normally set via kinit).
     *
     * @param token
     *            the authentication token being used for the user.
     *
     * @throws IOException
     *             if an IO error occurred.
     * @throws AuthenticationException
     *             if an authentication error occurred.
     */
    private void doSpnegoSequence(AuthenticatedURL.Token token)
            throws IOException, AuthenticationException {
        try {

			/*
			 * // AccessControlContext context = AccessController.getContext();
			 * Subject subject = Subject.getSubject(context); if (subject ==
			 * null) { subject = new Subject(); LoginContext login = new
			 * LoginContext("", subject, null, new KerberosConfiguration());
			 * login.login(); }
			 */
            LoginContext loginContext = new LoginContext("hadoop-password-kerberos", null,
                    new KerberosClientCallbackHandler(username, password),
                    new LoginConfig(this.debug));
            loginContext.login();
//			if (LOG.isDebugEnabled()) {
//				LOG.debug("Kerberos authenticated user: "
//						+ loginContext.getSubject());
//			}
            Subject subject = loginContext.getSubject();

            Subject.doAs(subject, new PrivilegedExceptionAction<Void>() {

                public Void run() throws Exception {
                    GSSContext gssContext = null;
                    try {
                        GSSManager gssManager = GSSManager.getInstance();
                        Oid oid = KerberosUtil
                                .getOidInstance("NT_GSS_KRB5_PRINCIPAL");
                        String sp = KerberosAuthenticator2.this.servicePrincipal;
                        if(sp == null){
                            sp = "HTTP/"+ KerberosAuthenticator2.this.url.getHost();
                        }
                        GSSName serviceName = gssManager.createName(
                                sp, oid);
                        oid = KerberosUtil.getOidInstance("GSS_KRB5_MECH_OID");
                        gssContext = gssManager.createContext(serviceName, oid,
                                null, GSSContext.DEFAULT_LIFETIME);
                        gssContext.requestCredDeleg(true);
                        gssContext.requestMutualAuth(true);

                        byte[] inToken = new byte[0];
                        byte[] outToken;
                        boolean established = false;

                        // Loop while the context is still not established
                        while (!established) {
                            outToken = gssContext.initSecContext(inToken, 0,
                                    inToken.length);
                            if (outToken != null) {
                                sendToken(outToken);
                            }

                            if (!gssContext.isEstablished()) {
                                inToken = readToken();
                            } else {
                                established = true;
                            }
                        }
                    } finally {
                        if (gssContext != null) {
                            gssContext.dispose();
                            gssContext = null;
                        }
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw new AuthenticationException(ex.getException());
        } catch (LoginException ex) {
            throw new AuthenticationException(ex);
        }
        AuthenticatedURL.extractToken(conn, token);
    }

    /*
     * Sends the Kerberos token to the server.
     */
    private void sendToken(byte[] outToken) throws IOException,
            AuthenticationException {
        String token = base64.encodeToString(outToken);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(AUTH_HTTP_METHOD);
        conn.setRequestProperty(AUTHORIZATION, NEGOTIATE + " " + token);
        conn.connect();
    }

    /*
     * Retrieves the Kerberos token returned by the server.
     */
    private byte[] readToken() throws IOException, AuthenticationException {
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK
                || status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
            if (authHeader == null || !authHeader.trim().startsWith(NEGOTIATE)) {
                throw new AuthenticationException("Invalid SPNEGO sequence, '"
                        + WWW_AUTHENTICATE + "' header incorrect: "
                        + authHeader);
            }
            String negotiation = authHeader.trim()
                    .substring((NEGOTIATE + " ").length()).trim();
            return base64.decode(negotiation);
        }
        throw new AuthenticationException(
                "Invalid SPNEGO sequence, status code: " + status);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private static class LoginConfig extends Configuration {
        private boolean debug;

        public LoginConfig(boolean debug) {
            super();
            this.debug = debug;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("storeKey", "true");
            if (debug) {
                options.put("debug", "true");
            }

            return new AppConfigurationEntry[] { new AppConfigurationEntry(
                    "com.sun.security.auth.module.Krb5LoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options), };
        }
    }

    private static class KerberosClientCallbackHandler implements
            CallbackHandler {
        private String username;
        private String password;

        public KerberosClientCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException,
                UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) callback;
                    ncb.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pwcb = (PasswordCallback) callback;
                    pwcb.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(
                            callback,
                            "We got a "
                                    + callback.getClass().getCanonicalName()
                                    + ", but only NameCallback and PasswordCallback is supported");
                }
            }
        }
    }

    public void setConnectionConfigurator(ConnectionConfigurator arg0) {
        // TODO Auto-generated method stub
    }
}
