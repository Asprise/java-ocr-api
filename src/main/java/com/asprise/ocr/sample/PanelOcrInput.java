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

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;


public class PanelOcrInput extends javax.swing.JPanel {
    static Preferences prefs = Preferences.userRoot().node(PanelOcrInput.class.getName());
    static String PREF_FILE_IMG = "file-img";
    static String PREF_TEXT_LAYOUT = "text-layout";
    static String PREF_DATA_CAPTURE_BOOLEAN = "data-capture";
    static String PREF_AUTO_ROTATE_BOOLEAN = "auto-rotate";
    static String PREF_WORD_LEVEL = "word-level";
    static String PREF_OUTPUT_FORMAT = "output-format";
    static String PREF_PDF_HIGHLIGHT_TEXT = "pdf-highlight-text";
    static String PREF_LANGUAGE = "lang";
    static String PREF_RECOGNIZE_TYPE = "recognize-type";

    static String PREF_PROPS_START = "props-start";
    static String PREF_PROPS_RECOGNITION = "props-recognition";

    ButtonGroup buttonGroupOutputFormat = new ButtonGroup();
    
    void init() {

        comboTextLayout.setModel(new DefaultComboBoxModel(new String[]{
                "auto", "single_block", "single_column", "single_line", "single_word", "single_char", "scattered"
        }));
        comboRecognizeType.setModel(new DefaultComboBoxModel(new String[] {
                "Text + Barcodes", "Text only", "Barcodes only"
        }));
        comboLanguage.setModel(new DefaultComboBoxModel(new Object[0]));

        ActionListener actionListenerForOutputFormatRadios = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUI();
            }
        };
        radioOutputFormatPlainText.addActionListener(actionListenerForOutputFormatRadios);
        radioOutputFormatXml.addActionListener(actionListenerForOutputFormatRadios);
        radioOutputFormatPdf.addActionListener(actionListenerForOutputFormatRadios);
        radioOutputFormatRtf.addActionListener(actionListenerForOutputFormatRadios);

        buttonGroupOutputFormat.add(radioOutputFormatPlainText);
        buttonGroupOutputFormat.add(radioOutputFormatXml);
        buttonGroupOutputFormat.add(radioOutputFormatPdf);
        buttonGroupOutputFormat.add(radioOutputFormatRtf);
        radioOutputFormatXml.setSelected(true);

        comboFileImage.setEditable(true);
        DemoUtils.registerBrowseButtonListener(comboFileImage, buttonBrowseFile, true, true, DemoUtils.getFileFilterForExtensions(
                new String[] {"bmp", "gif", "jpg", "jpeg", "pdf", "png", "tif", "tiff"}, true
        ), null);

        comboPropsStart.setEditable(true);
        comboPropsRecognition.setEditable(true);

        DemoUtils.loadPrefs(prefs, PREF_FILE_IMG, comboFileImage);
        DemoUtils.loadPrefs(prefs, PREF_PROPS_START, comboPropsStart);
        DemoUtils.loadPrefs(prefs, PREF_PROPS_RECOGNITION, comboPropsRecognition);

        selectCombo(comboTextLayout, prefs.get(PREF_TEXT_LAYOUT, "auto"));
        selectCombo(comboRecognizeType, prefs.get(PREF_RECOGNIZE_TYPE, "Text + Barcodes"));
        checkDataCapture.setSelected(prefs.getBoolean(PREF_DATA_CAPTURE_BOOLEAN, true));
        checkAutoRotatePages.setSelected(prefs.getBoolean(PREF_AUTO_ROTATE_BOOLEAN, false));
        checkWordLevel.setSelected(prefs.getBoolean(PREF_WORD_LEVEL, false));
        radioOutputFormatPlainText.setSelected(prefs.get(PREF_OUTPUT_FORMAT, "").toLowerCase().contains("text"));

        radioOutputFormatPdf.setSelected(prefs.get(PREF_OUTPUT_FORMAT, "").toLowerCase().contains("pdf"));
        radioOutputFormatRtf.setSelected(prefs.get(PREF_OUTPUT_FORMAT, "").toLowerCase().contains("rtf"));

        checkPdfHighlightText.setSelected(prefs.getBoolean(PREF_PDF_HIGHLIGHT_TEXT, true));

        comboLanguage.setPreferredSize(new Dimension(90, comboLanguage.getPreferredSize().height));

        try {
            linkLabelHelp.setup("Help", new URI("http://asprise.com/ocr/docs/html/asprise-ocr-sdk-api-options.html?src=demo_java"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        refreshUI();
    }

    public String getFileImage() {
        return DemoUtils.getText(comboFileImage);
    }

    public boolean isImageFileOk() {
        File file = null;
        String files = getFileImage();
        if(files.contains(",")) {
            StringTokenizer st = new StringTokenizer(files, ",");
            while(st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                file = new File(token);
                break;
            }
        } else {
            file = new File(files.trim());
        }
        return file != null && file.exists() && file.isFile();
    }

    public String getPropsStart() {
        return DemoUtils.getText(comboPropsStart);
    }

    public String getPropsRecognition() {
        return DemoUtils.getText(comboPropsRecognition);
    }

    public String getTextLayout() {
        return comboTextLayout.getSelectedItem().toString();
    }

    public boolean isDataCaptureChecked() {
        return checkDataCapture.isSelected();
    }

    public boolean isAutoRotatePagesChecked() {
        return checkAutoRotatePages.isSelected();
    }

    public boolean isWordLevelChecked() {
        return checkWordLevel.isSelected();
    }

    public String getOutputFormat() {
        return radioOutputFormatPlainText.isSelected() ? "text" :
                radioOutputFormatXml.isSelected() ? "xml" : (radioOutputFormatRtf.isSelected() ? "rtf" : "pdf");
    }

    public boolean isPdfHighlightTextChecked() {
        return checkPdfHighlightText.isSelected();
    }

    public String getRecognizeType() {
        return comboRecognizeType.getSelectedItem().toString();
    }

    
    public String getLanguage() {
        String lang = comboLanguage.getSelectedItem() == null ? null : comboLanguage.getSelectedItem().toString();
        return lang == null || lang.trim().isEmpty() ? prefs.get(PREF_LANGUAGE, "eng") : lang;
    }

    public JButton getButtonOcr() {
        return buttonOcr;
    }

    public void savePrefs() {
        try {
            prefs.put(PREF_TEXT_LAYOUT, comboTextLayout.getSelectedItem().toString());
            prefs.put(PREF_RECOGNIZE_TYPE, comboRecognizeType.getSelectedItem().toString());
            prefs.putBoolean(PREF_DATA_CAPTURE_BOOLEAN, checkDataCapture.isSelected());
            prefs.putBoolean(PREF_AUTO_ROTATE_BOOLEAN, checkAutoRotatePages.isSelected());
            prefs.putBoolean(PREF_WORD_LEVEL, checkWordLevel.isSelected());
            prefs.put(PREF_OUTPUT_FORMAT, getOutputFormat());
            prefs.putBoolean(PREF_PDF_HIGHLIGHT_TEXT, checkPdfHighlightText.isSelected());
            prefs.put(PREF_LANGUAGE, comboLanguage.getSelectedItem().toString());
            DemoUtils.savePrefs(prefs, PREF_FILE_IMG, comboFileImage, getFileImage()); 
            DemoUtils.savePrefs(prefs, PREF_PROPS_START, comboPropsStart, getPropsStart()); 
            DemoUtils.savePrefs(prefs, PREF_PROPS_RECOGNITION, comboPropsRecognition, getPropsRecognition()); 
        } catch (Throwable t) {

        }
    }

    public void setOcrLangs(String[] langs) {
        comboLanguage.setModel(new DefaultComboBoxModel(langs));
        selectCombo(comboLanguage, prefs.get(PREF_LANGUAGE, "eng"));
    }

    static void selectCombo(JComboBox comboBox, String value) {
        if(value == null || value.trim().length() == 0) {
            return;
        }
        ComboBoxModel model = comboBox.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            if(value.equals(model.getElementAt(i).toString())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    void refreshUI() {
        checkPdfHighlightText.setEnabled(radioOutputFormatPdf.isSelected());
    }

    
    public PanelOcrInput() {
        initComponents();
        init();
    }

    public static void main(String[] args) {
        DemoUtils.autoAwesomeLookAndFeel(null, null);
        JFrame frame = new JFrame("JFrame Source Demo");

        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.getContentPane().add(new PanelOcrInput(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    
    @SuppressWarnings("unchecked")

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        comboFileImage = new javax.swing.JComboBox();
        buttonBrowseFile = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        comboTextLayout = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        radioOutputFormatPlainText = new javax.swing.JRadioButton();
        radioOutputFormatXml = new javax.swing.JRadioButton();
        radioOutputFormatPdf = new javax.swing.JRadioButton();
        checkPdfHighlightText = new javax.swing.JCheckBox();
        checkDataCapture = new javax.swing.JCheckBox();
        checkWordLevel = new javax.swing.JCheckBox();
        checkAutoRotatePages = new javax.swing.JCheckBox();
        buttonOcr = new javax.swing.JButton();
        comboLanguage = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        comboRecognizeType = new javax.swing.JComboBox();
        radioOutputFormatRtf = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        comboPropsStart = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        comboPropsRecognition = new javax.swing.JComboBox();
        linkLabelHelp = new com.asprise.ocr.sample.util.LinkLabel();

        jLabel1.setText("Image:");

        comboFileImage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonBrowseFile.setText("Browse ...");

        jLabel2.setText("Text layout:");

        comboTextLayout.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Output format:");

        radioOutputFormatPlainText.setText("Plain text");

        radioOutputFormatXml.setText("Xml");

        radioOutputFormatPdf.setText("PDF");

        checkPdfHighlightText.setSelected(true);
        checkPdfHighlightText.setText("Highlight text in PDF");

        checkDataCapture.setSelected(true);
        checkDataCapture.setText("Data capture (invoices and forms)");

        checkWordLevel.setText("Word level (instead of line)");

        checkAutoRotatePages.setText("Auto rotate pages");

        buttonOcr.setBackground(new java.awt.Color(102, 204, 255));
        buttonOcr.setFont(new java.awt.Font("Tahoma", 1, 14)); 
        buttonOcr.setText("OCR");

        comboLanguage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Language:");

        comboRecognizeType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        radioOutputFormatRtf.setText("RTF/Word");

        jLabel5.setText("Recognize:");

        jLabel6.setText("Engine start props (optional):");

        comboPropsStart.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Props (optional):");

        comboPropsRecognition.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        linkLabelHelp.setText("Help");
        linkLabelHelp.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(comboTextLayout, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkDataCapture)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkAutoRotatePages)
                        .addGap(18, 18, 18)
                        .addComponent(checkWordLevel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(comboLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboPropsStart, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(comboFileImage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonBrowseFile))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioOutputFormatPlainText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(radioOutputFormatXml)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(radioOutputFormatPdf)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkPdfHighlightText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(radioOutputFormatRtf)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboRecognizeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(comboPropsRecognition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(linkLabelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonOcr)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(comboLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(comboPropsStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(comboFileImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBrowseFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboTextLayout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(checkDataCapture)
                    .addComponent(checkAutoRotatePages)
                    .addComponent(checkWordLevel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(radioOutputFormatPlainText)
                    .addComponent(radioOutputFormatXml)
                    .addComponent(radioOutputFormatPdf)
                    .addComponent(checkPdfHighlightText)
                    .addComponent(radioOutputFormatRtf)
                    .addComponent(jLabel5)
                    .addComponent(comboRecognizeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(comboPropsRecognition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(linkLabelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonOcr))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }



    private javax.swing.JButton buttonBrowseFile;
    private javax.swing.JButton buttonOcr;
    private javax.swing.JCheckBox checkAutoRotatePages;
    private javax.swing.JCheckBox checkDataCapture;
    private javax.swing.JCheckBox checkPdfHighlightText;
    private javax.swing.JCheckBox checkWordLevel;
    private javax.swing.JComboBox comboFileImage;
    private javax.swing.JComboBox comboLanguage;
    private javax.swing.JComboBox comboPropsRecognition;
    private javax.swing.JComboBox comboPropsStart;
    private javax.swing.JComboBox comboRecognizeType;
    private javax.swing.JComboBox comboTextLayout;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private com.asprise.ocr.sample.util.LinkLabel linkLabelHelp;
    private javax.swing.JRadioButton radioOutputFormatPdf;
    private javax.swing.JRadioButton radioOutputFormatPlainText;
    private javax.swing.JRadioButton radioOutputFormatRtf;
    private javax.swing.JRadioButton radioOutputFormatXml;

}
