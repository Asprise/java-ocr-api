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
package com.asprise.ocr.sample.util;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LinkLabel extends JLabel {
    private static final long serialVersionUID = 8273875024682878518L;
    private String text;
    private URI uri;

    public LinkLabel() {
    }

    public LinkLabel(String text, URI uri){
        super();
        setup(text,uri);
    }

    public LinkLabel(String text, String uri){
        super();
        URI oURI;
        try {
            oURI = new URI(uri);
        } catch (URISyntaxException e) {

            throw new RuntimeException(e);
        }
        setup(text,oURI);
    }

    public void setup(String t, URI u){
        text = t;
        uri = u;
        setText(text);
        setToolTipText(uri.toString());
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                open(uri);
            }
            public void mouseEntered(MouseEvent e) {
                setText(text,false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                setText(text,true);
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    @Override
    public void setText(String text){
        setText(text,true);
    }

    public void setText(String text, boolean ul){
        String link = ul ? "<u>"+text+"</u>" : text;
        super.setText("<html><span style=\"color: #000099;\">"+
                link+"</span></html>");
        this.text = text;
    }

    public String getRawText(){
        return text;
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "URL: " + uri,
                        "Please use your browser to visit:", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "URL: " + uri,
                    "Please use your browser to visit:", JOptionPane.WARNING_MESSAGE);
        }
    }
}