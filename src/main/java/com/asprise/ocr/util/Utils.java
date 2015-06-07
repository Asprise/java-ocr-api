/**********************************************************************************************
 *
 * Asprise OCR Java API
 * Copyright (C) 1998-2015. Asprise Inc. <asprise.com>
 *
 * This file is licensed under the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * You should have received a copy of the GNU Affero General Public License.  If not, please
 * visit <http://www.gnu.org/licenses/agpl-3.0.html>.
 *
 **********************************************************************************************/
package com.asprise.ocr.util;


import javax.swing.JOptionPane;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Utils {
    static String osName = System.getProperty("os.name");
    static String osArch = System.getProperty("os.arch");

    
    public static boolean isWindows() {
        return osName != null && osName.toLowerCase().startsWith("windows");
    }

    
    public static boolean isMac() {
        return osName != null && osName.toLowerCase().startsWith("mac");
    }

    
    public static boolean is64Bit() {
        return osArch.contains("64");
    }

    
    public static boolean is32Bit() {
        return !is64Bit();
    }

    
    public static String flagToString(long value, Object... flagValueAndNames) {
        if(flagValueAndNames.length % 2 != 0) {
            throw new IllegalArgumentException("Flag values and names must come in pair");
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < flagValueAndNames.length; ) {
            if((value & (Long)flagValueAndNames[i]) != 0) {
                if(sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(flagValueAndNames[i+1]);
            }
            i += 2;
        }

        for(int i = 0; i < flagValueAndNames.length; ) {
            if((value & (Long)flagValueAndNames[i]) != 0) {
                value -= (Long)flagValueAndNames[i];
            }
            i += 2;
        }
        boolean exhausted = value == 0;

        if(! exhausted) {
            sb.append(" (0x").append(Long.toHexString(value)).append(")");
        }

        return sb.toString();
    }

    
    public static long clearFlags(long value, long... flags) {
        for(int i = 0; i < flags.length; i++) {
            if((value & flags[i]) != 0) {
                value -= flags[i];
            }
        }
        return value;
    }

    public static String getEnvInfo(boolean withClassPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" ").append(System.getProperty("os.arch")).append("\n");
        if(! isWindows()) {
            String uname = null;
            try {
                uname = execute(true, false, false, new String[]{"/usr/bin/uname", "-a"}, null, null);
            } catch (Throwable t) {

            }
            if(StringUtils.isEmpty(uname)) {
                try {
                    uname = execute(true, false, false, new String[]{"/bin/uname", "-a"}, null, null);
                } catch (Throwable t) {

                }
            }
            if(! StringUtils.isEmpty(uname)) {
                sb.append(uname).append("\n");
            }
        }
        sb.append("JVM: ").append(Utils.firstNotNull(System.getProperty("java.runtime.version"), System.getProperty("java.version"))).append(" ").append(Utils.is64Bit() ? "64bit" : "32bit")
            .append(" by ").append(System.getProperty("java.vendor")).append("\n");
        if(withClassPath) {
            sb.append("Classpath: ").append(System.getProperty("java.class.path"));
        }
        return sb.toString();
    }

    public static String getRunInfo() {
        StringBuilder sb = new StringBuilder();
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threads = threadSet.toArray(new Thread[threadSet.size()]);
        sb.append("Threads: [").append(threads.length).append("] ").append(Arrays.toString(threads));

        return sb.toString();
    }

    public static Object firstNotNull(Object... args) {
        for(Object o : args) {
            if(o != null) {
                return o;
            }
        }
        return null;
    }

    public static String firstNotEmpty(String... args) {
        for(String s : args) {
            if(s != null && s.trim().length() > 0) {
                return s;
            }
        }
        return null;
    }

    public static void displayErrorDialogAndThrowException(String errorMesg, Throwable t, boolean rethrowException) {
        try {
            if(errorMesg != null && errorMesg.length() > 80) {
                errorMesg = errorMesg.replaceAll("(.{80})", "$1\n");
            }
            JOptionPane.showMessageDialog(null, errorMesg, "Fatal error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException e) {
            e.printStackTrace();
        }
        if(rethrowException) {
            throw new RuntimeException(errorMesg, t);
        }
    }

    
    public static boolean testCreateFileInside(File folder) {
        if(! folder.exists()) { 
            try {
                folder.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(! folder.isDirectory()) {
            return false;
        }

        File f = new File(folder, "t-" + System.currentTimeMillis() + "-" + StringUtils.randomAlphanumeric(3));
        try {
            boolean fileCreated = f.createNewFile();
            boolean fileDeleted = f.delete();
            return fileCreated;
        } catch (Throwable t) {
            return false;
        }
    }

    static File dirTempWritable = null;

    public static File getTempFolderWritable() {
        if(dirTempWritable != null) {
            return dirTempWritable;
        }

        File[] dirs = new File[] {
            new File(System.getProperty("java.io.tmpdir")), 
            new File(System.getProperty("user.dir")), 
            new File(Utils.isWindows() ?
                ((System.getProperty("java.io.tmpdir") != null && System.getProperty("java.io.tmpdir").length() > 0 ? System.getProperty("java.io.tmpdir").substring(0, 1) : "C") + ":\\") : "/") 
        };

        for(File dir : dirs) {
            if(testCreateFileInside(dir)) {
                dirTempWritable = dir;
                return dirTempWritable;
            }
        }

        StringBuilder errorMesg = new StringBuilder("Failed to find a writable temp directory. Tried: ");
        for(int i = 0; i < dirs.length; i++) {
            errorMesg.append(i == 0 ? "" : ", ");
            errorMesg.append(dirs[i].getAbsolutePath());
        }

        displayErrorDialogAndThrowException(errorMesg.toString(), null, true);
        return null;
    }

    public static void unzipToFolder(File zipFile, File outputFolder) throws IOException {
        outputFolder.mkdirs();

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

        ZipEntry ze = zis.getNextEntry();

        byte[] buffer = new byte[1024 * 4];

        while(ze!=null){

            String fileName = ze.getName();
            File newFile = new File(outputFolder + File.separator + fileName);

            if(ze.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
            }
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    
    public static File extractResourceToFolder(ClassLoader classLoader, String resourcePath, File targetFolder, boolean forceWrite) throws IOException {
        int lastSlashPos = Math.max(resourcePath.lastIndexOf('/'), resourcePath.lastIndexOf('\\'));
        String simpleName = lastSlashPos > 0 ? resourcePath.substring(lastSlashPos + 1) : resourcePath;

        targetFolder.mkdirs();
        if(! targetFolder.exists()) {
            throw new IOException("Folder does not exist despite effort of attempting to create it: " + targetFolder.getAbsolutePath());
        }

        File targetFile = new File(targetFolder, simpleName);
        boolean skipWriting = false;
        try {
            if((!forceWrite) && targetFile.exists()) {
                String md5ExistingFile = getMd5(new FileInputStream(targetFile));
                String md5Stream = getMd5(classLoader.getResourceAsStream(resourcePath));
                if(md5ExistingFile != null && md5Stream != null && md5ExistingFile.equalsIgnoreCase(md5Stream)) { 
                    skipWriting = true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if(! skipWriting) {
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if(inputStream == null) {
                throw new IOException("Can not find resource: " + resourcePath);
            }

            try {
                FileOutputStream outputStream = new FileOutputStream(targetFile);
                byte[] buffer = new byte[1024 * 4];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();

            } finally {
                inputStream.close();
            }
        }

        if(resourcePath.endsWith(".zip")) {
            String simpleNameWithoutExt = targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));
            File zipOutputFolder = new File(targetFile.getAbsoluteFile().getParentFile(), simpleNameWithoutExt);
            if(zipOutputFolder.exists() && zipOutputFolder.isFile()) { 
                zipOutputFolder.delete();
                zipOutputFolder = new File(zipOutputFolder.getAbsolutePath());
                zipOutputFolder.mkdirs();
            }
            unzipToFolder(targetFile, zipOutputFolder);
            return zipOutputFolder;
        }

        return targetFile;
    }

    
    static String getMd5(InputStream input) {
        if(input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(input, md);
            byte[] buffer = new byte[1024 * 8]; 
            while(dis.read(buffer) != -1) {
                ;
            }
            dis.close();
            byte[] raw = md.digest();

            BigInteger bigInt = new BigInteger(1, raw);
            StringBuilder hash = new StringBuilder(bigInt.toString(16));

            while(hash.length() < 32 ){
                hash.insert(0, '0');
            }
            return hash.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getFileExtensionWithoutDot(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    public static boolean openInFileExplorer(String path) {
        if(isWindows()) {
            try {
                Process p = new ProcessBuilder("explorer.exe", path).start();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    
    public static void saveToFile(URL url, File target) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream output = new FileOutputStream(target);
        output.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    static int isLaptop = Integer.MIN_VALUE;

    
    public static int isLaptop() {
        if(isLaptop != Integer.MIN_VALUE) {
            return isLaptop;
        }

        if(! isWindows()) {
            return -1;
        }
        String result = "";
        File file = null;
        try {
            file = File.createTempFile("info", ".vbs", null);

            FileWriter fw = new FileWriter(file);

            String vbs = "strComputer = \".\"\n" +
                    "Set objWMIService = GetObject(\"winmgmts:\" _\n" +
                    "    & \"{impersonationLevel=impersonate}!\\\\\" & strComputer & \"\\root\\cimv2\")\n" +
                    "Set colChassis = objWMIService.ExecQuery _\n" +
                    "    (\"Select * from Win32_SystemEnclosure\")\n" +
                    "For Each objChassis in colChassis\n" +
                    "    For  Each strChassisType in objChassis.ChassisTypes\n" +
                    "        Wscript.Echo strChassisType\n" +
                    "    Next\n" +
                    "Next\n";

            fw.write(vbs);
            fw.close();

            Process p = Runtime.getRuntime().exec("cscript //NoLogo \"" + file.getPath() + "\"");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (IOException e) {
            return -1;
        }

        try {
            file.delete();
        } catch (Exception e) {

        }

        int chassis = Integer.parseInt(result.trim());
        return chassis >= 8 && chassis <= 12 ? 1 : 0;
    }

    public static void main(String[] args) {
        System.out.println(execute(true, true, true, new String[] {"cmd", "/c", "dir"}, new File("W:"), null));
    }

    
    public static String execute(boolean returnOutput, boolean returnError, boolean throwException, String[] cmd, File dir, String[] env) {
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        try {
            Process proc = Runtime.getRuntime().exec(cmd, env, dir);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String line = null;
            while ((line = stdInput.readLine()) != null) {
                if(output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
            }

            while ((line = stdError.readLine()) != null) {
                if(error.length() > 0) {
                    error.append("\n");
                }
                error.append(line);
            }

            if(returnOutput) {
                return output.toString();
            }
            if(returnError) {
                return error.toString();
            }
            return null;
        } catch (Throwable t) {
            if(throwException) {
                throw new RuntimeException(t);
            } else {
                if(System.getProperty("DEBUG") != null) {
                    t.printStackTrace();
                }
                if(returnOutput) {
                    return output.length() > 0 ? output.toString() : null;
                }
                if(returnError) {
                    return error.length() > 0 ? error.toString() : t.getMessage();
                }
                return null;
            }
        }
    }
}
