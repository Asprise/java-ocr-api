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

import com.asprise.ocr.Ocr;
import com.asprise.ocr.sample.util.prefs.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;


public class OcrLibHelper {

    protected static boolean loaded = false;

    public static void loadOcrLib() {
        if(loaded) {
            log("Native library already loaded.");
            return;
        }

        String libFileName = null;
        if(Utils.isWindows()) {
            libFileName = Utils.is64Bit() ? "aocr_x64.dll" : "aocr.dll";
        } else if(Utils.isMac()) {
            libFileName = "libaocr_x64.dylib";
            if(! Utils.is64Bit()) {
                System.err.println("You must run Java in 64bit mode on Mac OS X: " + Utils.getEnvInfo(false));
                System.exit(1);
            }
        } else { 
            libFileName = Utils.is64Bit() ? "libaocr_x64.so" : "libaocr.so";
        }
        if(libFileName == null) {
            System.err.println("Failed to detect lib file name. os.name=" + System.getProperty("os.name") + ", os.arch=" + System.getProperty("os.arch"));
            System.exit(2);
        }

        File fileLib = null;
        try {
            if(StringUtils.isEmpty(System.getProperty("ASPRISE_OCR_LIB_FILE"))) { 
                fileLib = Utils.extractResourceToFolder(OcrLibHelper.class.getClassLoader(),
                        Utils.firstNotEmpty(System.getProperty("ASPRISE_OCR_LIB_PATH"),
                                libFileName), Utils.getTempFolderWritable(), "true".equalsIgnoreCase(System.getProperty("ASPRISE_OCR_FORCE_WRITE")));
                log("Loading native library from file: " + fileLib.getAbsolutePath());
                System.load(fileLib.getAbsolutePath());
            } else { 
                fileLib = new File(System.getProperty("ASPRISE_OCR_LIB_FILE"));
                log("Loading native library from file: " + System.getProperty("ASPRISE_OCR_LIB_FILE"));
                System.load(System.getProperty("ASPRISE_OCR_LIB_FILE"));
            }
            log("Native library loaded successfully.");
            loaded = true;
        } catch (UnsatisfiedLinkError ule) {
            String osUrlEncoded = System.getProperty("os.name").replace(" ", "%20");
            System.err.println("\n>>> UnsatisfiedLinkError occurs. To fix it, visit http://asprise.com/ocr/fix-link-error?os=" + osUrlEncoded);
            System.err.println(getDebugInfo(fileLib));
            ule.printStackTrace();
            if(ule.getMessage() != null && ule.getMessage().contains("already loaded")) {
                log("Skipped loading native library as: " + ule);
            }
            System.err.println("\n>>> Relax, it's easy to fix: http://asprise.com/ocr/fix-link-error?os=" + osUrlEncoded);
        } catch (Throwable e) {
            System.err.println(">>> Asprise will be glad to help you. Please copy the follow text and email to support@asprise.com");
            System.err.println(getDebugInfo(fileLib));
            e.printStackTrace();
            Utils.displayErrorDialogAndThrowException("Failed to load native library: " + e.getMessage() + "\n" + getDebugInfo(fileLib), e, false);
        }
    }

    static String getDebugInfo(File libFile) {
        StringBuilder sb = new StringBuilder("\n");
        if(libFile != null && !Utils.isWindows()) { 
            String[] cmds = null;
            if(Utils.isMac()) {
                cmds = new String[] {"/usr/bin/otool", "-L", libFile.getAbsolutePath()};
            } else {
                cmds = new String[] {"/usr/bin/ldd", libFile.getAbsolutePath()};
            }
            String deps = Utils.execute(true, false, false, cmds, null, null);
            if(! StringUtils.isEmpty(deps)) {
                sb.append("\nDependency information: " + deps);
            }
        }

        String ldLibPath = System.getenv("LD_LIBRARY_PATH");
        if(! StringUtils.isEmpty(ldLibPath)) {
            sb.append("\nLD_LIBRARY_PATH=" + ldLibPath);
        }

        sb.append("\njava.library.path=" + System.getProperty("java.library.path"));
        sb.append("\n").append(Utils.getEnvInfo(false));

        return sb.toString();
    }

    
    public static String loadDllAndReturnVersion(String dllPath) {
        System.setProperty("ASPRISE_OCR_LIB_FILE", dllPath);
        loadOcrLib();
        return Ocr.getLibraryVersion();
    }

    private static BufferedImage getSampleImage() throws IOException {
        String imageBase64 = "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";
        byte[] imgBytes = Base64.base64ToByteArray(imageBase64);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
        return img;
    }

    static void setLibFile(String libFile) {
        System.setProperty("ASPRISE_OCR_LIB_FILE", libFile);
    }

    static void testOcr() throws IOException {
        System.out.println("OCR version: " + Ocr.getLibraryVersion(true));
        Ocr.setUp();
        Ocr ocr = new Ocr();
        ocr.startEngine("eng", Ocr.SPEED_FASTEST);
        String s = ocr.recognize(getSampleImage(), Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PDF,
                new Ocr.PropertyBuilder()
                        .setPageType(Ocr.PageType.SINGLE_BLOCK)
                        .setPdfOutputFile(File.createTempFile("ocr-result", ".pdf"))
                        .setPdfTextVisible(true)
                        .setPdfOutputReturnXml());
        System.out.println("OCR result: \n" + s);
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            System.out.println("Usage: java com.asprise.ocr.util.OcrLibHelper PATH_TO_LIB_FILE");
            return;
        }

        setLibFile(args[0]);
        testOcr();
    }

    public static void log(String s) {
        if(shouldPrintLog()) {
            System.out.println(s);
        }
    }

    private static int shouldPrintLog = -1;
    private static boolean shouldPrintLog() {
        if(shouldPrintLog < 0) {
            shouldPrintLog = (!StringUtils.isEmpty(System.getProperty("DEBUG"))) ? 1 : 0;
        }

        return shouldPrintLog == 1;
    }
}
