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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public abstract class ActionBase extends AbstractAction {


    
    public ActionBase() {
    }

    
    public ActionBase(String name) {
        super(name);

    }

    
    public ActionBase(String name, Icon smallIcon) {
        super(name, smallIcon);
    }

    
    public ActionBase(String name, Icon smallIcon, Icon largeIcon,
                      String shortDescription, String longDescription,
                      int keyCode, int keyModifiers) {
        super(name);

        if(smallIcon != null) {
            setSmallIcon(smallIcon);
        }
        if(largeIcon != null) {
            setLargeIcon(largeIcon);
        }
        if(shortDescription != null) {
            setShortDescription(shortDescription);
        }
        if(longDescription != null) {
            setLongDescription(longDescription);
        }
        if(keyCode != -1) {
            setAcceleratorKey(keyCode, keyModifiers);
        }
    }


    
    public void setSmallIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
    }

    
    public Icon getSmallIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    
    public void setLargeIcon(Icon icon) {
        putValue("SwingLargeIconKey", icon);
    }

    
    public Icon getLargeIcon() {
        return (Icon) getValue("SwingLargeIconKey");
    }


    
    public void setName(String name) {
        putValue(NAME, name);
    }

    
    public String getName() {
        return (String) getValue(NAME);
    }

    
    public void setAcceleratorKey(KeyStroke key) {
        putValue(ACCELERATOR_KEY, key);
    }

    
    public KeyStroke getAcceleratorKey() {
        return (KeyStroke)getValue(ACCELERATOR_KEY);
    }

    
    public void setAcceleratorKey(int keyCode, int modifiers) {
        setAcceleratorKey(KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    
    public void setShortDescription(String desc) {
        putValue(SHORT_DESCRIPTION, desc);
    }

    
    public String getShortDescription() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    
    public void setLongDescription(String desc) {
        putValue(LONG_DESCRIPTION, desc);
    }

    
    public String getLongDescription() {
        return (String) getValue(LONG_DESCRIPTION);
    }
}
