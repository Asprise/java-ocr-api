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
package com.asprise.ocr;

import com.asprise.ocr.sample.FrameOcrSample;
import com.asprise.ocr.util.OcrLibHelper;
import com.asprise.ocr.util.StringUtils;
import com.asprise.ocr.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Represents an engine of Asprise OCR - a high performance OCR engine for Java/C#/VB.NET/C/C++ on Windows, Linux, Mac OS X and Unix.
 * <p><a href='http://asprise.com/ocr/docs/html/?src=javadoc' target='_blank'>Click here to access the Asprise OCR developer's guide.</a></p>
 * <p>
 *     The OCR engine is capable of recognizing text in 20+ languages (English, Spanish, French, German, Italian,
 *     Hungarian, Finnish, Swedish, Romanian, Polish, Malay, Arabic, Indonesian, and Russian) and 1-D/2-D barcode of most
 *     popular formats (EAN-8, EAN-13, UPC-A, UPC-E, ISBN-10, ISBN-13, Interleaved 2 of 5, Code 39, Code 128, PDF417,
 *     and QR Code.).
 * </p>
 * <p>An instance of this class should be used by one thread at a time. For multi-treading, please
 * create multiple instance of this class.</p>
 * <h3>Basic code sample</h3>
 * <pre>Ocr.setUp(); // one time setup
 * Ocr ocr = new Ocr();
 * ocr.startEngine("eng", Ocr.SPEED_FASTEST);
 * String s = ocr.recognize(new File[] {new File("test.jpg")}, Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PLAINTEXT, 0, null);
 * System.out.println("RESULT: " + s);
 * // do more recognition here ...
 * ocr.stopEngine();</pre>
 */
public class Ocr {

    /** Highest speed, accuracy may suffer - default option */
    public static final String SPEED_FASTEST           = "fastest";
    /** less speed, better accuracy */
    public static final String SPEED_FAST              = "fast";
    /** lowest speed, best accuracy */
    public static final String SPEED_SLOW              = "slow";

    /** Recognize  text */
    public static final String RECOGNIZE_TYPE_TEXT     = "text";
    /** Recognize barcode */
    public static final String RECOGNIZE_TYPE_BARCODE  = "barcode";
    /** Recognize both text and barcode */
    public static final String RECOGNIZE_TYPE_ALL      = "all";

    /** Output recognition result as plain text */
    public static final String OUTPUT_FORMAT_PLAINTEXT = "text";
    /** Output recognition result in XML format with additional information if coordination, confidence, runtime, etc. */
    public static final String OUTPUT_FORMAT_XML       = "xml";
    /** Output recognition result as searchable PDF */
    public static final String OUTPUT_FORMAT_PDF       = "pdf";
    /** Output to editable format RTF (can be edited in MS Word) */
    public static final String OUTPUT_FORMAT_RTF       = "rtf";

    /** Common used languages */
    /** eng (English) */
    public static final String LANGUAGE_ENG = "eng";
    /** spa (Spanish) */
    public static final String LANGUAGE_SPA = "spa";
    /** por (Portuguese) */
    public static final String LANGUAGE_POR = "por";
    /** deu (German) */
    public static final String LANGUAGE_DEU = "deu";
    /** fra (French) */
    public static final String LANGUAGE_FRA = "fra";
    // around 30 languages are supported - use their ISO 639 3-letter as the id

    // ------------------------ dictionary properties ------------------------

    /** set to 'true' to skip using the default built in dict. Default value: 'false' - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
    public static final String START_PROP_DICT_SKIP_BUILT_IN_DEFAULT = "START_PROP_DICT_SKIP_BUILT_IN_DEFAULT";

    /** set to 'true' to skip using all built-in dicts. Default value: 'false' - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
    public static final String START_PROP_DICT_SKIP_BUILT_IN_ALL = "START_PROP_DICT_SKIP_BUILT_IN_ALL";
    /** Path to your custom dictionary (words are separated using line breaks). Default value: null. - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
    public static final String START_PROP_DICT_CUSTOM_DICT_FILE = "START_PROP_DICT_CUSTOM_DICT_FILE";
    /** Path to your custom templates (templates are separated using line breaks). Default value: null. - can only be used for {@linkplain #startEngine(String, String, Object...)} */
    public static final String START_PROP_DICT_CUSTOM_TEMPLATES_FILE = "START_PROP_DICT_CUSTOM_TEMPLATES_FILE";

    /** Percentage measuring the importance of the dictionary (0: not at all; 100: extremely important; default: 10) */
    public static final String PROP_DICT_DICT_IMPORTANCE = "PROP_DICT_DICT_IMPORTANCE";

    // ------------------------ general options ------------------------

    /** Use this property to hint the OCR engine about page type. */
    public static final String PROP_PAGE_TYPE = "PROP_PAGE_TYPE";

    public static final String PROP_PAGE_TYPE_AUTO = "auto";
    public static final String PROP_PAGE_TYPE_SINGLE_BLOCK = "single_block";
    public static final String PROP_PAGE_TYPE_SINGLE_COLUMN = "single_column";
    public static final String PROP_PAGE_TYPE_SINGLE_LINE = "single_line";
    public static final String PROP_PAGE_TYPE_SINGLE_WORD = "single_word";
    public static final String PROP_PAGE_TYPE_SINGLE_CHAR = "single_char";
    public static final String PROP_PAGE_TYPE_SCATTERED = "scattered";

    /** Recognizes only the specified list of characters. */
    public static final String PROP_LIMIT_TO_CHARSET = "PROP_LIMIT_TO_CHARSET";

    /** Set to 'true' to set the output level as word instead of the default, line. */
    public static final String  PROP_OUTPUT_SEPARATE_WORDS = "PROP_OUTPUT_SEPARATE_WORDS";

    /** The DPI to be used to render the PDF file; default is 300 if not specified */
    public static final String PROP_INPUT_PDF_DPI = "PROP_INPUT_PDF_DPI";

    // ------------------------ Image pre-processing ------------------------

    /** Image pre-processing type */
    public static final String PROP_IMG_PREPROCESS_TYPE = "PROP_IMG_PREPROCESS_TYPE";
    /** Use system default */
    public static final String PROP_IMG_PREPROCESS_TYPE_DEFAULT = "default";
    /** Default + page orientation detection */
    public static final String PROP_IMG_PREPROCESS_TYPE_DEFAULT_WITH_ORIENTATION_DETECTION = "default_with_orientation_detection";
    /** Custom, need to set PROP_IMG_PREPROCESS_CUSTOM_CMDS */
    public static final String PROP_IMG_PREPROCESS_TYPE_CUSTOM = "custom";

    /** Custom mage pre-processing command */
    public static final String PROP_IMG_PREPROCESS_CUSTOM_CMDS = "PROP_IMG_PREPROCESS_CUSTOM_CMDS";

    // ------------------------ Table detection ------------------------

    /** table will be detected by default; set this property to true to skip detection. */
    public static final String PROP_TABLE_SKIP_DETECTION = "PROP_TABLE_SKIP_DETECTION";

    /** default is 31 if not specified */
    public static final String PROP_TABLE_MIN_SIDE_LENGTH = "PROP_TABLE_MIN_SIDE_LENGTH";

    /** Save intermediate images generated for debug purpose - don't specify or empty string to skip saving */
    public static final String PROP_SAVE_INTERMEDIATE_IMAGES_TO_DIR = "PROP_SAVE_INTERMEDIATE_IMAGES_TO_DIR";

    // ------------------------ PDF specific ------------------------

    /** PDF output file - required for PDF output. Valid prop value: absolute path to the target output file. */
    public static final String PROP_PDF_OUTPUT_FILE         = "PROP_PDF_OUTPUT_FILE";
    /** The DPI of the images or '0' to auto-detect. Optional. Valid prop value: 0(default: auto-detect), 300, 200, etc. */
    public static final String PROP_PDF_OUTPUT_IMAGE_DPI           = "PROP_PDF_OUTPUT_IMAGE_DPI";

    /** Font to be used for PDF output. Optional. Valid values: "serif" (default), "sans". */
    public static final String PROP_PDF_OUTPUT_FONT = "PROP_PDF_OUTPUT_FONT";

    /** Make text visible - for debugging and analysis purpose. Optional. Valid prop values false(default), true. */
    public static final String PROP_PDF_OUTPUT_TEXT_VISIBLE  = "PROP_PDF_OUTPUT_TEXT_VISIBLE";

    /** Convert images into black/white to reduce PDF output file size. Optional. Valid prop values: false(default), true.*/
    public static final String PROP_PDF_OUTPUT_IMAGE_FORCE_BW= "PROP_PDF_OUTPUT_IMAGE_FORCE_BW";

    /** Valid value: 0 ~ 100 - text recognized below or above confidence will be highlighted in different colors. */
    public static final String PROP_PDF_OUTPUT_CONF_THRESHOLD = "PROP_PDF_OUTPUT_CONF_THRESHOLD";

    /** Return text in 'text' or 'xml' format when the output format is set to PDF. */
    public static final String PROP_PDF_OUTPUT_RETURN_TEXT = "PROP_PDF_OUTPUT_RETURN_TEXT";
    public static final String PROP_PDF_OUTPUT_RETURN_TEXT_FORMAT_PLAINTEXT = "text";
    public static final String PROP_PDF_OUTPUT_RETURN_TEXT_FORMAT_XML = "xml";

    // ------------------------ RTF specific ------------------------
    /** RTF output file - required for RTF output. Valid prop value: absolute path to the target output file. */
    public static final String PROP_RTF_OUTPUT_FILE         = "PROP_RTF_OUTPUT_FILE";
    /** default is LETTER, may set to A4. */
    public static final String PROP_RTF_PAPER_SIZE           = "PROP_RTF_PAPER_SIZE";

    /** Return text in 'text' or 'xml' format when the output format is set to RTF. */
    public static final String PROP_RTF_OUTPUT_RETURN_TEXT = "PROP_RTF_OUTPUT_RETURN_TEXT";
    public static final String PROP_RTF_OUTPUT_RETURN_TEXT_FORMAT_PLAINTEXT = "text";
    public static final String PROP_RTF_OUTPUT_RETURN_TEXT_FORMAT_XML = "xml";

    /** Do not change unless you are told so. */
    public static String CONFIG_PROP_SEPARATOR = "|";
    /** Do not change unless you are told so. */
    public static String CONFIG_PROP_KEY_VALUE_SEPARATOR = "=";

    /** Recognize all pages. */
    public static final int PAGES_ALL = -1;

    /** Builder for configuring scan properties. */
    public static class PropertyBuilder extends Properties {

        /** set to 'true' to skip using the default built in dict. Default value: 'false' - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
        public PropertyBuilder setDictSkipBuiltInDefault(boolean skip) {
            setProperty(START_PROP_DICT_SKIP_BUILT_IN_DEFAULT, skip ? "true" : "false");
            return this;
        }

        /** set to 'true' to skip using all built-in dicts. Default value: 'false' - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
        public PropertyBuilder setDictSkipBuiltInAll(boolean skip) {
            setProperty(START_PROP_DICT_SKIP_BUILT_IN_ALL, skip ? "true" : "false");
            return this;
        }

        /** Path to your custom dictionary (words are separated using line breaks). Default value: null. - can only be used for {@linkplain #startEngine(String, String, Object...)}*/
        public PropertyBuilder setDictCustomDictFile(File customDictFile) {
            if(customDictFile == null) {
                remove(START_PROP_DICT_CUSTOM_DICT_FILE);
            } else {
                setProperty(START_PROP_DICT_CUSTOM_DICT_FILE, customDictFile.getAbsolutePath());
            }
            return this;
        }

        /** Path to your custom templates (templates are separated using line breaks). Default value: null. - can only be used for {@linkplain #startEngine(String, String, Object...)} */
        public PropertyBuilder setDictCustomTemplatesFile(File customTemplatesFile) {
            if(customTemplatesFile == null) {
                remove(START_PROP_DICT_CUSTOM_TEMPLATES_FILE);
            } else {
                setProperty(START_PROP_DICT_CUSTOM_TEMPLATES_FILE, customTemplatesFile.getAbsolutePath());
            }
            return this;
        }

        /** Percentage measuring the importance of the dictionary (0: not at all; 100: extremely important; default: 10) */
        public PropertyBuilder setDictImportance(int importance) {
            setProperty(PROP_DICT_DICT_IMPORTANCE, Integer.toString(importance));
            return this;
        }

        /*---- general recognition options -- */

        /** hints the page type to the OCR engine */
        public PropertyBuilder setPageType(PageType pageType) {
            setProperty(PROP_PAGE_TYPE, pageType.toString());
            return this;
        }

        /** hints the page type to the OCR engine */
        public PropertyBuilder setPageType(String pageType) {
            PageType type = PageType.parse(pageType);
            if(type == null) {
                throw new IllegalArgumentException();
            }
            setPageType(type);
            return this;
        }

        /** recognizes only certain characters */
        public PropertyBuilder setLimitToCharset(String chars) {
            setProperty(PROP_LIMIT_TO_CHARSET, chars);
            return this;
        }

        /** only recognizes '0123456789' */
        public PropertyBuilder setLimitToCharsetDigitsOnly() {
            setLimitToCharset("0123456789");
            return this;
        }

        /** whether to use word as the base unit for output; set to false to use the default unit: line. */
        public PropertyBuilder setOutputSeparateWords(boolean enable) {
            setProperty(PROP_OUTPUT_SEPARATE_WORDS, String.valueOf(enable));
            return this;
        }

        /** The DPI to be used to render the PDF file; default is 300 if not specified */
        public PropertyBuilder setInputPdfDpi(int dpi) {
            if(dpi <= 0) {
                throw new IllegalArgumentException("DPI must be greater than 0. Suggested values: 300, 350, 400.");
            }
            setProperty(PROP_INPUT_PDF_DPI, String.valueOf(dpi));
            return this;
        }

        // ------------------------ Image pre-processing ------------------------

        /** Sets the image pre-processing type */
        public PropertyBuilder setImagePreProcessingType(ImagePreProcessingType type) {
            setProperty(PROP_IMG_PREPROCESS_TYPE, type.toString());
            return this;
        }

        /** set the image pre-processing type to {@linkplain ImagePreProcessingType#CUSTOM} and the corresponding custom commands.*/
        public PropertyBuilder setCustomImagePreProcessing(String cmds) {
            setImagePreProcessingType(ImagePreProcessingType.CUSTOM);
            setProperty(PROP_IMG_PREPROCESS_CUSTOM_CMDS, cmds == null ? "" : cmds);
            return this;
        }

        // ------------------------ Table detection ------------------------

        /** Whether table detection should be skipped. */
        public PropertyBuilder setSkipTableDetection(boolean skipTableDetection) {
            setProperty(PROP_TABLE_SKIP_DETECTION, String.valueOf(skipTableDetection));
            return this;
        }

        /** Sets the min side length of a table cell when table detection is enabled.  */
        public PropertyBuilder setTableDetectionMinSideLength(int minSideLength) {
            setProperty(PROP_TABLE_MIN_SIDE_LENGTH, String.valueOf(minSideLength));
            return this;
        }

        /** Save intermediate images generated for debug purpose - don't specify or empty string to skip saving */
        public PropertyBuilder saveIntermediateImagesToDir(File dir) {
            setProperty(PROP_SAVE_INTERMEDIATE_IMAGES_TO_DIR, dir == null ? "" : dir.toString());
            return this;
        }

        // ------------------------ PDF output specific ------------------------

        /** Target pdf output file when output format is set to pdf */
        public PropertyBuilder setPdfOutputFile(File file) {
            setProperty(PROP_PDF_OUTPUT_FILE, file.getAbsolutePath());
            return this;
        }

        /** Target pdf output file when output format is set to pdf */
        public PropertyBuilder setPdfOutputFile(String filePath) {
            return setPdfOutputFile(new File(filePath));
        }

        /** true to set PDF output font to Sans, false to Serif (default). */
        public PropertyBuilder setPdfOutputFontSans(boolean enable) {
            setProperty(PROP_PDF_OUTPUT_FONT, enable ? "sans" : "");
            return this;
        }

        /** true to make text recognized visible on PDF or false for transparent (default). */
        public PropertyBuilder setPdfTextVisible(boolean enable) {
            setProperty(PROP_PDF_OUTPUT_TEXT_VISIBLE, String.valueOf(enable));
            return this;
        }

        /** true to convert image in PDF to black/white to save space false for keeping as is (default). */
        public PropertyBuilder setPdfImageForceBlackWhite(boolean enable) {
            setProperty(PROP_PDF_OUTPUT_IMAGE_FORCE_BW, String.valueOf(enable));
            return this;
        }

        /** Return plain text when the output format is set to PDF.*/
        public PropertyBuilder setPdfOutputReturnPlainText() {
            setProperty(PROP_PDF_OUTPUT_RETURN_TEXT, PROP_PDF_OUTPUT_RETURN_TEXT_FORMAT_PLAINTEXT);
            return this;
        }

        /** Return xml when the output format is set to PDF.*/
        public PropertyBuilder setPdfOutputReturnXml() {
            setProperty(PROP_PDF_OUTPUT_RETURN_TEXT, PROP_PDF_OUTPUT_RETURN_TEXT_FORMAT_XML);
            return this;
        }

        /** Return neither plain text nor xml when the output format is set to PDF.*/
        public PropertyBuilder setPdfOutputReturnNothing() {
            remove(PROP_PDF_OUTPUT_RETURN_TEXT);
            return this;
        }

        // ------------------------ RTF output specific ------------------------

        /** Target rtf output file when output format is set to rtf */
        public PropertyBuilder setRtfOutputFile(File file) {
            setProperty(PROP_RTF_OUTPUT_FILE, file.getAbsolutePath());
            return this;
        }

        /** Return plain text when the output format is set to RTF.*/
        public PropertyBuilder setRtfOutputReturnPlainText() {
            setProperty(PROP_RTF_OUTPUT_RETURN_TEXT, PROP_RTF_OUTPUT_RETURN_TEXT_FORMAT_PLAINTEXT);
            return this;
        }

        /** Return xml when the output format is set to rtf.*/
        public PropertyBuilder setRtfOutputReturnXml() {
            setProperty(PROP_RTF_OUTPUT_RETURN_TEXT, PROP_PDF_OUTPUT_RETURN_TEXT_FORMAT_XML);
            return this;
        }

        /** Return neither plain text nor xml when the output format is set to RTF.*/
        public PropertyBuilder setRtfOutputReturnNothing() {
            remove(PROP_RTF_OUTPUT_RETURN_TEXT);
            return this;
        }
    }

    /** Page type hint */
    public static enum PageType {AUTO, SINGLE_BLOCK, SINGLE_COLUMN, SINGLE_LINE, SINGLE_WORD, SINGLE_CHAR, SCATTERED;
        @Override
        public String toString() {
            switch (this) {
                case AUTO: return PROP_PAGE_TYPE_AUTO;
                case SINGLE_BLOCK: return PROP_PAGE_TYPE_SINGLE_BLOCK;
                case SINGLE_COLUMN: return PROP_PAGE_TYPE_SINGLE_COLUMN;
                case SINGLE_LINE: return PROP_PAGE_TYPE_SINGLE_LINE;
                case SINGLE_WORD: return PROP_PAGE_TYPE_SINGLE_WORD;
                case SINGLE_CHAR: return PROP_PAGE_TYPE_SINGLE_CHAR;
                case SCATTERED: return PROP_PAGE_TYPE_SCATTERED;
                default: return "!unknown";
            }
        }

        public static PageType parse(String s) {
            if(PROP_PAGE_TYPE_AUTO.equals(s)) {
                return AUTO;
            } else if(PROP_PAGE_TYPE_SINGLE_BLOCK.equals(s)) {
                return SINGLE_BLOCK;
            } else if(PROP_PAGE_TYPE_SINGLE_COLUMN.equals(s)) {
                return SINGLE_COLUMN;
            } else if(PROP_PAGE_TYPE_SINGLE_LINE.equals(s)) {
                return SINGLE_LINE;
            } else if(PROP_PAGE_TYPE_SINGLE_WORD.equals(s)) {
                return SINGLE_WORD;
            } else if(PROP_PAGE_TYPE_SINGLE_CHAR.equals(s)) {
                return SINGLE_CHAR;
            } else if(PROP_PAGE_TYPE_SCATTERED.equals(s)) {
                return SCATTERED;
            } else {
                throw new UnsupportedOperationException(s);
            }
        }
    };

    /** Image pre-processing type */
    public static enum ImagePreProcessingType {DEFAULT, DEFAULT_WITH_ORIENTATION_DETECTION, CUSTOM;
        @Override
        public String toString() {
            switch(this) {
                case DEFAULT: return PROP_IMG_PREPROCESS_TYPE_DEFAULT;
                case DEFAULT_WITH_ORIENTATION_DETECTION: return PROP_IMG_PREPROCESS_TYPE_DEFAULT_WITH_ORIENTATION_DETECTION;
                case CUSTOM: return PROP_IMG_PREPROCESS_TYPE_CUSTOM;
                default: return "!unknown";
            }
        }
    }

    static {
        OcrLibHelper.loadOcrLib();
    }

    private long handle;

    /**
     * The library version.
     * @return
     */
    public static String getLibraryVersion() {
        return doGetVersion();
    }

    /**
     * The library version.
     * @return
     */
    public static String getLibraryVersion(boolean verbose) {
        return doGetVersion() + System.getProperty("line.separator") + doGetBuildInfo();
    }

    /**
     * Whether one-time setup is required.
     * @return
     */
    public static boolean isSetupRequired() {
        return doSetup(true) != 1;
    }

    /**
     * Performs one-time setup; does nothing if setup has already been done.
     * @return
     */
    public static void setUp() {
        int result = doSetup(false);
        if(result != 1) {
            throw new OcrException("Failed to set up OCR. Error code: " + result);
        }
    }

    /** Returns all supported languages. */
    public static String[] listSupportedLanguages() {
        String s = doListSupportedLangs();
        if(s == null) {
            return new String[0];
        } else {
            String[] langs = StringUtils.split(s,",");
            if(langs != null && langs.length > 0) {
                Arrays.sort(langs);
            }
            return langs;
        }
    }

    /**
     * Starts the OCR engine with optional properties (e.g., to specify dictionary/templates file)
     * @param lang e.g., "eng" for English
     * @param speed valid values: {@linkplain #SPEED_FASTEST}, {@linkplain #SPEED_FAST}, {@linkplain #SPEED_SLOW}.
     * @param startPropSpec optional start properties, can be a single {@linkplain java.util.Properties} object or inline specification in pairs or a single string. Valid property names are defined in this class, etc.
     */
    public void startEngine(String lang, String speed, Object... startPropSpec) {
        if(handle > 0) {
            return;
        }

        if(StringUtils.isEmpty(lang)) {
            throw new IllegalArgumentException("Language is required.");
        }

        if(! (SPEED_FASTEST.equals(speed) || SPEED_FAST.equals(speed) || SPEED_SLOW.equals(speed))) {
            throw new IllegalArgumentException("Invalid speed: " + speed);
        }

        Properties props = readProperties(startPropSpec);

        String s = doStart(lang, speed, propsToString(props), CONFIG_PROP_SEPARATOR, CONFIG_PROP_KEY_VALUE_SEPARATOR);
        if(s != null) {
            throw new OcrException(s);
        }
    }

    /**
     * Stops the OCR engine; does nothing if it has already been stopped.
     */
    public void stopEngine() {
        if(handle > 0) {
            doStop();
        }
    }

    /**
     * Returns true only if the engine has been started and has not been stopped yet.
     * @return
     */
    public boolean isEngineRunning() {
        return handle > 0;
    }


    /**
     * Performs text/barcode recognition on the given files with the specified output format.
     * <p>Supported file formats:
     * <ul>
     *     <li>BMP</li>
     *     <li>JPEG</li>
     *     <li>PNG</li>
     *     <li>TIFF</li>
     *     <li>PNM</li>
     *     <li>GIF</li>
     * </ul>
     * </p>
     * @param sources input image files - can be local files or files on remote server
     * @param recognizeType valid values: {@linkplain #RECOGNIZE_TYPE_TEXT}, {@linkplain #RECOGNIZE_TYPE_BARCODE} or {@linkplain #RECOGNIZE_TYPE_ALL}.
     * @param outputFormat valid values: {@linkplain #OUTPUT_FORMAT_PLAINTEXT}, {@linkplain #OUTPUT_FORMAT_XML}, {@linkplain #OUTPUT_FORMAT_PDF} or {@linkplain #OUTPUT_FORMAT_RTF}.
     * @param propSpec additional properties, can be a single {@linkplain java.util.Properties} object or inline specification in pairs or a single string. Valid property names are defined in this class, etc.
     * @return the recognition output in the specified format or <pre>null</pre> if there is no input file.
     */
    public String recognize(URL[] sources, String recognizeType, String outputFormat, Object... propSpec) {
        File tmpDir= Utils.getTempFolderWritable();
        if(tmpDir == null) {
            throw new OcrException("Unable to find temporary dir");
        }

        List<File> files = new ArrayList<File>();
        for(int i = 0; sources != null && i < sources.length; i++) {
            File tmpFile = new File(tmpDir, "" + System.currentTimeMillis() + "-" + StringUtils.randomAlphanumeric(3));
            try {
                Utils.saveToFile(sources[i], tmpFile);
            } catch (Throwable e) {
                throw new OcrException(e);
            }
            files.add(tmpFile);
        }

        String s = recognize(files.toArray(new File[0]), recognizeType, outputFormat, propSpec);

        for(File file : files) {
            file.delete();
        }
        return s;
    }

    /**
     * Performs text/barcode recognition on the given files with the specified output format.
     * <p>Supported file formats:
     * <ul>
     *     <li>BMP</li>
     *     <li>JPEG</li>
     *     <li>PNG</li>
     *     <li>TIFF</li>
     *     <li>PNM</li>
     *     <li>GIF</li>
     * </ul>
     * </p>
     * @param files input image files - files must exist and file name can not contain ','
     * @param recognizeType valid values: {@linkplain #RECOGNIZE_TYPE_TEXT}, {@linkplain #RECOGNIZE_TYPE_BARCODE} or {@linkplain #RECOGNIZE_TYPE_ALL}.
     * @param outputFormat valid values: {@linkplain #OUTPUT_FORMAT_PLAINTEXT}, {@linkplain #OUTPUT_FORMAT_XML}, {@linkplain #OUTPUT_FORMAT_PDF} or {@linkplain #OUTPUT_FORMAT_RTF}.
     * @param propSpec additional properties, can be a single {@linkplain java.util.Properties} object or inline specification in pairs or a single string. Valid property names are defined in this class, etc.
     * @return the recognition output in the specified format or <pre>null</pre> if there is no input file.
     */
    public String recognize(File[] files, String recognizeType, String outputFormat, Object... propSpec) {
        String fs = filesToString(files);
        if (StringUtils.isEmpty(fs)) {
            return null;
        }

        String s = recognize(fs, -1, -1, -1, -1, -1, recognizeType, outputFormat, propSpec);
        return s;
    }

    /**
     * Performs text/barcode recognition on the given image with the specified output format.
     * @param img input image
     * @param recognizeType valid values: {@linkplain #RECOGNIZE_TYPE_TEXT}, {@linkplain #RECOGNIZE_TYPE_BARCODE} or {@linkplain #RECOGNIZE_TYPE_ALL}.
     * @param outputFormat valid values: {@linkplain #OUTPUT_FORMAT_PLAINTEXT}, {@linkplain #OUTPUT_FORMAT_XML}, {@linkplain #OUTPUT_FORMAT_PDF} or {@linkplain #OUTPUT_FORMAT_RTF}.
     * @param propSpec additional properties, can be a single {@linkplain java.util.Properties} object or inline specification in pairs or a single string. Valid property names are defined in this class, etc.
     * @return the recognition output in the specified format or <pre>null</pre> if there is no input file.
     */
    public String recognize(RenderedImage img, String recognizeType, String outputFormat, Object... propSpec) {
        if(img == null) {
            throw new IllegalArgumentException("img is null");
        }

        File fileImageTmp = null;
        try {
            fileImageTmp = File.createTempFile("ocr", ".png");
            ImageIO.write(img, "png", fileImageTmp);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to save image to " + fileImageTmp, t);
        }

        String s = recognize(fileImageTmp.getAbsolutePath(), -1, -1, -1, -1, -1, recognizeType, outputFormat, propSpec);
        return s;
    }


    /** Used to enforce single thread access to the OCR engine. */
    volatile Thread threadDoingOCR;

    /**
     * Performs OCR on the given input files.
     * @param files comma ',' separated image file path (JPEG, BMP, PNG, TIFF)
     * @param pageIndex -1 for all pages or the specified page (first page is 1) for multi-page image format like TIFF
     * @param startX -1 for whole page or the starting x coordinate of the specified region
     * @param startY -1 for whole page or the starting y coordinate of the specified region
     * @param width -1 for whole page or the width of the specified region
     * @param height -1 for whole page or the height of the specified region
     * @param recognizeType valid values: {@linkplain #RECOGNIZE_TYPE_TEXT}, {@linkplain #RECOGNIZE_TYPE_BARCODE} or {@linkplain #RECOGNIZE_TYPE_ALL}.
     * @param outputFormat valid values: {@linkplain #OUTPUT_FORMAT_PLAINTEXT}, {@linkplain #OUTPUT_FORMAT_XML}, {@linkplain #OUTPUT_FORMAT_PDF} or {@linkplain #OUTPUT_FORMAT_RTF}
     * @param propSpec additional properties, can be a single {@linkplain java.util.Properties} object or inline specification in pairs or a single string. Valid property names are defined in this class, etc.
     * @return text (plain text, xml) recognized for {@linkplain #OUTPUT_FORMAT_PLAINTEXT}, {@linkplain #OUTPUT_FORMAT_XML}; null for {@linkplain #OUTPUT_FORMAT_PDF}.
     */
    public String recognize(String files, int pageIndex, int startX, int startY, int width, int height, String recognizeType, String outputFormat,
        Object... propSpec) {
        if(threadDoingOCR != null) {
            throw new OcrException("Currently " + threadDoingOCR + " is using this OCR engine. Please create multiple OCR engine instances for multi-threading. ");
        }

        // process properties
        Properties props = readProperties(propSpec);

        try {
            threadDoingOCR = Thread.currentThread();
            // validation
            if(StringUtils.isEmpty(files)) {
                throw new IllegalArgumentException("files can not be empty!");
            }

            // PDF output
            String pdfOutputFile = props.getProperty(PROP_PDF_OUTPUT_FILE);
            if((OUTPUT_FORMAT_PDF.equals(outputFormat)) && StringUtils.isEmpty(pdfOutputFile)) {
                throw new IllegalArgumentException("You must specify PDF output through property named: " + PROP_PDF_OUTPUT_FILE);
            }

            return doRecognize(files, pageIndex, startX, startY, width, height, recognizeType, outputFormat, propsToString(props), CONFIG_PROP_SEPARATOR, CONFIG_PROP_KEY_VALUE_SEPARATOR);
        } finally {
            threadDoingOCR = null;
        }
    }

    protected static Properties readProperties(Object[] propSpec) {
        Properties props = new Properties();
        if(propSpec == null || propSpec.length == 0 || (propSpec.length == 1 && propSpec[0] == null)) {
            // nothing to do.
        } else if(propSpec.length == 1 && propSpec[0] instanceof String) {
            // parse properties
            props = stringToProps((String)propSpec[0]);
        } else if(propSpec.length > 0 && (propSpec[0] instanceof Properties)) {
            props = (Properties) propSpec[0];
        } else {
            if (propSpec.length % 2 == 1) {
                throw new IllegalArgumentException("Property specification must come in pairs: " + Arrays.toString(propSpec));
            }
            for (int p = 0; p < propSpec.length; p += 2) {
                Object key = propSpec[p];
                Object value = propSpec[p + 1];
                String valueAsString = null;
                if(value == null) {
                    valueAsString = "";
                } else if(value instanceof File) {
                    valueAsString = ((File)value).getAbsolutePath();
                } else {
                    valueAsString = String.valueOf(value);
                }
                props.setProperty(String.valueOf(key), valueAsString);
            }
        }

        // validation.
        for(Object key : props.keySet()) {
            Object value = props.get(key);
            if(key == null || value == null) {
                throw new IllegalArgumentException("Neither key or value of a property can be null: " + key + "=" + value);
            }

            if(key.toString().contains(CONFIG_PROP_KEY_VALUE_SEPARATOR)) {
                throw new IllegalArgumentException("Please change CONFIG_PROP_KEY_VALUE_SEPARATOR to a different value as \"" +
                        key + "\" contains \"" + CONFIG_PROP_KEY_VALUE_SEPARATOR + "\"");
            }

            if(value.toString().contains(CONFIG_PROP_SEPARATOR)) {
                throw new IllegalArgumentException("Please change CONFIG_PROP_SEPARATOR to a different value as \"" +
                        value + "\" contains \"" + CONFIG_PROP_SEPARATOR + "\"");
            }
        }
        return props;
    }

    private static Properties stringToProps(String spec) {
        Properties props = new Properties();
        StringTokenizer stProps = new StringTokenizer(spec, CONFIG_PROP_SEPARATOR);
        while(stProps.hasMoreTokens()) {
            String tokenProp = stProps.nextToken();
            if(tokenProp == null || tokenProp.trim().length() == 0) {
                continue;
            }
            StringTokenizer stKeyVal = new StringTokenizer(tokenProp, CONFIG_PROP_KEY_VALUE_SEPARATOR);
            if(stKeyVal.countTokens() == 2) {
                props.setProperty(stKeyVal.nextToken().trim(), stKeyVal.nextToken().trim());
            }
        }
        return props;
    }

    public static String propsToString(Properties props) {
        StringBuilder sb = new StringBuilder();
        for(Object key : props.keySet()) {
            if(sb.length() > 0) {
                sb.append(CONFIG_PROP_SEPARATOR);
            }
            sb.append(String.valueOf(key));
            sb.append(CONFIG_PROP_KEY_VALUE_SEPARATOR);
            sb.append(String.valueOf(props.get(key)));
        }

        return sb.toString();
    }

    private static String filesToString(File[] files) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; files != null && i < files.length; i++) {
            File file = files[i];
            if(! file.exists()) {
                System.err.println("File does not exist: " + file.getAbsolutePath());
                continue;
            }
            if(file.getAbsolutePath().contains(",")) {
                throw new OcrException("Comma is not allowed in file name: " + file.getAbsolutePath());
            }

            if(sb.length() > 0) {
                sb.append(",");
            }
            sb.append(file.getAbsolutePath());
        }

        return sb.toString();
    }

    private native static String doGetVersion();

    private native static String doGetBuildInfo();

    private native static int doSetup(boolean queryOnly);

    private native static String doListSupportedLangs();

    private native String doStart(String lang, String speed, String properties, String propSeparator, String propNameValueSeparator);

    private native String doRecognize(String file, int pageIndex, int startX, int startY, int width, int height, String recognizeType, String outputFormat, String properties, String propSeparator, String propNameValueSeparator);

    private native String doStop();

    /** Saves aocr.xsl to the specified directory  */
    public static boolean saveAocrXslToDir(File dir, boolean overwrite) {
        if(dir == null) {
            throw new IllegalArgumentException();
        }

        File file = new File(dir, "aocr.xsl");
        if(file.exists() && !overwrite) {
            return false;
        }

        file.getParentFile().mkdirs();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = Ocr.class.getResourceAsStream("/aocr.xsl");
            if(inputStream == null) {
                return false;
            }
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                outputStream.write(buffer, 0, len);
                len = inputStream.read(buffer);
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    /** -1 unknow / 0 no / 1 yes */
    public static int getConsoleMode() {
        try {
            Class.forName("java.io.Console");
            return System.console() != null ? 1 : 0;
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * Displays the library version and optional performs OCR on the input file.
     * Usage: <pre>Usage: java -jar aocr.jar INPUT_FILE [text|xml|pdf]</pre>
     * @param args
     */
    public static void main(String[] args) {
        if(!(args.length > 0 && "console".equalsIgnoreCase(args[args.length - 1])) && !java.awt.GraphicsEnvironment.isHeadless()) {
            FrameOcrSample.main(args);
            return;
        }
        String copyright = "Copyright Asprise, " + Calendar.getInstance().get(Calendar.YEAR) + ". All Rights Reserved. Visit www.asprise.com";
        String version = "Library version: " + getLibraryVersion();
        int consoleMode = getConsoleMode();
        if (consoleMode != 1) { // double click
            JOptionPane.showMessageDialog(null, version, copyright, JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
        }

        if (consoleMode != 0 || args.length > 0) { // command line
            try {
                System.out.println(copyright);
                System.out.println(version);
                System.out.println(Utils.getEnvInfo(false));
                setUp();
                System.out.println("Languages supported: " + Arrays.toString(listSupportedLanguages()));

                String outputFormat = OUTPUT_FORMAT_PLAINTEXT;
                if (args.length >= 2 && args[1] != null) {
                    if ("xml".equalsIgnoreCase(args[1].trim())) {
                        outputFormat = OUTPUT_FORMAT_XML;
                    } else if ("pdf".equalsIgnoreCase(args[1].trim())) {
                        outputFormat = OUTPUT_FORMAT_PDF;
                    } else {
                        if (!"text".equalsIgnoreCase(args[1].trim())) {
                            System.out.println("Invalid output format: " + args[1] + ", will use plain text instead. ");
                        }
                    }
                }

                String recognizeType = RECOGNIZE_TYPE_ALL;
                if(args.length >= 3 && args[2] != null) {
                    if ("barcode".equalsIgnoreCase(args[2].trim())) {
                        recognizeType = RECOGNIZE_TYPE_ALL;
                    } else if ("text".equalsIgnoreCase(args[2].trim())) {
                        recognizeType = RECOGNIZE_TYPE_TEXT;
                    } else {
                        if (!"all".equalsIgnoreCase(args[2].trim())) {
                            System.out.println("Invalid recognize type: " + args[2] + ", will use all instead. ");
                        }
                    }
                }

                String lang = "eng";
                if(args.length >= 4 && args[3] != null) {
                    lang = args[3].trim();
                }

                // make sure lang is supported.
                String[] langs = listSupportedLanguages();
                Arrays.sort(langs);
                if(Arrays.binarySearch(langs, lang) < 0) {
                    throw new IllegalArgumentException("'" + lang + "' is not in supported language list: " + Arrays.toString(langs));
                }

                if (args.length == 0) { // show usage.
                    String usage = "Usage: java -jar aocr.jar INPUT_FILE [text|xml|pdf] [all|text|barcode] [eng|fra|micr|...]";
                    System.out.println(usage);
                } else { // recognize
                    File inputFile = new File(args[0]);
                    if(! inputFile.exists() && inputFile.isFile()) {
                        throw new IOException("File does not exist or unable to read: " + inputFile.getAbsolutePath());
                    }
                    int lastDot = inputFile.getName().lastIndexOf('.');

                    File pdfOutputFile = null;

                    setUp();
                    Ocr ocr = new Ocr();
                    ocr.startEngine(lang, SPEED_FASTEST);
                    long timeStart = System.currentTimeMillis();
                    Properties props = new Properties();
                    if(outputFormat == OUTPUT_FORMAT_PDF) {
                        pdfOutputFile = new File("output-" + inputFile.getName() + "-" + System.currentTimeMillis() + ".pdf");
                        props.put(PROP_PDF_OUTPUT_FILE, pdfOutputFile.getAbsolutePath());
                        props.put(PROP_PDF_OUTPUT_TEXT_VISIBLE, true);
                    }

                    if(Utils.isLaptop() == 1) {
                        System.out.println("NOTICE: You are using a portable computer. It could result long execution time due to low processor power.");
                    }
                    System.out.println("OCR in progress, please stand by ...");

                    String s = null;
                    s = ocr.recognize(new File[]{inputFile}, recognizeType, outputFormat, props);

                    long timeTaken = System.currentTimeMillis() - timeStart;
                    ocr.stopEngine();
                    System.out.println("--- RESULT ---");
                    if(pdfOutputFile != null) {
                        System.out.println("PDF output has been written to the following file: \n" + pdfOutputFile.getAbsolutePath());
                        System.out.println("For illustration purpose, text has been rendered in color instead of transparent.");

                        if(pdfOutputFile.exists() && pdfOutputFile.length() > 0) {
                            if(Utils.isWindows()) {
                                try {
                                    Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + pdfOutputFile.getAbsolutePath());
                                } catch (Throwable t) {
                                    // ignore
                                }
                            }
                        }
                    } else {
                        System.out.println(s);
                    }
                    System.out.println("--- Time taken (excluding engine startup time): " + (timeTaken / 1000.0) + "s ---");
                }

            } catch (Throwable e) { // System.console() is only available for Java 1.6 and later
                e.printStackTrace();
            }
        }
    }

}