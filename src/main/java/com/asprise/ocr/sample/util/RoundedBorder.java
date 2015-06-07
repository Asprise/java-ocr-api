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

import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;



public class RoundedBorder implements Border {
    protected int cornerRadius;

    
    protected Color color;

    
    public RoundedBorder() {
        this(10, Color.lightGray);
    }

    
    public RoundedBorder(int cornerRadius, Color color) {
        this.cornerRadius = cornerRadius;
        setColor(color);
    }

    
    public void setColor(Color color) {
    	this.color = color;
    }

    
    public void setCornerRadius(int cornerRadius) {
    	this.cornerRadius = cornerRadius;
    }

    public int getCornerRadius() {
    	return cornerRadius;
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0,0,0,0));
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = insets.bottom = cornerRadius/2;
        insets.left = insets.right = 1;
        return insets;
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    	if(color == null) {
    		return;
    	}

        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(deriveColorAlpha(color, 40));
        g2.drawRoundRect(x, y + 2, width - 1, height - 3, cornerRadius, cornerRadius);
        g2.setColor(deriveColorAlpha(color, 90));
        g2.drawRoundRect(x, y + 1, width - 1, height - 2, cornerRadius, cornerRadius);
        g2.setColor(deriveColorAlpha(color, 255));
        g2.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);

        g2.dispose();
    }



    public static BufferedImage createTranslucentImage(int width, int height) {

        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);

    }

    public static Color deriveColorAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    
    public static Color deriveColorHSB(Color base, float dH, float dS, float dB) {
        float hsb[] = Color.RGBtoHSB(
                base.getRed(), base.getGreen(), base.getBlue(), null);

        hsb[0] += dH;
        hsb[1] += dS;
        hsb[2] += dB;
        return Color.getHSBColor(
                hsb[0] < 0? 0 : (hsb[0] > 1? 1 : hsb[0]),
                hsb[1] < 0? 0 : (hsb[1] > 1? 1 : hsb[1]),
                hsb[2] < 0? 0 : (hsb[2] > 1? 1 : hsb[2]));

    }
}
