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

import com.asprise.ocr.sample.util.ActionBase;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.GregorianCalendar;


public class PanelLogging extends javax.swing.JPanel {

    ActionBase actionCopyLogs = new ActionBase("Copy") {
        @Override
        public void actionPerformed(ActionEvent e) {
            setClipboard(textLogging.getText());
            JOptionPane.showMessageDialog(getContainingWindow(PanelLogging.this), "Logs have been copied to system clipboard successfully.");
        }
    };

    ActionBase actionClearLogs = new ActionBase("Clear") {
        @Override
        public void actionPerformed(ActionEvent e) {
            textLogging.setText("");
        }
    };

    
    public PanelLogging() {
        initComponents();
        init();
    }

    void init() {

        DemoUtils.enableMenu(textLogging);
    }

    
    public void log(String mesg) {
        logAndScrollToBottom(textLogging, formatMessage(mesg));
    }

    private static void logAndScrollToBottom(final JTextArea textArea, final String mesg) {
        if(! SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    logAndScrollToBottom(textArea, mesg);
                }
            });
            return;
        }
        textArea.append(textArea.getText().length() == 0 ? mesg : "\n" + mesg);

        String text = textArea.getText();
        if(text.length() > 2) {
            int lastLinePos = text.lastIndexOf("\n", text.length() - 2);
            if(lastLinePos > 0) {
                textArea.setCaretPosition(lastLinePos + 1); 
            }
        }
    }

    String formatMessage(String mesg) {
        String threadName = Thread.currentThread().getName();
        if(threadName.length() > 10) {
            threadName = threadName.substring(threadName.length() - 10, threadName.length());
        }
        return String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-10s %3$s", new GregorianCalendar(), threadName, mesg);
    }


    
    public static void setClipboard(String content) {
        StringSelection ss = new StringSelection(content);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    public static Window getContainingWindow(Component component) {
        if(component == null)
            return null;

        Container container = component.getParent();

        while(true) {
            if(container == null)
                return null;

            if(container instanceof Window)
                return (Window)container;

            container = container.getParent();
        }
    }

    
    @SuppressWarnings("unchecked")

    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textLogging = new javax.swing.JTextArea();

        textLogging.setEditable(false);
        textLogging.setBackground(new java.awt.Color(214, 240, 240));
        textLogging.setColumns(20);
        textLogging.setFont(new java.awt.Font("Monospaced", 0, 12)); 
        textLogging.setRows(5);
        jScrollPane1.setViewportView(textLogging);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
    }



    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textLogging;

}
