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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;



class FileSystemPreferences extends AbstractPreferences {
    
    private static final int SYNC_INTERVAL = Math.max(1,
            Integer.parseInt(
                    AccessController.doPrivileged(
                            new sun.security.action.GetPropertyAction(
                                    "java.util.prefs.syncInterval", "30"))));

    

    private static Logger _logger;
    private static Logger getLogger() {
        if(_logger == null) {
            _logger = Logger.getLogger("asprise.FileSystemPreferences");
            _logger.setLevel(Level.SEVERE);
        }
        return _logger;
    }

    
    private static File systemRootDir;


    private static boolean isSystemRootWritable;

    
    private static File userRootDir;


    private static boolean isUserRootWritable;

    
    static Preferences userRoot = null;

    static synchronized Preferences getUserRoot() {
        if (userRoot == null) {
            setupUserRoot();
            userRoot = new FileSystemPreferences(true);
        }
        return userRoot;
    }

    private static void setupUserRoot() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                userRootDir =
                        new File(System.getProperty("java.util.prefs.userRoot",
                                System.getProperty("user.home")), ".java/.userPrefs");

                if (!userRootDir.exists()) {
                    if (userRootDir.mkdirs()) {
                        try {
                            chmod(userRootDir.getCanonicalPath(), USER_RWX);
                        } catch (IOException e) {
                            getLogger().warning("Could not change permissions" +
                                    " on userRoot directory. ");
                        }
                        getLogger().info("Created user preferences directory.");
                    }
                    else
                        getLogger().warning("Couldn't create user preferences" +
                                " directory. User preferences are unusable.");
                }
                isUserRootWritable = userRootDir.canWrite();
                String USER_NAME = System.getProperty("user.name");
                userLockFile = new File (userRootDir,".user.lock." + USER_NAME);
                userRootModFile = new File (userRootDir,
                        ".userRootModFile." + USER_NAME);
                if (!userRootModFile.exists())
                    try {

                        userRootModFile.createNewFile();

                        int result = chmod(userRootModFile.getCanonicalPath(),
                                USER_READ_WRITE);
                        if (result !=0)
                            getLogger().warning("Problem creating userRoot " +
                                    "mod file. Chmod failed on " +
                                    userRootModFile.getCanonicalPath() +
                                    " Unix error code " + result);
                    } catch (IOException e) {
                        getLogger().warning(e.toString());
                    }
                userRootModTime = userRootModFile.lastModified();
                return null;
            }
        });
    }


    
    static Preferences systemRoot;

    static synchronized Preferences getSystemRoot() {
        if (systemRoot == null) {
            setupSystemRoot();
            systemRoot = new FileSystemPreferences(false);
        }
        return systemRoot;
    }

    private static void setupSystemRoot() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                String systemPrefsDirName =
                        System.getProperty("java.util.prefs.systemRoot","/etc/.java");
                systemRootDir =
                        new File(systemPrefsDirName, ".systemPrefs");

                if (!systemRootDir.exists()) {

                    systemRootDir =
                            new File(System.getProperty("java.home"),
                                    ".systemPrefs");
                    if (!systemRootDir.exists()) {
                        if (systemRootDir.mkdirs()) {
                            getLogger().info(
                                    "Created system preferences directory "
                                            + "in java.home.");
                            try {
                                chmod(systemRootDir.getCanonicalPath(),
                                        USER_RWX_ALL_RX);
                            } catch (IOException e) {
                            }
                        } else {
                            getLogger().warning("Could not create "
                                    + "system preferences directory. System "
                                    + "preferences are unusable.");
                        }
                    }
                }
                isSystemRootWritable = systemRootDir.canWrite();
                systemLockFile = new File(systemRootDir, ".system.lock");
                systemRootModFile =
                        new File (systemRootDir,".systemRootModFile");
                if (!systemRootModFile.exists() && isSystemRootWritable)
                    try {

                        systemRootModFile.createNewFile();
                        int result = chmod(systemRootModFile.getCanonicalPath(),
                                USER_RW_ALL_READ);
                        if (result !=0)
                            getLogger().warning("Chmod failed on " +
                                    systemRootModFile.getCanonicalPath() +
                                    " Unix error code " + result);
                    } catch (IOException e) { getLogger().warning(e.toString());
                    }
                systemRootModTime = systemRootModFile.lastModified();
                return null;
            }
        });
    }


    
    private static final int USER_READ_WRITE = 0600;

    private static final int USER_RW_ALL_READ = 0644;


    private static final int USER_RWX_ALL_RX = 0755;

    private static final int USER_RWX = 0700;

    
    static File userLockFile;



    
    static File systemLockFile;

    

    private static int userRootLockHandle = 0;

    

    private static int systemRootLockHandle = 0;

    
    private final File dir;

    
    private final File prefsFile;

    
    private final File tmpFile;

    
    private static  File userRootModFile;

    
    private static boolean isUserRootModified = false;

    
    private static long userRootModTime;



    private static File systemRootModFile;

    private static boolean isSystemRootModified = false;

    
    private static long systemRootModTime;

    
    private Map<String, String> prefsCache = null;

    
    private long lastSyncTime = 0;

    
    private static final int EAGAIN = 11;

    
    private static final int EACCES = 13;


    private static final int LOCK_HANDLE = 0;
    private static final int ERROR_CODE = 1;

    
    final List<Change> changeLog = new ArrayList<Change>();

    
    private abstract class Change {

        abstract void replay();
    };

    
    private class Put extends Change {
        String key, value;

        Put(String key, String value) {
            this.key = key;
            this.value = value;
        }

        void replay() {
            prefsCache.put(key, value);
        }
    }

    
    private class Remove extends Change {
        String key;

        Remove(String key) {
            this.key = key;
        }

        void replay() {
            prefsCache.remove(key);
        }
    }

    
    private class NodeCreate extends Change {

        void replay() {
        }
    }

    
    NodeCreate nodeCreate = null;

    
    private void replayChanges() {
        for (int i = 0, n = changeLog.size(); i<n; i++)
            changeLog.get(i).replay();
    }

    private static Timer syncTimer = new Timer(true); 

    static {

        syncTimer.schedule(new TimerTask() {
            public void run() {
                syncWorld();
            }
        }, SYNC_INTERVAL*1000, SYNC_INTERVAL*1000);

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        syncTimer.cancel();
                        syncWorld();
                    }
                });
                return null;
            }
        });
    }

    private static void syncWorld() {

        Preferences userRt;
        Preferences systemRt;
        synchronized(FileSystemPreferences.class) {
            userRt   = userRoot;
            systemRt = systemRoot;
        }

        try {
            if (userRt != null)
                userRt.flush();
        } catch(BackingStoreException e) {
            getLogger().warning("Couldn't flush user prefs: " + e);
        }

        try {
            if (systemRt != null)
                systemRt.flush();
        } catch(BackingStoreException e) {
            getLogger().warning("Couldn't flush system prefs: " + e);
        }
    }

    private final boolean isUserNode;

    
    private FileSystemPreferences(boolean user) {
        super(null, "");
        isUserNode = user;
        dir = (user ? userRootDir: systemRootDir);
        prefsFile = new File(dir, "prefs.xml");
        tmpFile   = new File(dir, "prefs.tmp");
    }

    
    private FileSystemPreferences(FileSystemPreferences parent, String name) {
        super(parent, name);
        isUserNode = parent.isUserNode;
        dir  = new File(parent.dir, dirName(name));
        prefsFile = new File(dir, "prefs.xml");
        tmpFile  = new File(dir, "prefs.tmp");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                newNode = !dir.exists();
                return null;
            }
        });
        if (newNode) {

            prefsCache = new TreeMap<String, String>();
            nodeCreate = new NodeCreate();
            changeLog.add(nodeCreate);
        }
    }

    public boolean isUserNode() {
        return isUserNode;
    }

    protected void putSpi(String key, String value) {
        initCacheIfNecessary();
        changeLog.add(new Put(key, value));
        prefsCache.put(key, value);
    }

    protected String getSpi(String key) {
        initCacheIfNecessary();
        return prefsCache.get(key);
    }

    protected void removeSpi(String key) {
        initCacheIfNecessary();
        changeLog.add(new Remove(key));
        prefsCache.remove(key);
    }

    
    private void initCacheIfNecessary() {
        if (prefsCache != null)
            return;

        try {
            loadCache();
        } catch(Exception e) {

            prefsCache = new TreeMap<String, String>();
        }
    }

    
    private void loadCache() throws BackingStoreException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws BackingStoreException {
                            Map<String, String> m = new TreeMap<String, String>();
                            long newLastSyncTime = 0;
                            try {
                                newLastSyncTime = prefsFile.lastModified();

                                    FileInputStream fis = new FileInputStream(prefsFile);
                                    XmlSupport.importMap(fis, m);

                            } catch(Exception e) {
                                if (e instanceof InvalidPreferencesFormatException) {
                                    getLogger().warning("Invalid preferences format in "
                                            +  prefsFile.getPath());

                                   renameFile(prefsFile, new File(prefsFile.getParentFile(), "IncorrectFormatPrefs.xml"));
                                    m = new TreeMap<String, String>();
                                } else if (e instanceof FileNotFoundException) {

                                } else {
                                    throw new BackingStoreException(e);
                                }
                            }

                            prefsCache = m;
                            lastSyncTime = newLastSyncTime;
                            return null;
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (BackingStoreException) e.getException();
        }
    }

    
    private void writeBackCache() throws BackingStoreException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws BackingStoreException {
                            try {
                                if (!dir.exists() && !dir.mkdirs())
                                    throw new BackingStoreException(dir +
                                            " create failed.");

                                    FileOutputStream fos = new FileOutputStream(tmpFile);
                                    XmlSupport.exportMap(fos, prefsCache);

                                if (!renameFile(tmpFile, prefsFile))
                                    throw new BackingStoreException("Can't rename " +
                                            tmpFile + " to " + prefsFile);
                            } catch(Exception e) {
                                if (e instanceof BackingStoreException)
                                    throw (BackingStoreException)e;
                                throw new BackingStoreException(e);
                            }
                            return null;
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (BackingStoreException) e.getException();
        }
    }

    protected String[] keysSpi() {
        initCacheIfNecessary();
        return prefsCache.keySet().toArray(new String[prefsCache.size()]);
    }

    protected String[] childrenNamesSpi() {
        return AccessController.doPrivileged(
                new PrivilegedAction<String[]>() {
                    public String[] run() {
                        List<String> result = new ArrayList<String>();
                        File[] dirContents = dir.listFiles();
                        if (dirContents != null) {
                            for (int i = 0; i < dirContents.length; i++)
                                if (dirContents[i].isDirectory())
                                    result.add(nodeName(dirContents[i].getName()));
                        }
                        return result.toArray(EMPTY_STRING_ARRAY);
                    }
                });
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    protected AbstractPreferences childSpi(String name) {
        return new FileSystemPreferences(this, name);
    }

    public void removeNode() throws BackingStoreException {
        synchronized (isUserNode()? userLockFile: systemLockFile) {

            if (!lockFile(false))
                throw(new BackingStoreException("Couldn't get file lock."));
            try {
                super.removeNode();
            } finally {
                unlockFile();
            }
        }
    }

    
    protected void removeNodeSpi() throws BackingStoreException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws BackingStoreException {
                            if (changeLog.contains(nodeCreate)) {
                                changeLog.remove(nodeCreate);
                                nodeCreate = null;
                                return null;
                            }
                            if (!dir.exists())
                                return null;
                            prefsFile.delete();
                            tmpFile.delete();

                            File[] junk = dir.listFiles();
                            if (junk.length != 0) {
                                getLogger().warning(
                                        "Found extraneous files when removing node: "
                                                + Arrays.asList(junk));
                                for (int i=0; i<junk.length; i++)
                                    junk[i].delete();
                            }
                            if (!dir.delete())
                                throw new BackingStoreException("Couldn't delete dir: "
                                        + dir);
                            return null;
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (BackingStoreException) e.getException();
        }
    }

    public synchronized void sync() throws BackingStoreException {
        boolean userNode = isUserNode();
        boolean shared;

        if (userNode) {
            shared = false; 
        } else {

            shared = !isSystemRootWritable;
        }
        synchronized (isUserNode()? userLockFile:systemLockFile) {
            if (!lockFile(shared))
                throw(new BackingStoreException("Couldn't get file lock."));
            final Long newModTime =
                    AccessController.doPrivileged(
                            new PrivilegedAction<Long>() {
                                public Long run() {
                                    long nmt;
                                    if (isUserNode()) {
                                        nmt = userRootModFile.lastModified();
                                        isUserRootModified = userRootModTime == nmt;
                                    } else {
                                        nmt = systemRootModFile.lastModified();
                                        isSystemRootModified = systemRootModTime == nmt;
                                    }
                                    return new Long(nmt);
                                }
                            });
            try {
                super.sync();
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        if (isUserNode()) {
                            userRootModTime = newModTime.longValue() + 1000;
                            userRootModFile.setLastModified(userRootModTime);
                        } else {
                            systemRootModTime = newModTime.longValue() + 1000;
                            systemRootModFile.setLastModified(systemRootModTime);
                        }
                        return null;
                    }
                });
            } finally {
                unlockFile();
            }
        }
    }

    protected void syncSpi() throws BackingStoreException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws BackingStoreException {
                            syncSpiPrivileged();
                            return null;
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (BackingStoreException) e.getException();
        }
    }
    private void syncSpiPrivileged() throws BackingStoreException {
        if (isRemoved())
            throw new IllegalStateException("Node has been removed");
        if (prefsCache == null)
            return;  
        long lastModifiedTime;
        if ((isUserNode() ? isUserRootModified : isSystemRootModified)) {
            lastModifiedTime = prefsFile.lastModified();
            if (lastModifiedTime  != lastSyncTime) {

                loadCache();
                replayChanges();
                lastSyncTime = lastModifiedTime;
            }
        } else if (lastSyncTime != 0 && !dir.exists()) {

            prefsCache = new TreeMap<String, String>();
            replayChanges();
        }
        if (!changeLog.isEmpty()) {
            writeBackCache();  
            lastModifiedTime = prefsFile.lastModified();

            if (lastSyncTime <= lastModifiedTime) {
                lastSyncTime = lastModifiedTime + 1000;
                prefsFile.setLastModified(lastSyncTime);
            }
            changeLog.clear();
        }
    }

    public void flush() throws BackingStoreException {
        if (isRemoved())
            return;
        sync();
    }

    protected void flushSpi() throws BackingStoreException {

    }

    
    private static boolean isDirChar(char ch) {
        return ch > 0x1f && ch < 0x7f && ch != '/' && ch != '.' && ch != '_';
    }

    
    private static String dirName(String nodeName) {

        StringBuilder sb = new StringBuilder(nodeName);
        for(int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if( (c >= 0 && c <= 9)
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                || (c == '-' || c == '_' )
                    ) {

            } else {
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString(); 
    }

    
    private static byte[] byteArray(String s) {
        int len = s.length();
        byte[] result = new byte[2*len];
        for (int i=0, j=0; i<len; i++) {
            char c = s.charAt(i);
            result[j++] = (byte) (c>>8);
            result[j++] = (byte) c;
        }
        return result;
    }

    
    private static String nodeName(String dirName) {
        if (dirName.charAt(0) != '_')
            return dirName;
        byte a[] = Base64.altBase64ToByteArray(dirName.substring(1));
        StringBuffer result = new StringBuffer(a.length/2);
        for (int i = 0; i < a.length; ) {
            int highByte = a[i++] & 0xff;
            int lowByte =  a[i++] & 0xff;
            result.append((char) ((highByte << 8) | lowByte));
        }
        return result.toString();
    }

    
    private boolean lockFile(boolean shared) throws SecurityException{
        boolean usernode = isUserNode();
        int[] result;
        int errorCode = 0;
        File lockFile = (usernode ? userLockFile : systemLockFile);
        long sleepTime = INIT_SLEEP_TIME;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                int perm = (usernode? USER_READ_WRITE: USER_RW_ALL_READ);
                result = lockFile0(lockFile.getCanonicalPath(), perm, shared);

                errorCode = result[ERROR_CODE];
                if (result[LOCK_HANDLE] != 0) {
                    if (usernode) {
                        userRootLockHandle = result[LOCK_HANDLE];
                    } else {
                        systemRootLockHandle = result[LOCK_HANDLE];
                    }
                    return true;
                }
            } catch(IOException e) {

            }

            try {
                Thread.sleep(sleepTime);
            } catch(InterruptedException e) {
                checkLockFile0ErrorCode(errorCode);
                return false;
            }
            sleepTime *= 2;
        }
        checkLockFile0ErrorCode(errorCode);
        return false;
    }

    
    private void checkLockFile0ErrorCode (int errorCode)
            throws SecurityException {
        if (errorCode == EACCES)
            throw new SecurityException("Could not lock " +
                    (isUserNode()? "User prefs." : "System prefs.") +
                    " Lock file access denied.");
        if (errorCode != EAGAIN)
            getLogger().warning("Could not lock " +
                    (isUserNode()? "User prefs. " : "System prefs.") +
                    " Unix error code " + errorCode + ".");
    }

    
    private static int[] lockFile0(String fileName, int permission, boolean shared) {
        return new int[] {1, 0};
    }

    
    private  static int unlockFile0(int lockHandle) {
        return 0;
    }

    

    private static int chmod(String fileName, int permission) {
        return 0;
    }

    
    private static int INIT_SLEEP_TIME = 50;

    
    private static int MAX_ATTEMPTS = 5;

    
    private void unlockFile() {
        int result;
        boolean usernode = isUserNode();
        File lockFile = (usernode ? userLockFile : systemLockFile);
        int lockHandle = ( usernode ? userRootLockHandle:systemRootLockHandle);
        if (lockHandle == 0) {
            getLogger().warning("Unlock: zero lockHandle for " +
                    (usernode ? "user":"system") + " preferences.)");
            return;
        }
        result = unlockFile0(lockHandle);
        if (result != 0) {
            getLogger().warning("Could not drop file-lock on " +
                    (isUserNode() ? "user" : "system") + " preferences." +
                    " Unix error code " + result + ".");
            if (result == EACCES)
                throw new SecurityException("Could not unlock" +
                        (isUserNode()? "User prefs." : "System prefs.") +
                        " Lock file access denied.");
        }
        if (isUserNode()) {
            userRootLockHandle = 0;
        } else {
            systemRootLockHandle = 0;
        }
    }

    @Override
    public boolean isRemoved() {
        return super.isRemoved();
    }

    public Object getLock() {
        return lock;
    }

    static boolean renameFile(File source, File target) {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            if(copyFile(source, target)) {
                source.delete();
                return true;
            } else {
                return false;
            }
        } else {
            return source.renameTo(target); 
        }
    }

    static boolean copyFile(File source, File target) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            if(inputStream == null) {
                return false;
            }
            outputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                outputStream.write(buffer, 0, len);
                len = inputStream.read(buffer);
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {

                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
