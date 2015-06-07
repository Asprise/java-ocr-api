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

import javax.swing.JTextArea;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class JTextAreaAdditionalPaintable extends JTextArea {

    AdditionalPaintable additionalPaint;

    public AdditionalPaintable getAdditionalPaint() {
        return additionalPaint;
    }

    public void setAdditionalPaint(AdditionalPaintable additionalPaint) {
        this.additionalPaint = additionalPaint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        if(additionalPaint != null) {
            additionalPaint.additionalPaint(this, g2, w, h);
        }
    }
}
