package net.ME1312.SubServers.Bungee.Library;

import java.io.*;
import java.util.Random;

/**
 * SubServers Utility Class
 */
public final class Util {
    private Util(){}
    public interface ExceptionRunnable {
        void run() throws Throwable;
    }

    /**
     * Read Everything from Reader
     *
     * @param rd Reader
     * @return Reader Contents
     * @throws IOException
     */
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Copy from the Class Loader
     *
     * @param loader ClassLoader
     * @param resource Location From
     * @param destination Location To
     */
    public static void copyFromJar(ClassLoader loader, String resource, String destination) {
        InputStream resStreamIn = loader.getResourceAsStream(resource);
        File resDestFile = new File(destination);
        try {
            OutputStream resStreamOut = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
            resStreamIn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Determines if an Exception will occur
     *
     * @param runnable Runnable
     * @return If an Exception occured
     */
    public static boolean isException(ExceptionRunnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (Throwable e) {
            return true;
        }
    }

    /**
     * Delete Directory
     *
     * @param folder Location
     */
    public static void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Copy a Directory
     *
     * @param from Source
     * @param to Destination
     */
    public static void copyDirectory(File from, File to) {
        if (from.isDirectory()) {
            if (!to.exists()) {
                to.mkdirs();
            }

            String files[] = from.list();

            for (String file : files) {
                File srcFile = new File(from, file);
                File destFile = new File(to, file);

                copyDirectory(srcFile, destFile);
            }
        } else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(from);
                out = new FileOutputStream(to);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {
                try {
                    if (in != null) in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    if (out != null) out.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * Get a Random Integer
     *
     * @param min Minimum Value
     * @param max Maximum Value
     * @return Random Integer
     */
    public static int random(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    /**
     * Parse escapes in a Java String
     *
     * @param str String
     * @return Unescaped String
     */
    public static String unescapeJavaString(String str) {

        StringBuilder sb = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == str.length() - 1) ? '\\' : str
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < str.length() - 1) && str.charAt(i + 1) >= '0'
                            && str.charAt(i + 1) <= '7') {
                        code += str.charAt(i + 1);
                        i++;
                        if ((i < str.length() - 1) && str.charAt(i + 1) >= '0'
                                && str.charAt(i + 1) <= '7') {
                            code += str.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= str.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + str.charAt(i + 2) + str.charAt(i + 3)
                                        + str.charAt(i + 4) + str.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
