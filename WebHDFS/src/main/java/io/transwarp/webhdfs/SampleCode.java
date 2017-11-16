package io.transwarp.webhdfs;

import org.apache.hadoop.security.authentication.client.AuthenticationException;

import java.io.*;
import java.net.URISyntaxException;

public class SampleCode {
    public static void main(String[] args) throws IOException, URISyntaxException, AuthenticationException {
        WebHDFSConnectionFactory webHDFSConnectionFactory = new WebHDFSConnectionFactory("172.16.2.96",50070, "hdfs",
                "123456", WebHDFSConnectionFactory.AuthenticationType.KERBEROS.name());
        WebHDFSConnection connection = webHDFSConnectionFactory.getConnection();
        String json = "";

        try {
            json = connection.delete("tmp/hdfs-site0g0.xml");
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    public static void testPseudo() {
        WebHDFSConnectionFactory webHDFSConnectionFactory = new WebHDFSConnectionFactory("172.16.1.35",50070, "hdfs",
                "123456", WebHDFSConnectionFactory.AuthenticationType.PSEUDO.name());
        WebHDFSConnection connection = webHDFSConnectionFactory.getConnection();

        try {
            String json = "";
            json = connection.delete("user/hdfs/core-site.xml006");
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }
}
