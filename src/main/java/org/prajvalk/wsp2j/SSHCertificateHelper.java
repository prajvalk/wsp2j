package org.prajvalk.wsp2j;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSHCertificateHelper {

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    public static void loadcert(String... args) throws Exception {

        String host       = null;
        int    port       = -1;
        String keystore   = null;
        char[] passphrase = null;

        String defaultPassphrase = "changeit";

        int numArg = 0;
        int nbArgs = args.length;

        boolean invalidArgs = false;
        boolean isQuiet = false;

        while (numArg < nbArgs) {
            String arg = args[numArg++];

            if (host == null) {  // 1st argument is the "host:port"
                String[] c = arg.split(":");
                host = c[0];
                port = (c.length == 1) ? 443 : Integer.parseInt(c[1]);
            }
            else if (keystore == null) {  //  2nd argument is the keystore passphrase
                keystore = arg;
            }
            else if (passphrase == null) {  //  2nd argument is the keystore passphrase
                passphrase = arg.toCharArray();
            }
            else {
                invalidArgs = true;  // too many args
            }
        }

        if (host == null) {
            invalidArgs = true;
        }

        if (invalidArgs) {
            System.out.println("wsp2j::sshcertservice ERROR: Internal Communication Error");
            return;
        }

        File file = null;
        if(keystore != null) {
            file = new File(keystore);
        } else {
            file = new File("");
        }

        if (!file.isFile()) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (!file.isFile()) {
                passphrase = defaultPassphrase.toCharArray();
                file = new File(dir, "cacerts");
                keystore = file.getAbsolutePath();
            }
        }

        //Load keystore
        InputStream in = new FileInputStream(keystore);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SaveTrustManager tm = new SaveTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();

        //Initiate socket
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);

        try {
            socket.startHandshake();
            socket.close();
            return;
        } catch (SSLException e) {
            e.printStackTrace(System.out);
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            System.out.println("wsp2j::sshcertservice ERROR: SSH Root Certificate Not Found");
            return;
        }

        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);

            OutputStream out = new FileOutputStream(keystore);
            ks.store(out, passphrase);
            out.close();

            System.out.println(cert);
            System.out.println("wsp2j::sshcertservice: Added SSH Certificate " + alias + " to local keystore");
        }

    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    //Inner class to save the default trust manager
    private static class SaveTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SaveTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            // This change has been done due to the following resolution advised for Java 1.7+
            // http://infposs.blogspot.kr/2013/06/installcert-and-java-7.html
            return new X509Certificate[0];
            //throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}