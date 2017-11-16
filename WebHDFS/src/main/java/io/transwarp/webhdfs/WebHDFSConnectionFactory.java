package io.transwarp.webhdfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebHDFSConnectionFactory {
    // logger
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final int DEFAULT_CONNECTION_NUM=10;
    public static BlockingQueue<WebHDFSConnection> connectionQueue ;
    // The default host to connect to
    public static final String DEFAULT_HOST = "172.16.1.35";
    // The default port
    public static final int DEFAULT_PORT = 14000;
    // The default username
    public static final String DEFAULT_USERNAME = "hdfs";
    // The default password
    public static final String DEFAULT_PASSWORD = "123456";
    // default protocol http
    public static final String DEFAULT_PROTOCOL = "http://";
    public static final String DEFAULT_LINUXUSER="root";
    public static final String DEFAULT_LINUXPASSWORD="123456";
    public static final String DEFAULT_CHARSET = "utf-8";
    public static  String DEFAULT_LINUXHOST;

    // Authentication Type KERBEROS or PSEUDO
    public static enum AuthenticationType {
        KERBEROS, PSEUDO
    }

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private String authenticationType = AuthenticationType.KERBEROS.name();
    private WebHDFSConnection webHDFSConnection;

    // construction function  with nothing
    public WebHDFSConnectionFactory() {
        connectionQueue = new LinkedBlockingQueue(DEFAULT_CONNECTION_NUM);
    }

    // construction function with host,port,username,password,authType
    public WebHDFSConnectionFactory(
            String host, int port, String username, String password, String authType) {
        this.DEFAULT_LINUXHOST = host;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.authenticationType = authType;
        connectionQueue = new LinkedBlockingQueue(DEFAULT_CONNECTION_NUM);
    }

    // get the connection
    public WebHDFSConnection getConnection() {
        // TODO: use pool ...
        String httpfsUrl = DEFAULT_PROTOCOL + host + ":" + port;
        if(connectionQueue.size() != 0) {
            try {
                return connectionQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (webHDFSConnection == null) {
            if (authenticationType.equalsIgnoreCase(AuthenticationType.KERBEROS.name())) {
                for(int i=0;i<DEFAULT_CONNECTION_NUM;i++) {
                    try{
                        connectionQueue.put(new KerberosWebHDFSConnection(httpfsUrl,
                                username, password));
                    }catch(Exception e){
                        e.printStackTrace();
                        return null;
                    }
                }
            } else if (authenticationType.equalsIgnoreCase(AuthenticationType.PSEUDO.name())) {
                for(int i = 0; i < DEFAULT_CONNECTION_NUM; i++) {
                    try {
                        connectionQueue.put(new PseudoWebHDFSConnection(httpfsUrl, username, password));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } else {
                webHDFSConnection = null;
            }
        }
        try {
            return connectionQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
}
