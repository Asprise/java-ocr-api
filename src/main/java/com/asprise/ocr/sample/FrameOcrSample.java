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

package com.asprise.ocr.sample;

import com.asprise.ocr.Ocr;
import com.asprise.ocr.sample.util.AdditionalPaintable;
import com.asprise.ocr.util.StringUtils;
import com.asprise.ocr.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.DatatypeConverter;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class FrameOcrSample extends javax.swing.JFrame {

    static {
        DemoUtils.setPreferencesWithXmlBackstoreOnWindows();

    }

    ExecutorService ocrExecutor = Executors.newSingleThreadExecutor();
    AtomicInteger executorQueueSize = new AtomicInteger(0);

    PanelOcrInput panelOcrInput;

    Ocr ocr;
    String currentLang;
    String currentPropsStart;

    void init() {

        panelOcrInput = new PanelOcrInput();
        panelTop.setLayout(new BorderLayout());
        panelTop.add(panelOcrInput, BorderLayout.CENTER);

        textLogging.setAdditionalPaint(new AdditionalPaintable() {
            @Override
            public void additionalPaint(JComponent component, Graphics2D g2d, int width, int height) {
                BufferedImage logo = getEvalImg();
                g2d.drawImage(logo, width - logo.getWidth() - 20, height - logo.getHeight() - 20, null);
            }
        });

        executorQueueSize.incrementAndGet();
        ocrExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("ocr-t1");
                    final String version = Ocr.getLibraryVersion();
                    log(version);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            FrameOcrSample.this.setTitle(version);
                        }
                    });

                    Ocr.setUp();

                    panelOcrInput.setOcrLangs(Ocr.listSupportedLanguages());
                    String lang = panelOcrInput.getLanguage();
                    ocr = new Ocr();
                    if(isLanguageSupported(lang)) {
                        String propsStart = panelOcrInput.getPropsStart();
                        ocr.startEngine(lang, Ocr.SPEED_FASTEST, propsStart);
                        currentLang = lang;
                        currentPropsStart = propsStart;
                    }
                } catch (Throwable t) {
                    log("Failed to start OCR engine - please contact support: support@asprise.com", t);
                } finally {
                    executorQueueSize.decrementAndGet();
                }
            }
        });

        panelOcrInput.getButtonOcr().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(executorQueueSize.get() > 0) {
                    showMessage("OCR activity in progress, please wait ...", false);
                    return;
                }
                executorQueueSize.incrementAndGet();
                ocrExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            performOcr();
                        } finally {
                            executorQueueSize.decrementAndGet();
                        }
                    }
                });
            }
        });

        DemoUtils.enableMenu(textLogging);
        setIconImages(DemoUtils.getApplicationIconsLogo());
        try {
            linkLabel.setup("Questions? We are here to help: asprise.com", new URI("http://asprise.com/royalty-free-library/java-ocr-api-overview.html?src=demo_java"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    
    void performOcr() {
        try {
            String files = panelOcrInput.getFileImage();
            if (! panelOcrInput.isImageFileOk()) {
                String errorString = (files == null || files.toString().trim().isEmpty() ? "" : "File(s) not readable: " + files + "\n") + "Please browse an image file.";
                log(errorString);
                showMessage(errorString, true);
                return;
            }

            String language = panelOcrInput.getLanguage();
            String propsStart = panelOcrInput.getPropsStart();
            String propsRecognize = panelOcrInput.getPropsRecognition();
            if(! isLanguageSupported(language)) {
                String errorString = "Language '" + language + "' is not available in this trial. Please contact support@asprise.com";
                log(errorString);
                showMessage(errorString, true);
                return;
            }

            Ocr.PropertyBuilder propertyBuilder = new Ocr.PropertyBuilder()
                    .setPageType(panelOcrInput.getTextLayout())
                    .setSkipTableDetection(!panelOcrInput.isDataCaptureChecked())
                    .setImagePreProcessingType(panelOcrInput.isAutoRotatePagesChecked() ? Ocr.ImagePreProcessingType.DEFAULT_WITH_ORIENTATION_DETECTION : Ocr.ImagePreProcessingType.DEFAULT)
                    .setOutputSeparateWords(panelOcrInput.isWordLevelChecked())
                    .setPdfTextVisible(panelOcrInput.isPdfHighlightTextChecked());

            if(ocr.isEngineRunning() &&
                    ((!StringUtils.equals(currentLang, language, true)) || (!StringUtils.equals(currentPropsStart, propsStart, true))) ) {
                ocr.stopEngine();
                ocr = new Ocr(); 
            }

            if(! ocr.isEngineRunning()) {
                ocr.startEngine(language, Ocr.SPEED_FASTEST, propsStart);
                currentLang = language;
                currentPropsStart = propsStart;
            }

            String recognizeType = panelOcrInput.getRecognizeType().toLowerCase().contains("text") ?
                    (panelOcrInput.getRecognizeType().toLowerCase().contains("barcode") ? Ocr.RECOGNIZE_TYPE_ALL : Ocr.RECOGNIZE_TYPE_TEXT) : Ocr.RECOGNIZE_TYPE_BARCODE;
            String outputFormat = panelOcrInput.getOutputFormat();

            File outputFile = new File("asprise-ocr-" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_SSS").format(new Date()) + "."
            + (outputFormat.equals(Ocr.OUTPUT_FORMAT_PLAINTEXT) ? "txt" : outputFormat));

            if(outputFormat.equals(Ocr.OUTPUT_FORMAT_PDF)) {
                propertyBuilder.setPdfOutputFile(outputFile).setPdfImageForceBlackWhite(true).setOutputSeparateWords(true);
            }
            if(outputFormat.equals(Ocr.OUTPUT_FORMAT_RTF)) {
                propertyBuilder.setRtfOutputFile(outputFile).setOutputSeparateWords(false);
            }

            String allRecogProps = Ocr.propsToString(propertyBuilder) +
                    (StringUtils.isEmpty(propsRecognize) ? "" : Ocr.CONFIG_PROP_SEPARATOR + propsRecognize);
            String status = "Recognizing " + recognizeType + " to output as " + outputFormat + " on image: " + files + " ...\n" +
                "OCR engine start props: " + currentPropsStart + "\n" +
                "OCR recognition props:  " + allRecogProps + "\n" +
                "Please standby ... " + (Utils.isWindows() ? "" : "Trial version on Unix: q, x, 0, and 9 will be replaced with *")   ;

            log(status);
            showText(status, false);

            String s = ocr.recognize(files, Ocr.PAGES_ALL, -1, -1, -1, -1, recognizeType, outputFormat, allRecogProps);
            if(Ocr.OUTPUT_FORMAT_PLAINTEXT.equals(outputFormat)) {
                showText(s, false);
            } else if(Ocr.OUTPUT_FORMAT_XML.equals(outputFormat)) {
                writeStringToFile(s, outputFile);
                showText("You may view the XML file using IE, Firefox or Safari: " + outputFile.getAbsolutePath(), false);
                showText(s, true);
                Ocr.saveAocrXslToDir(outputFile.getAbsoluteFile().getParentFile(), false);
                try {
                    Desktop.getDesktop().browse(outputFile.getAbsoluteFile().toURI());
                } catch (Throwable t) {
                    Desktop.getDesktop().open(outputFile);
                }
            } else if(Ocr.OUTPUT_FORMAT_PDF.equals(outputFormat)) {
                showText("PDF file has been generated: " + outputFile.getAbsolutePath(), false);
                Desktop.getDesktop().open(outputFile);
            } else if(Ocr.OUTPUT_FORMAT_RTF.equals(outputFormat)) {
                showText("RTF file has been generated: " + outputFile.getAbsolutePath(), false);
                Desktop.getDesktop().open(outputFile);
            } else {
                showText(s, false);
            }
            panelOcrInput.savePrefs();
            log("OCR completed.");

        } catch (Throwable t) {
            log(null, t);
        }
    }

    void stopOcr() {
        try {
            if (ocr != null && ocr.isEngineRunning()) {
                ocr.stopEngine();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    static void writeStringToFile(String s, File f) throws IOException {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), Charset.forName("UTF-8")));
            writer.write(s);
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    boolean isLanguageSupported(String lang) {
        return new HashSet<String>(Arrays.asList(Ocr.listSupportedLanguages())).contains(lang);
    }

    void showText(final String text, final boolean append) {
        if(! SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showText(text, append);
                }
            });
            return;
        }
        String textProcessed = text == null ? "(null)" : text;
        if(!append) {
            textLogging.setText(textProcessed);
        } else {
            textLogging.append(textLogging.getText().length() == 0 ? textProcessed : "\n" + textProcessed);
        }

        String content = textLogging.getText();
        if(content.length() > 2) {
            int lastLinePos = text.lastIndexOf("\n", content.length() - 2);
            if(lastLinePos > 0) {
                textLogging.setCaretPosition(lastLinePos + 1); 
            }
        }
    }

    void log(String mesg) {
        log(mesg, null);
    }

    void log(String mesg, Throwable t) {
        if(mesg != null) {
            panelLogging.log(mesg);
        }
        if(t != null) {
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            panelLogging.log(writer.toString());
        }
    }

    void showMessage(final String mesg, final boolean isError) {
        if(! SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showMessage(mesg, isError);
                }
            });
            return;
        }
        JOptionPane.showMessageDialog(FrameOcrSample.this, mesg, "", isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }


    void setSplitPaneDividerLocation(final int location) {
        if(! SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setSplitPaneDividerLocation(location);
                }
            });
            return;
        }
        splitPane.setDividerLocation(location);
    }

    
    public FrameOcrSample() {
        initComponents();
        init();
    }

    static BufferedImage evalImg;
    static BufferedImage getEvalImg() {
        if(evalImg != null) {
            return evalImg;
        }
        try {
            byte[] bytes = DatatypeConverter.parseBase64Binary("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");
            evalImg = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return evalImg;
    }

    
    @SuppressWarnings("unchecked")

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelTop = new javax.swing.JPanel();
        panelBottom = new javax.swing.JPanel();
        linkLabel = new com.asprise.ocr.sample.util.LinkLabel();
        splitPane = new javax.swing.JSplitPane();
        scrollPaneCenter = new javax.swing.JScrollPane();
        textLogging = new com.asprise.ocr.sample.util.JTextAreaAdditionalPaintable();
        panelLogging = new com.asprise.ocr.sample.PanelLogging();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panelTop.setBorder(javax.swing.BorderFactory.createTitledBorder("OCR"));

        javax.swing.GroupLayout panelTopLayout = new javax.swing.GroupLayout(panelTop);
        panelTop.setLayout(panelTopLayout);
        panelTopLayout.setHorizontalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 617, Short.MAX_VALUE)
        );
        panelTopLayout.setVerticalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        getContentPane().add(panelTop, gridBagConstraints);

        panelBottom.setMinimumSize(new java.awt.Dimension(20, 20));
        panelBottom.setPreferredSize(new java.awt.Dimension(20, 20));
        panelBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        linkLabel.setMinimumSize(new java.awt.Dimension(10, 10));
        panelBottom.add(linkLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 2, 10);
        getContentPane().add(panelBottom, gridBagConstraints);

        splitPane.setDividerLocation(200);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(1.0);

        scrollPaneCenter.setPreferredSize(new java.awt.Dimension(100, 400));

        textLogging.setColumns(20);
        textLogging.setRows(5);
        scrollPaneCenter.setViewportView(textLogging);

        splitPane.setLeftComponent(scrollPaneCenter);

        panelLogging.setPreferredSize(new java.awt.Dimension(229, 100));
        splitPane.setRightComponent(panelLogging);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        getContentPane().add(splitPane, gridBagConstraints);

        pack();
    }

    
    public static void main(String args[]) {
        DemoUtils.fixPrefsWarning();

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameOcrSample.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameOcrSample.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameOcrSample.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameOcrSample.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        DemoUtils.fixPrefsWarning();
        DemoUtils.autoAwesomeLookAndFeel(null, null);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final FrameOcrSample frame = new FrameOcrSample();
                frame.pack();

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension dimension = frame.getSize();
                frame.setSize(dimension.width < 900 ? Math.min(900, screenSize.width * 80) : dimension.width,
                        dimension.height < 700 ? (int) Math.min(700, screenSize.height * .80) : dimension.height);
                DemoUtils.showWindowBestSizeAndPosition(frame);

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        frame.stopOcr();
                        System.exit(0);
                    }
                });

                frame.setSplitPaneDividerLocation(frame.splitPane.getHeight() - 120);

                frame.setVisible(true);
            }
        });
    }


    private com.asprise.ocr.sample.util.LinkLabel linkLabel;
    private javax.swing.JPanel panelBottom;
    private com.asprise.ocr.sample.PanelLogging panelLogging;
    private javax.swing.JPanel panelTop;
    private javax.swing.JScrollPane scrollPaneCenter;
    private javax.swing.JSplitPane splitPane;
    private com.asprise.ocr.sample.util.JTextAreaAdditionalPaintable textLogging;

}
