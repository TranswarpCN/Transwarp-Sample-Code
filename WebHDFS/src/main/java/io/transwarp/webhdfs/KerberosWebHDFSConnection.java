package io.transwarp.webhdfs;

import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by XKJ on 2016/12/12.
 */
public class KerberosWebHDFSConnection implements WebHDFSConnection {
    // logger
    protected static final Logger logger = LoggerFactory.getLogger(KerberosWebHDFSConnection2.class);
    // http fs url
    private String httpfsUrl = WebHDFSConnectionFactory.DEFAULT_PROTOCOL
            + WebHDFSConnectionFactory.DEFAULT_HOST + ":"
            + WebHDFSConnectionFactory.DEFAULT_PORT;
    // default user name
    private String principal = WebHDFSConnectionFactory.DEFAULT_USERNAME;
    // default password
    private String password = WebHDFSConnectionFactory.DEFAULT_PASSWORD;
    private String delegation = "";
    private RemoteShellTool tool;
    private String linuxUser = WebHDFSConnectionFactory.DEFAULT_LINUXUSER;
    private String linuxPassword = WebHDFSConnectionFactory.DEFAULT_LINUXPASSWORD;
    private String charset = WebHDFSConnectionFactory.DEFAULT_CHARSET;
    private AuthenticatedURL.Token token = new AuthenticatedURL.Token();
    private static AuthenticatedURL authenticatedURL  = new AuthenticatedURL();;

    public KerberosWebHDFSConnection() {
    }

    public KerberosWebHDFSConnection(String httpfsUrl, String principal,
                                     String password)  {
        this.httpfsUrl = httpfsUrl;
        this.principal = principal;
        this.password = password;
        this.delegation = generateToken();
    }

    String  generateToken(){
        tool = new RemoteShellTool(WebHDFSConnectionFactory.DEFAULT_LINUXHOST,linuxUser, linuxPassword, charset);
        String cmd0 = "kdestroy ;" +
                "rm -rf "+principal+".keytab ;" +
                "kadmin.local -q \"xst -norandkey -k "+principal+".keytab "+principal+"\"";

        String cmd = "kinit -kt "+principal+".keytab "+principal+" ;" +
                "curl -s --negotiate -u: \"http://172.16.2.96:50070/webhdfs/v1/?op=GETDELEGATIONTOKEN\"";

        tool.exec(cmd0);
        String result =  tool.exec(cmd);
        String token1 = result.substring(result.indexOf("\":\"")+3,result.indexOf("\"}}"));
        token=new AuthenticatedURL.Token(token1);
        return  token1;
    }

    protected static long copy(InputStream input, OutputStream result)
            throws IOException {
        byte[] buffer = new byte[12288]; // 8K=8192 12K=12288 64K=
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            result.write(buffer, 0, n);
            count += n;
            result.flush();
        }
        result.flush();
        return count;
    }

    private static String result(HttpURLConnection conn, boolean input)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        if (input) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            is.close();
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", conn.getResponseCode());
        result.put("mesg", conn.getResponseMessage());
        result.put("type", conn.getContentType());
        result.put("data", sb);

        // Convert a Map into JSON string.
        String json =toJson(result);

        return json;
    }

    public static String toJson( Map<String, Object> jsonMap) {
        StringBuffer buffer = new StringBuffer();
        if (jsonMap.size() > 0) {
            buffer.append("{");
            for (String key : jsonMap.keySet()) {
                if (!key.equals("class"))
                    buffer.append(key + " : '" + jsonMap.get(key) + "',");
            }
            // 去掉最后一个','
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("}");
        return buffer.toString();
    }

    public void ensureValidToken() {
        if (delegation.equals("")) { // if token is null
            delegation = generateToken();
        }
    }

    /*
     * <b>GETHOMEDIRECTORY</b>
     *
     * curl -i "http://<HOST>:<PORT>/webhdfs/v1/?op=GETHOMEDIRECTORY"
     *
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String getHomeDirectory() throws MalformedURLException, IOException,
            AuthenticationException {
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(new URL(
                new URL(httpfsUrl), "/webhdfs/v1/?delegation="+delegation+"&op=GETHOMEDIRECTORY"), token);

        conn.connect();
        String resp = result(conn, true);
        conn.disconnect();
        return resp;
    }

    /*
     * <b>OPEN</b>
     *
     * curl -i -L "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=OPEN
     * [&offset=<LONG>][&length=<LONG>][&buffersize=<INT>]"
     *
     * @param path
     * @param os
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String open(String path, OutputStream os)
            throws MalformedURLException, IOException, AuthenticationException {
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=OPEN", URLUtil.encodePath(path))),
                token);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.connect();
        InputStream is = conn.getInputStream();
        copy(is, os);
        is.close();
        os.close();
        String resp = result(conn, false);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>GETCONTENTSUMMARY</b>
     *
     * curl -i "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=GETCONTENTSUMMARY"
     *
     * @param path
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String getContentSummary(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=GETCONTENTSUMMARY",
                        URLUtil.encodePath(path))), token);
        conn.setRequestMethod("GET");
        // conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.connect();
        String resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>LISTSTATUS</b>
     *
     * curl -i "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=LISTSTATUS"
     *
     * @param path
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String listStatus(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        ensureValidToken();
        System.out.println("Token = "+token.isSet());

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=LISTSTATUS",
                        URLUtil.encodePath(path))), token);
        conn.setRequestMethod("GET");
        conn.connect();
        String resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>GETFILESTATUS</b>
     *
     * curl -i "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=GETFILESTATUS"
     *
     * @param path
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String getFileStatus(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=GETFILESTATUS",
                        URLUtil.encodePath(path))), token);
        conn.setRequestMethod("GET");
        conn.connect();
        String resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>GETFILECHECKSUM</b>
     *
     * curl -i "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=GETFILECHECKSUM"
     *
     * @param path
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String getFileCheckSum(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=GETFILECHECKSUM",
                        URLUtil.encodePath(path))), token);

        conn.setRequestMethod("GET");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>CREATE</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=CREATE
     * [&overwrite=<true|false>][&blocksize=<LONG>][&replication=<SHORT>]
     * [&permission=<OCTAL>][&buffersize=<INT>]"
     *
     * @param path
     * @param is
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String create(String path, InputStream is)
            throws MalformedURLException, IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        String redirectUrl = null;
        HttpURLConnection conn = authenticatedURL
                .openConnection(
                        new URL(new URL(httpfsUrl), MessageFormat.format(
                                "/webhdfs/v1/{0}?delegation="+delegation+"&op=CREATE",
                                URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        logger.info("Location:" + conn.getHeaderField("Location"));
        System.out.println("Location:" + conn.getHeaderField("Location"));
        resp = result(conn, true);
        if (conn.getResponseCode() == 307)
            redirectUrl = conn.getHeaderField("Location");
        conn.disconnect();

        if (redirectUrl != null) {
            conn = authenticatedURL.openConnection(new URL(redirectUrl), token);
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            // conn.setRequestProperty("Transfer-Encoding", "chunked");
            final int _SIZE = is.available();
            conn.setRequestProperty("Content-Length", "" + _SIZE);
            conn.setFixedLengthStreamingMode(_SIZE);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            copy(is, os);
            // Util.copyStream(is, os);
            is.close();
            os.close();
            resp = result(conn, false);
            conn.disconnect();
        }

        return resp;
    }

    /*
     * <b>MKDIRS</b>
     *
     * curl -i -X PUT
     * "http://<HOST>:<PORT>/<PATH>?op=MKDIRS[&permission=<OCTAL>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String mkdirs(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL
                .openConnection(
                        new URL(new URL(httpfsUrl), MessageFormat.format(
                                "/webhdfs/v1/{0}?delegation="+delegation+"&op=MKDIRS",
                                URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>CREATESYMLINK</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/<PATH>?op=CREATESYMLINK
     * &destination=<PATH>[&createParent=<true|false>]"
     *
     * @param srcPath
     * @param destPath
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String createSymLink(String srcPath, String destPath)
            throws MalformedURLException, IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=CREATESYMLINK&destination={1}",
                        URLUtil.encodePath(srcPath),
                        URLUtil.encodePath(destPath))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>RENAME</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/<PATH>?op=RENAME
     * &destination=<PATH>[&createParent=<true|false>]"
     *
     * @param srcPath
     * @param destPath
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String rename(String srcPath, String destPath)
            throws MalformedURLException, IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=RENAME&destination={1}",
                        URLUtil.encodePath(srcPath),
                        URLUtil.encodePath(destPath))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>SETPERMISSION</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=SETPERMISSION
     * [&permission=<OCTAL>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String setPermission(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=SETPERMISSION",
                        URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>SETOWNER</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=SETOWNER
     * [&owner=<USER>][&group=<GROUP>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String setOwner(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl),
                        MessageFormat.format("/webhdfs/v1/{0}?delegation="+delegation+"&op=SETOWNER&owner=hdfs",
                                URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>SETREPLICATION</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=SETREPLICATION
     * [&replication=<SHORT>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String setReplication(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=SETREPLICATION",
                        URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * <b>SETTIMES</b>
     *
     * curl -i -X PUT "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=SETTIMES
     * [&modificationtime=<TIME>][&accesstime=<TIME>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String setTimes(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl),
                        MessageFormat.format("/webhdfs/v1/{0}?delegation="+delegation+"&op=SETTIMES",
                                URLUtil.encodePath(path))), token);
        conn.setRequestMethod("PUT");
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    /*
     * curl -i -X POST
     * "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=APPEND[&buffersize=<INT>]"
     *
     * @param path
     * @param is
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws AuthenticationException
     */
    public String append(String path, InputStream is)
            throws MalformedURLException, IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        String redirectUrl = null;
        HttpURLConnection conn = authenticatedURL.openConnection(
                new URL(new URL(httpfsUrl), MessageFormat.format(
                        "/webhdfs/v1/{0}?delegation="+delegation+"&op=APPEND", path)), token);
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        logger.info("Location:" + conn.getHeaderField("Location"));
        resp = result(conn, true);
        if (conn.getResponseCode() == 307)
            redirectUrl = conn.getHeaderField("Location");
        conn.disconnect();

        if (redirectUrl != null) {
            conn = authenticatedURL.openConnection(new URL(redirectUrl), token);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            // conn.setRequestProperty("Transfer-Encoding", "chunked");
            final int _SIZE = is.available();
            conn.setRequestProperty("Content-Length", "" + _SIZE);
            conn.setFixedLengthStreamingMode(_SIZE);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            copy(is, os);
            // Util.copyStream(is, os);
            is.close();
            os.close();
            resp = result(conn, true);
            conn.disconnect();
        }

        return resp;
    }

    /*
     * <b>DELETE</b>
     *
     * curl -i -X DELETE "http://<host>:<port>/webhdfs/v1/<path>?op=DELETE
     * [&recursive=<true|false>]"
     *
     * @param path
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws MalformedURLException
     */
    public String delete(String path) throws MalformedURLException,
            IOException, AuthenticationException {
        String resp = null;
        ensureValidToken();

        HttpURLConnection conn = authenticatedURL
                .openConnection(
                        new URL(new URL(httpfsUrl), MessageFormat.format(
                                "/webhdfs/v1/{0}?delegation="+delegation+"&op=DELETE",
                                URLUtil.encodePath(path))), token);
        conn.setRequestMethod("DELETE");
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        resp = result(conn, true);
        conn.disconnect();

        return resp;
    }

    public String getHttpfsUrl() {
        return httpfsUrl;
    }

    public void setHttpfsUrl(String httpfsUrl) {
        this.httpfsUrl = httpfsUrl;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
