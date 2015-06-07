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

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


public class JPanelGradient extends JPanel {
	private static final long serialVersionUID = 1L;

    
    public static interface AdditionalPaint {
        void additionalPaint(Graphics2D g2d, int width, int height);
    }

	protected Color bgGradient1;
	protected Color bgGradient2;

	protected RoundedBorder border;

	public JPanelGradient() {
		this(10, Color.lightGray, Color.white, new Color(0xe8e8e8));
	}

	
	public JPanelGradient(int borderCornerRadius, Color borderColor, Color bgGradient1, Color bgGradient2) {
		setBackgroundGradient(bgGradient1, bgGradient2);
		border = new RoundedBorder(borderCornerRadius, borderColor);
		setBorder(border);
	}

	public void setBorderCornerRadius(int borderCornerRadius) {
		border.setCornerRadius(borderCornerRadius);
		repaint();
	}

	public void setBorderColor(Color borderColor) {
		border.setColor(borderColor);
		repaint();
	}

	public void setBackgroundGradient(Color color1, Color color2) {
		this.bgGradient1 = color1;
		this.bgGradient2 = color2;
	}

    public void setBackgroundGradientColor1(Color color) {
        this.bgGradient1 = color;
    }

    public void setBackgroundGradientColor2(Color color) {
        this.bgGradient2 = color;
    }

    AdditionalPaint additionalPaint;

    public AdditionalPaint getAdditionalPaint() {
        return additionalPaint;
    }

    public void setAdditionalPaint(AdditionalPaint additionalPaint) {
        this.additionalPaint = additionalPaint;
    }

    @Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(isOpaque()) {
           return;
        }
		if(bgGradient1 == null || bgGradient2 == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        GradientPaint gradient = new GradientPaint(0, 0, bgGradient1, 0, h, bgGradient2, false);
        g2.setPaint(gradient);

        g2.fillRoundRect(0, 0, w, h, border.getCornerRadius(), border.getCornerRadius());

        if(additionalPaint != null) {
            additionalPaint.additionalPaint(g2, w, h);
        }
	}
}
