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

import com.asprise.ocr.sample.util.prefs.FileSystemPreferencesFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

public class DemoUtils {

    static final String DELIMITER = "`";

    public static void setPreferencesWithXmlBackstoreOnWindows() {
        if(isWindows()) {
            System.setProperty("java.util.prefs.PreferencesFactory", FileSystemPreferencesFactory.class.getName());
        }
    }

    
    public static void loadPrefs(Preferences prefs, String prefKey, JComboBox combo) {

        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        String recents = prefs.get(prefKey, null);
        if (recents != null) {
            StringTokenizer st = new StringTokenizer(recents, DELIMITER);
            while (st.hasMoreTokens()) {
                comboModel.addElement(st.nextToken());
            }
        }

        combo.setModel(comboModel);
    }

    
    public static void savePrefs(Preferences prefs, String prefKey, JComboBox combo, String newValidValue) {
        if (newValidValue == null) { 
            return;
        }

        DefaultComboBoxModel comboModel = (DefaultComboBoxModel) combo.getModel();

        int existingIndex = comboModel.getIndexOf(newValidValue);
        if (existingIndex >= 0) { 
            comboModel.removeElementAt(existingIndex);
        }
        comboModel.insertElementAt(newValidValue, 0);
        combo.setSelectedIndex(0);

        StringBuilder entries = new StringBuilder();
        int size = Math.min(comboModel.getSize(), 20); 
        for (int i = 0; i < size; i++) {
            entries.append(comboModel.getElementAt(i));
            if (i != size - 1) { 
                entries.append(DELIMITER);
            }
        }

        while (entries.length() > Preferences.MAX_VALUE_LENGTH) {
            int lastIndex = entries.lastIndexOf(DELIMITER);
            if (lastIndex == -1) {
                break;
            } else {
                entries.delete(lastIndex, entries.length());
            }
        }

        prefs.put(prefKey, entries.toString());
        try {
            prefs.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    
    public static void autoAwesomeLookAndFeel(String fontName, Map<Object, Object> defaults) {

        if (!isWindows()) { 
            setSystemLookAndFeel();
        }

        if (UIManager.getLookAndFeel().toString().contains("MetalLookAndFeel")) {

            FontUIResource font = new FontUIResource(fontName == null ? Font.SANS_SERIF : fontName.trim(), Font.PLAIN, 12);
            java.util.Enumeration keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    FontUIResource fontExisting = (FontUIResource) value;

                    UIManager.put(key, font);
                }
            }

            UIManager.put("SplitPaneDivider.draggingColor", Color.gray);

            if (defaults != null) {
                for (Object key : defaults.keySet()) {
                    UIManager.put(key, defaults.get(key));
                }
            }
        }
    }

    
    public static void setSystemLookAndFeel() {
        if (isWindows()) {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } else if (isMac()) {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } else {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
    }

    
    private static boolean setLookAndFeel(String lookAndFeelClass) {
        try {
            UIManager.setLookAndFeel(lookAndFeelClass);
            return true;
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return false;
        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static JFileChooser registerBrowseButtonListener(final JComboBox comboBox, final JButton button, final boolean chooseFile, final boolean isOpen, final FileFilter fileFilter, final File initialDirectory) {
        if(! comboBox.isEditable()) {
            throw new IllegalArgumentException("The combo box must be editable.");
        }

        final JFileChooser fileChooser = new JFileChooser();

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileChooser.setCurrentDirectory(comboBox.getSelectedItem() == null || comboBox.getSelectedItem().toString().trim().length() == 0 ?
                        initialDirectory : new File(comboBox.getSelectedItem().toString()));

                fileChooser.setFileSelectionMode(chooseFile ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);

                if(fileFilter != null) {
                    fileChooser.addChoosableFileFilter(fileFilter);
                }

                int ret = isOpen ? fileChooser.showOpenDialog(comboBox) :
                        fileChooser.showSaveDialog(comboBox);

                if(ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    comboBox.getEditor().setItem(file.getAbsolutePath());
                }
            }
        };

        button.addActionListener(listener);

        return fileChooser;
    }

    public static FileFilter getFileFilterForExtensions(final String[] exts, final boolean ignoreCase) {
        FileFilter filter = new FileFilter() {
            @Override
            public String getDescription() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < exts.length; i++) {
                    if(i != 0) {
                        sb.append(", ");
                    }
                    sb.append("*." + exts[i]);
                }
                return sb.toString();
            }

            @Override
            public boolean accept(File f) {
                if(f == null) {
                    return false;
                }
                if(f.isDirectory()) {

                    return true;
                }else if(ignoreCase) {
                    for (int i = 0; i < exts.length; i++) {
                        if(f.getName().toUpperCase().endsWith("." + exts[i].toUpperCase())) {
                            return true;
                        }
                    }
                }else{
                    for (int i = 0; i < exts.length; i++) {
                        if(f.getName().endsWith("." + exts[i])) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };

        return filter;
    }

    public static void fixPrefsWarning() {
        try {

            Class classPlatformLogger = Class.forName("sun.util.logging.PlatformLogger");
            Method methodGetLogger = classPlatformLogger.getMethod("getLogger", String.class);
            Object objectLogger = methodGetLogger.invoke(null, "java.util.prefs");

            Method methodSetLevel = classPlatformLogger.getMethod("setLevel", int.class);
            methodSetLevel.invoke(objectLogger, 1000); 
        } catch (Throwable t) {

        }
    }

    public static void showWindowBestSizeAndPosition(Window window) {
        Dimension dimBack = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dimWin = window.getSize(); 
        int x = (int)((1 - 0.5) * (dimBack.width - dimWin.width)); 
        int y = (int)((1 - 0.618) * (dimBack.height - dimWin.height)); 
        window.setLocation(x, y);
    }

    public static void enableMenu(JTextComponent text) {
        final Action actionCopySelected = text.getActionMap().get(DefaultEditorKit.copyAction);
        final Action actionSelectAll = text.getActionMap().get(DefaultEditorKit.selectAllAction);
        final Action actionUnselect = text.getActionMap().get("unselect");
        Action actionCopyAll = new AbstractAction("Copy All to System Clipboard") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(actionSelectAll != null) {
                    actionSelectAll.actionPerformed(e);
                }
                if(actionCopySelected != null) {
                    actionCopySelected.actionPerformed(e);
                }
                if(actionUnselect != null) {
                    actionUnselect.actionPerformed(e);
                }
            }
        };

        JPopupMenu popupMenu = new JPopupMenu();
        if(actionCopySelected != null) {
            actionCopySelected.putValue(Action.NAME, "Copy Selected");
            popupMenu.add(actionCopySelected);
        }
        if(actionSelectAll != null && actionCopySelected != null) {
            popupMenu.add(actionCopyAll);
        }
        text.setComponentPopupMenu(popupMenu);
    }

    private static List<Image> iconsLogo;

    public static List<Image> getApplicationIconsLogo() {
        if(iconsLogo == null) {
            iconsLogo = new ArrayList<Image>();
            iconsLogo.add(Toolkit.getDefaultToolkit().createImage(DemoUtils.class.getResource("/icon/16.png")));
            iconsLogo.add(Toolkit.getDefaultToolkit().createImage(DemoUtils.class.getResource("/icon/32.png")));
            iconsLogo.add(Toolkit.getDefaultToolkit().createImage(DemoUtils.class.getResource("/icon/64.png")));
            iconsLogo.add(Toolkit.getDefaultToolkit().createImage(DemoUtils.class.getResource("/icon/128.png")));
        }
        return iconsLogo;
    }

    public static String getText(JComboBox editableComboBox) {
        return ((JTextComponent)editableComboBox.getEditor().getEditorComponent()).getText().trim();
    }

}