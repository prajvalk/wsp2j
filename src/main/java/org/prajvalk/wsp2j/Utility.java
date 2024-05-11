package org.prajvalk.wsp2j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.Vector;

public class Utility {

    public static String getData(String URL) {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc.text();
    }

    public static String getHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(data.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveObject(String fn, Object o) {
        try {
            FileOutputStream fout = new FileOutputStream(fn);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(o);
            oos.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static Object getObject(String fn) {
        try {
            FileInputStream fin = new FileInputStream(fn);
            ObjectInputStream ois = new ObjectInputStream(fin);
            return ois.readObject();
        }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static Vector<String> readData(String fn) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fn));
            Vector<String> vec = new Vector<>(1,1);
            while(true) {
                String st = br.readLine();
                if(st == null) break;
                vec.addElement(st);
            }
            br.close();
            return vec;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public static void writeData(String fn, Vector<String> data) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fn));
            for(String st : data) {
                bw.write(st);
                bw.newLine();
            }
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static String getTime() {
        return (new Date()).toString();
    }

    public static String getSpecificTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return sdf.format(cal.getTime());
    }

}
