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
package com.asprise.ocr.sample.util.prefs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;


public class FileSystemPreferencesFactory implements PreferencesFactory {
    public Preferences userRoot() {
        return FileSystemPreferences.getUserRoot();
    }

    public Preferences systemRoot() {
        return FileSystemPreferences.getSystemRoot();
    }

    public static void main(String[] args) throws BackingStoreException {
        Preferences prefs = new FileSystemPreferencesFactory().userRoot().node(String.class.getName());
        String name = prefs.get("NAME", null);
        int age = prefs.getInt("AGE", -1);
        boolean isMale = prefs.getBoolean("MALE", false);

        System.out.println(name + "/" + age + "/" + isMale);

        prefs.put("NAME", "Homer");
        prefs.putInt("AGE", 45);
        prefs.putBoolean("MALE", true);
        prefs.flush();

        System.out.println("Done.");
    }
}
