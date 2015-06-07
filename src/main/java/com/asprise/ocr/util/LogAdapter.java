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
package com.asprise.ocr.util;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogAdapter {
    static enum LogType {JAVA, SLF4J, LOG4J, PRINTSTREAM};

    LogType logType;
    Object loggerObject;

    static Class clsSlf4JLoggerInterface;
    static Method methodSlf4JDebug;
    static Method methodSlf4JInfo;
    static Method methodSlf4JWarn;
    static Method methodSlf4JError;

    static Class clsLog4jLoggerCls;
    static Method methodLog4jDebug;
    static Method methodLog4jInfo;
    static Method methodLog4jWarn;
    static Method methodLog4jError;

    static {
        try {
            clsSlf4JLoggerInterface = Class.forName("org.slf4j.Logger", false, LogAdapter.class.getClassLoader());
            methodSlf4JDebug = clsSlf4JLoggerInterface.getMethod("debug", String.class, Throwable.class);
            methodSlf4JInfo = clsSlf4JLoggerInterface.getMethod("info", String.class, Throwable.class);
            methodSlf4JWarn = clsSlf4JLoggerInterface.getMethod("warn", String.class, Throwable.class);
            methodSlf4JError = clsSlf4JLoggerInterface.getMethod("error", String.class, Throwable.class);
        } catch (ClassNotFoundException e) {

        } catch (NoSuchMethodException me) {
            me.printStackTrace();
        }

        try {
            clsLog4jLoggerCls = Class.forName("org.apache.log4j.Logger", false, LogAdapter.class.getClassLoader());
            methodLog4jDebug = clsLog4jLoggerCls.getMethod("debug", Object.class, Throwable.class);
            methodLog4jInfo = clsLog4jLoggerCls.getMethod("info", Object.class, Throwable.class);
            methodLog4jWarn = clsLog4jLoggerCls.getMethod("warn", Object.class, Throwable.class);
            methodLog4jError = clsLog4jLoggerCls.getMethod("error", Object.class, Throwable.class);
        } catch (ClassNotFoundException e) {

        } catch (NoSuchMethodException me) {
            me.printStackTrace();
        }
    }

    public LogAdapter(Object loggerObject) {
        if(loggerObject == null) {
            throw new IllegalArgumentException("logger object can not be null.");
        }
        if(loggerObject instanceof Logger) {
            logType = LogType.JAVA;
        } else if(clsSlf4JLoggerInterface != null && clsSlf4JLoggerInterface.isInstance(loggerObject)) {
            logType = LogType.SLF4J;
        } else if(clsLog4jLoggerCls != null && clsLog4jLoggerCls.isInstance(loggerObject)) {
            logType = LogType.LOG4J;
        } else if(loggerObject instanceof PrintStream) {
            logType = LogType.PRINTSTREAM;
        } else {
            throw new IllegalArgumentException("Expecting a java.util.logging.Logger, org.slf4j.Logger, org.apache.log4j.Logger or java.io.PrintStream, actual: " + loggerObject.getClass());
        }
        this.loggerObject = loggerObject;
    }

    public void debug(String mesg, Throwable t) {
        switch (logType) {
            case JAVA:
                ((Logger) loggerObject).log(Level.FINE, mesg, t);
                break;

            case PRINTSTREAM:
                ((PrintStream) loggerObject).println(String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-16s %3$5s %4$s", new GregorianCalendar(), Thread.currentThread().getName(), "DEBUG", mesg));
                if(t != null) {
                    t.printStackTrace((PrintStream) loggerObject);
                }
                break;

            case SLF4J:
                try {
                    methodSlf4JDebug.invoke(loggerObject, mesg, t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LOG4J:
                try {
                    methodLog4jDebug.invoke(loggerObject, mesg, t);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void info(String mesg, Throwable t) {
        switch (logType) {
            case JAVA:
                ((Logger) loggerObject).log(Level.INFO, mesg, t);
                break;

            case PRINTSTREAM:
                ((PrintStream) loggerObject).println(String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-10s %3$5s %4$s", new GregorianCalendar(), Thread.currentThread().getName(), " INFO", mesg));
                if(t != null) {
                    t.printStackTrace((PrintStream) loggerObject);
                }
                break;

            case SLF4J:
                try {
                    methodSlf4JInfo.invoke(loggerObject, mesg, t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LOG4J:
                try {
                    methodLog4jInfo.invoke(loggerObject, mesg, t);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void warn(String mesg, Throwable t) {
        switch (logType) {
            case JAVA:
                ((Logger) loggerObject).log(Level.WARNING, mesg, t);
                break;

            case PRINTSTREAM:
                ((PrintStream) loggerObject).println(String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-10s %3$5s %4$s", new GregorianCalendar(), Thread.currentThread().getName(), " WARN", mesg));
                if(t != null) {
                    t.printStackTrace((PrintStream) loggerObject);
                }
                break;

            case SLF4J:
                try {
                    methodSlf4JWarn.invoke(loggerObject, mesg, t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LOG4J:
                try {
                    methodLog4jWarn.invoke(loggerObject, mesg, t);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void error(String mesg, Throwable t) {
        switch (logType) {
            case JAVA:
                ((Logger) loggerObject).log(Level.SEVERE, mesg, t);
                break;

            case PRINTSTREAM:
                ((PrintStream) loggerObject).println(String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-10s %3$5s %4$s", new GregorianCalendar(), Thread.currentThread().getName(), "ERROR", mesg));
                if(t != null) {
                    t.printStackTrace((PrintStream) loggerObject);
                }
                break;

            case SLF4J:
                try {
                    methodSlf4JError.invoke(loggerObject, mesg, t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LOG4J:
                try {
                    methodLog4jError.invoke(loggerObject, mesg, t);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
