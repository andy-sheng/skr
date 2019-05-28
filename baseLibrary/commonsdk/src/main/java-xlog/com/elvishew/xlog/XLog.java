/*
 * Copyright 2015 Elvis Hew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elvishew.xlog;

import com.elvishew.xlog.formatter.border.BorderFormatter;
import com.elvishew.xlog.formatter.message.json.JsonFormatter;
import com.elvishew.xlog.formatter.message.object.ObjectFormatter;
import com.elvishew.xlog.formatter.message.throwable.ThrowableFormatter;
import com.elvishew.xlog.formatter.message.xml.XmlFormatter;
import com.elvishew.xlog.formatter.stacktrace.StackTraceFormatter;
import com.elvishew.xlog.formatter.thread.ThreadFormatter;
import com.elvishew.xlog.interceptor.Interceptor;
import com.elvishew.xlog.internal.DefaultsFactory;
import com.elvishew.xlog.internal.Platform;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.PrinterSet;

public class XLog {

    /**
     * Global logger for all direct logging via {@link XLog}.
     */
    private static Logger sLogger;

    /**
     * Global log configuration.
     */
    static LogConfiguration sLogConfiguration;

    /**
     * Global log printer.
     */
    static Printer sPrinter;

    static boolean sIsInitialized;

    /**
     * Prevent instance.
     */
    private XLog() {
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @since 1.3.0
     */
    public static void init() {
        init(new LogConfiguration.Builder().build(), DefaultsFactory.createPrinter());
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel the log level, logs with a lower level than which would not be printed
     */
    public static void init(int logLevel) {
        init(new LogConfiguration.Builder().logLevel(logLevel).build(),
                DefaultsFactory.createPrinter());
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel         the log level, logs with a lower level than which would not be printed
     * @param logConfiguration the log configuration
     * @deprecated the log level is part of log configuration now, use {@link #init(LogConfiguration)}
     * instead, since 1.3.0
     */
    @Deprecated
    public static void init(int logLevel, LogConfiguration logConfiguration) {
        init(new LogConfiguration.Builder(logConfiguration).logLevel(logLevel).build());
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logConfiguration the log configuration
     * @since 1.3.0
     */
    public static void init(LogConfiguration logConfiguration) {
        init(logConfiguration, DefaultsFactory.createPrinter());
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param printers the printers, each log would be printed by all of the printers
     * @since 1.3.0
     */
    public static void init(Printer... printers) {
        init(new LogConfiguration.Builder().build(), printers);
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel the log level, logs with a lower level than which would not be printed
     * @param printers the printers, each log would be printed by all of the printers
     */
    public static void init(int logLevel, Printer... printers) {
        init(new LogConfiguration.Builder().logLevel(logLevel).build(), printers);
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel         the log level, logs with a lower level than which would not be printed
     * @param logConfiguration the log configuration
     * @param printers         the printers, each log would be printed by all of the printers
     * @deprecated the log level is part of log configuration now,
     * use {@link #init(LogConfiguration, Printer...)} instead, since 1.3.0
     */
    @Deprecated
    public static void init(int logLevel, LogConfiguration logConfiguration, Printer... printers) {
        init(new LogConfiguration.Builder(logConfiguration).logLevel(logLevel).build(), printers);
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logConfiguration the log configuration
     * @param printers         the printers, each log would be printed by all of the printers
     * @since 1.3.0
     */
    public static void init(LogConfiguration logConfiguration, Printer... printers) {
        if (sIsInitialized) {
            Platform.get().warn("XLog is already initialized, do not initialize again");
        }
        sIsInitialized = true;

        if (logConfiguration == null) {
            throw new IllegalArgumentException("Please specify a LogConfiguration");
        }
        sLogConfiguration = logConfiguration;

        sPrinter = new PrinterSet(printers);

        sLogger = new Logger(sLogConfiguration, sPrinter);
    }

    /**
     * Throw an IllegalStateException if not initialized.
     */
    static void assertInitialization() {
        if (!sIsInitialized) {
            throw new IllegalStateException("Do you forget to initialize XLog?");
        }
    }

    /**
     * Start to customize a {@link Logger} and set the log level.
     *
     * @param logLevel the log level to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     * @since 1.3.0
     */
    public static Logger.Builder logLevel(int logLevel) {
        return new Logger.Builder().logLevel(logLevel);
    }

    /**
     * Start to customize a {@link Logger} and set the tag.
     *
     * @param tag the tag to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder tag(String tag) {
        return new Logger.Builder().tag(tag);
    }

    /**
     * Start to customize a {@link Logger} and enable thread info.
     *
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder t() {
        return new Logger.Builder().t();
    }

    /**
     * Start to customize a {@link Logger} and disable thread info.
     *
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder nt() {
        return new Logger.Builder().nt();
    }

    /**
     * Start to customize a {@link Logger} and enable stack trace.
     *
     * @param depth the number of stack trace elements we should log, 0 if no limitation
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder st(int depth) {
        return new Logger.Builder().st(depth);
    }

    /**
     * Start to customize a {@link Logger} and enable stack trace.
     *
     * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log,
     *                         it can be a package name like "com.elvishew.xlog", a class name
     *                         like "com.yourdomain.logWrapper", or something else between
     *                         package name and class name, like "com.yourdomain.".
     *                         It is mostly used when you are using a logger wrapper
     * @param depth            the number of stack trace elements we should log, 0 if no limitation
     * @return the {@link Logger.Builder} to build the {@link Logger}
     * @since 1.4.0
     */
    public static Logger.Builder st(String stackTraceOrigin, int depth) {
        return new Logger.Builder().st(stackTraceOrigin, depth);
    }

    /**
     * Start to customize a {@link Logger} and disable stack trace.
     *
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder nst() {
        return new Logger.Builder().nst();
    }

    /**
     * Start to customize a {@link Logger} and enable border.
     *
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder b() {
        return new Logger.Builder().b();
    }

    /**
     * Start to customize a {@link Logger} and disable border.
     *
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder nb() {
        return new Logger.Builder().nb();
    }

    /**
     * Start to customize a {@link Logger} and set the {@link JsonFormatter}.
     *
     * @param jsonFormatter the {@link JsonFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder jsonFormatter(JsonFormatter jsonFormatter) {
        return new Logger.Builder().jsonFormatter(jsonFormatter);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link XmlFormatter}.
     *
     * @param xmlFormatter the {@link XmlFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder xmlFormatter(XmlFormatter xmlFormatter) {
        return new Logger.Builder().xmlFormatter(xmlFormatter);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link ThrowableFormatter}.
     *
     * @param throwableFormatter the {@link ThrowableFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder throwableFormatter(ThrowableFormatter throwableFormatter) {
        return new Logger.Builder().throwableFormatter(throwableFormatter);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link ThreadFormatter}.
     *
     * @param threadFormatter the {@link ThreadFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder threadFormatter(ThreadFormatter threadFormatter) {
        return new Logger.Builder().threadFormatter(threadFormatter);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link StackTraceFormatter}.
     *
     * @param stackTraceFormatter the {@link StackTraceFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder stackTraceFormatter(StackTraceFormatter stackTraceFormatter) {
        return new Logger.Builder().stackTraceFormatter(stackTraceFormatter);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link BorderFormatter}.
     *
     * @param borderFormatter the {@link BorderFormatter} to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder borderFormatter(BorderFormatter borderFormatter) {
        return new Logger.Builder().borderFormatter(borderFormatter);
    }

    /**
     * Start to customize a {@link Logger} and add an object formatter for specific class of object.
     *
     * @param objectClass     the class of object
     * @param objectFormatter the object formatter to add
     * @param <T>             the type of object
     * @return the {@link Logger.Builder} to build the {@link Logger}
     * @since 1.1.0
     */
    public static <T> Logger.Builder addObjectFormatter(Class<T> objectClass,
                                                        ObjectFormatter<? super T> objectFormatter) {
        return new Logger.Builder().addObjectFormatter(objectClass, objectFormatter);
    }

    /**
     * Start to customize a {@link Logger} and add an interceptor.
     *
     * @param interceptor the interceptor to add
     * @return the {@link Logger.Builder} to build the {@link Logger}
     * @since 1.3.0
     */
    public static Logger.Builder addInterceptor(Interceptor interceptor) {
        return new Logger.Builder().addInterceptor(interceptor);
    }

    /**
     * Start to customize a {@link Logger} and set the {@link Printer} array.
     *
     * @param printers the {@link Printer} array to customize
     * @return the {@link Logger.Builder} to build the {@link Logger}
     */
    public static Logger.Builder printers(Printer... printers) {
        return new Logger.Builder().printers(printers);
    }

    /**
     * Log an object with level {@link LogLevel#VERBOSE}.
     *
     * @param object the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.1.0
     */
    public static void v(String tag, Object object) {
        assertInitialization();
        sLogger.v(tag, object);
    }

    /**
     * Log an array with level {@link LogLevel#VERBOSE}.
     *
     * @param array the array to log
     */
    public static void v(String tag, Object[] array) {
        assertInitialization();
        sLogger.v(tag, array);
    }


    /**
     * Log a message with level {@link LogLevel#VERBOSE}.
     *
     * @param msg the message to log
     */
    public static void v(String tag, String msg) {
        assertInitialization();
        sLogger.v(tag, msg);
    }

    /**
     * Log a message and a throwable with level {@link LogLevel#VERBOSE}.
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    public static void v(String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.v(tag, msg, tr);
    }

    /**
     * Log an object with level {@link LogLevel#DEBUG}.
     *
     * @param object the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.1.0
     */
    public static void d(String tag, Object object) {
        assertInitialization();
        sLogger.d(tag, object);
    }

    /**
     * Log an array with level {@link LogLevel#DEBUG}.
     *
     * @param array the array to log
     */
    public static void d(String tag, Object[] array) {
        assertInitialization();
        sLogger.d(tag, array);
    }


    /**
     * Log a message with level {@link LogLevel#DEBUG}.
     *
     * @param msg the message to log
     */
    public static void d(String tag, String msg) {
        assertInitialization();
        sLogger.d(tag, msg);
    }

    /**
     * Log a message and a throwable with level {@link LogLevel#DEBUG}.
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    public static void d(String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.d(tag, msg, tr);
    }

    /**
     * Log an object with level {@link LogLevel#INFO}.
     *
     * @param object the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.1.0
     */
    public static void i(String tag, Object object) {
        assertInitialization();
        sLogger.i(tag, object);
    }

    /**
     * Log an array with level {@link LogLevel#INFO}.
     *
     * @param array the array to log
     */
    public static void i(String tag, Object[] array) {
        assertInitialization();
        sLogger.i(tag, array);
    }

    /**
     * Log a message with level {@link LogLevel#INFO}.
     *
     * @param msg the message to log
     */
    public static void i(String tag, String msg) {
        assertInitialization();
        sLogger.i(tag, msg);
    }

    /**
     * Log a message and a throwable with level {@link LogLevel#INFO}.
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    public static void i(String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.i(tag, msg, tr);
    }

    /**
     * Log an object with level {@link LogLevel#WARN}.
     *
     * @param object the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.1.0
     */
    public static void w(String tag, Object object) {
        assertInitialization();
        sLogger.w(tag, object);
    }

    /**
     * Log an array with level {@link LogLevel#WARN}.
     *
     * @param array the array to log
     */
    public static void w(String tag, Object[] array) {
        assertInitialization();
        sLogger.w(tag, array);
    }

    /**
     * Log a message with level {@link LogLevel#WARN}.
     *
     * @param msg the message to log
     */
    public static void w(String tag, String msg) {
        assertInitialization();
        sLogger.w(tag, msg);
    }

    /**
     * Log a message and a throwable with level {@link LogLevel#WARN}.
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    public static void w(String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.w(tag, msg, tr);
    }

    /**
     * Log an object with level {@link LogLevel#ERROR}.
     *
     * @param object the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.1.0
     */
    public static void e(String tag, Object object) {
        assertInitialization();
        sLogger.e(tag, object);
    }

    /**
     * Log an array with level {@link LogLevel#ERROR}.
     *
     * @param array the array to log
     */
    public static void e(String tag, Object[] array) {
        assertInitialization();
        sLogger.e(tag, array);
    }

    /**
     * Log a message with level {@link LogLevel#ERROR}.
     *
     * @param msg the message to log
     */
    public static void e(String tag, String msg) {
        assertInitialization();
        sLogger.e(tag, msg);
    }

    /**
     * Log a message and a throwable with level {@link LogLevel#ERROR}.
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    public static void e(String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.e(tag, msg, tr);
    }

    /**
     * Log an object with specific log level.
     *
     * @param logLevel the specific log level
     * @param object   the object to log
     * @see LogConfiguration.Builder#addObjectFormatter(Class, ObjectFormatter)
     * @since 1.4.0
     */
    public static void log(int logLevel, String tag, Object object) {
        assertInitialization();
        sLogger.log(logLevel, tag, object);
    }

    /**
     * Log an array with specific log level.
     *
     * @param logLevel the specific log level
     * @param array    the array to log
     * @since 1.4.0
     */
    public static void log(int logLevel, String tag, Object[] array) {
        assertInitialization();
        sLogger.log(logLevel, tag, array);
    }


    /**
     * Log a message with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     * @since 1.4.0
     */
    public static void log(int logLevel, String tag, String msg) {
        assertInitialization();
        sLogger.log(logLevel, tag, msg);
    }

    /**
     * Log a message and a throwable with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     * @param tr       the throwable to be log
     * @since 1.4.0
     */
    public static void log(int logLevel, String tag, String msg, Throwable tr) {
        assertInitialization();
        sLogger.log(logLevel, tag, msg, tr);
    }

    /**
     * Log a JSON string, with level {@link LogLevel#DEBUG} by default.
     *
     * @param json the JSON string to log
     */
    public static void json(String tag, String json) {
        assertInitialization();
        sLogger.json(tag, json);
    }

    /**
     * Log a XML string, with level {@link LogLevel#DEBUG} by default.
     *
     * @param xml the XML string to log
     */
    public static void xml(String tag, String xml) {
        assertInitialization();
        sLogger.xml(tag, xml);
    }

}
