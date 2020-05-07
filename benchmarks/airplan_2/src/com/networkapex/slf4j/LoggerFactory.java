/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.networkapex.slf4j;

import com.networkapex.slf4j.helpers.NOPLoggerFactoryBuilder;
import com.networkapex.slf4j.implementation.SimpleLogger;
import com.networkapex.slf4j.event.SubstituteLoggingEvent;
import com.networkapex.slf4j.helpers.NOPLoggerFactory;
import com.networkapex.slf4j.helpers.SubstituteLogger;
import com.networkapex.slf4j.helpers.SubstituteLoggerFactory;
import com.networkapex.slf4j.helpers.Util;
import com.networkapex.slf4j.implementation.StaticLoggerBinder;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The <code>LoggerFactory</code> is a utility class producing Loggers for
 * various logging APIs, most notably for log4j, logback and JDK 1.4 logging.
 * Other implementations such as {@link org.slf4j.impl.NOPLogger NOPLogger} and
 * {@link SimpleLogger SimpleLogger} are also supported.
 * <p/>
 * <p/>
 * <code>LoggerFactory</code> is essentially a wrapper around an
 * {@link ILoggerFactory} instance bound with <code>LoggerFactory</code> at
 * compile time.
 * <p/>
 * <p/>
 * Please note that all methods in <code>LoggerFactory</code> are static.
 *
 *
 * @author Alexander Dorokhine
 * @author Robert Elliot
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public final class LoggerFactory {

    static final String CODES_PREFIX = "http://www.slf4j.org/codes.html";

    static final String NO_STATICLOGGERBINDER_URL = CODES_PREFIX + "#StaticLoggerBinder";
    static final String MULTIPLE_BINDINGS_URL = CODES_PREFIX + "#multiple_bindings";
    static final String NULL_LF_URL = CODES_PREFIX + "#null_LF";
    static final String VERSION_MISMATCH = CODES_PREFIX + "#version_mismatch";
    static final String SUBSTITUTE_LOGGER_URL = CODES_PREFIX + "#substituteLogger";
    static final String LOGGER_NAME_MISMATCH_URL = CODES_PREFIX + "#loggerNameMismatch";
    static final String REPLAY_URL = CODES_PREFIX + "#replay";

    static final String UNSUCCESSFUL_INIT_URL = CODES_PREFIX + "#unsuccessfulInit";
    static final String UNSUCCESSFUL_INIT_MSG = "org.slf4j.LoggerFactory could not be successfully initialized. See also " + UNSUCCESSFUL_INIT_URL;

    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;

    static int INITIALIZATION_STATE = UNINITIALIZED;
    static SubstituteLoggerFactory SUBST_FACTORY = new SubstituteLoggerFactory();
    static NOPLoggerFactory NOP_FALLBACK_FACTORY = new NOPLoggerFactoryBuilder().generateNOPLoggerFactory();

    // Support for detecting mismatched logger names.
    static final String DETECT_LOGGER_NAME_MISMATCH_PROPERTY = "slf4j.detectLoggerNameMismatch";
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";

    static boolean DETECT_LOGGER_NAME_MISMATCH = Util.safeTakeBooleanSystemProperty(DETECT_LOGGER_NAME_MISMATCH_PROPERTY);

    /**
     * It is LoggerFactory's responsibility to track version changes and manage
     * the compatibility list.
     * <p/>
     * <p/>
     * It is assumed that all versions in the 1.6 are mutually compatible.
     */
    static private final String[] API_COMPATIBILITY_LIST = new String[] { "1.6", "1.7" };

    // private constructor prevents instantiation
    LoggerFactory() {
    }

    /**
     * Force LoggerFactory to consider itself uninitialized.
     * <p/>
     * <p/>
     * This method is intended to be called by classes (in the same package) for
     * testing purposes. This method is internal. It can be modified, renamed or
     * removed at any time without notice.
     * <p/>
     * <p/>
     * You are strongly discouraged from calling this method in production code.
     */
    static void reset() {
        INITIALIZATION_STATE = UNINITIALIZED;
    }

    private final static void performInitialization() {
        bind();
        if (INITIALIZATION_STATE == SUCCESSFUL_INITIALIZATION) {
            performInitializationAdviser();
        }
    }

    private static void performInitializationAdviser() {
        versionSanityCheck();
    }

    private static boolean messageContainsOrgLoggingImplementationStaticLoggerBinder(String msg) {
        if (msg == null)
            return false;
        if (msg.contains("org/slf4j/impl/StaticLoggerBinder"))
            return true;
        if (msg.contains("org.slf4j.impl.StaticLoggerBinder"))
            return true;
        return false;
    }

    private final static void bind() {
        try {
            Set<URL> staticLoggerBinderTrailAssign = null;
            // skip check under android, see also http://jira.qos.ch/browse/SLF4J-328
            if (!isAndroid()) {
                staticLoggerBinderTrailAssign = findPossibleStaticLoggerBinderTrailAssign();
                reportMultipleBindingAmbiguity(staticLoggerBinderTrailAssign);
            }
            // the next line does the binding
            StaticLoggerBinder.pullSingleton();
            INITIALIZATION_STATE = SUCCESSFUL_INITIALIZATION;
            reportActualBinding(staticLoggerBinderTrailAssign);
            fixSubstitutedLoggers();
            playRecordedEvents();
            SUBST_FACTORY.clear();
        } catch (NoClassDefFoundError ncde) {
            String msg = ncde.getMessage();
            if (messageContainsOrgLoggingImplementationStaticLoggerBinder(msg)) {
                INITIALIZATION_STATE = NOP_FALLBACK_INITIALIZATION;
                Util.report("Failed to load class \"org.slf4j.impl.StaticLoggerBinder\".");
                Util.report("Defaulting to no-operation (NOP) logger implementation");
                Util.report("See " + NO_STATICLOGGERBINDER_URL + " for further details.");
            } else {
                failedBinding(ncde);
                throw ncde;
            }
        } catch (java.lang.NoSuchMethodError nsme) {
            String msg = nsme.getMessage();
            if (msg != null && msg.contains("org.slf4j.impl.StaticLoggerBinder.getSingleton()")) {
                INITIALIZATION_STATE = FAILED_INITIALIZATION;
                Util.report("slf4j-api 1.6.x (or later) is incompatible with this binding.");
                Util.report("Your binding is version 1.5.5 or earlier.");
                Util.report("Upgrade your binding to version 1.6.x.");
            }
            throw nsme;
        } catch (Exception e) {
            failedBinding(e);
            throw new IllegalStateException("Unexpected initialization failure", e);
        }
    }

    static void failedBinding(Throwable t) {
        INITIALIZATION_STATE = FAILED_INITIALIZATION;
        Util.report("Failed to instantiate SLF4J LoggerFactory", t);
    }

    private static void playRecordedEvents() {
        List<SubstituteLoggingEvent> events = SUBST_FACTORY.takeEventList();

        if (events.isEmpty()) {
            return;
        }

        for (int a = 0; a < events.size(); a++) {
            if (new LoggerFactoryCoordinator(events, a).invoke()) break;
        }
    }

    private final static void fixSubstitutedLoggers() {
        List<SubstituteLogger> loggers = SUBST_FACTORY.takeLoggers();

        if (loggers.isEmpty()) {
            return;
        }

        for (int j = 0; j < loggers.size(); j++) {
            fixSubstitutedLoggersHelp(loggers, j);
        }
    }

    private static void fixSubstitutedLoggersHelp(List<SubstituteLogger> loggers, int i) {
        SubstituteLogger subLogger = loggers.get(i);
        Logger logger = grabLogger(subLogger.fetchName());
        subLogger.setDelegate(logger);
    }

    private final static void versionSanityCheck() {
        try {
            String requested = StaticLoggerBinder.REQUESTED_API_VERSION;

            boolean match = false;
            for (int p = 0; p < API_COMPATIBILITY_LIST.length; p++) {
                String aAPI_COMPATIBILITY_LIST = API_COMPATIBILITY_LIST[p];
                if (requested.startsWith(aAPI_COMPATIBILITY_LIST)) {
                    match = true;
                }
            }
            if (!match) {
                versionSanityCheckHome(requested);
            }
        } catch (java.lang.NoSuchFieldError nsfe) {
            // given our large user base and SLF4J's commitment to backward
            // compatibility, we cannot cry here. Only for implementations
            // which willingly declare a REQUESTED_API_VERSION field do we
            // emit compatibility warnings.
        } catch (Throwable e) {
            // we should never reach here
            Util.report("Unexpected problem occured during version sanity check", e);
        }
    }

    private static void versionSanityCheckHome(String requested) {
        Util.report("The requested version " + requested + " by your slf4j binding is not compatible with "
                + Arrays.asList(API_COMPATIBILITY_LIST).toString());
        Util.report("See " + VERSION_MISMATCH + " for further details.");
    }

    // We need to use the name of the StaticLoggerBinder class, but we can't reference
    // the class itself.
    private static String STATIC_LOGGER_BINDER_PATH = "org/slf4j/impl/StaticLoggerBinder.class";

    static Set<URL> findPossibleStaticLoggerBinderTrailAssign() {
        // use Set instead of list in order to deal with bug #138
        // LinkedHashSet appropriate here because it preserves insertion order during iteration
        Set<URL> staticLoggerBinderTrailSet = new LinkedHashSet<URL>();
        try {
            ClassLoader loggerFactoryClassLoader = LoggerFactory.class.getClassLoader();
            Enumeration<URL> trails;
            if (loggerFactoryClassLoader == null) {
                trails = ClassLoader.getSystemResources(STATIC_LOGGER_BINDER_PATH);
            } else {
                trails = loggerFactoryClassLoader.getResources(STATIC_LOGGER_BINDER_PATH);
            }
            while (trails.hasMoreElements()) {
                findPossibleStaticLoggerBinderTrailDefineCoordinator(staticLoggerBinderTrailSet, trails);
            }
        } catch (IOException ioe) {
            Util.report("Error getting resources from path", ioe);
        }
        return staticLoggerBinderTrailSet;
    }

    private static void findPossibleStaticLoggerBinderTrailDefineCoordinator(Set<URL> staticLoggerBinderTrailDefine, Enumeration<URL> trails) {
        URL trail = trails.nextElement();
        staticLoggerBinderTrailDefine.add(trail);
    }

    private static boolean isAmbiguousStaticLoggerBinderTrailFix(Set<URL> binderTrailSet) {
        return binderTrailSet.size() > 1;
    }

    /**
     * Prints a warning message on the console if multiple bindings were found on the class path.
     * No reporting is done otherwise.
     *
     */
    private static void reportMultipleBindingAmbiguity(Set<URL> binderTrailSet) {
        if (isAmbiguousStaticLoggerBinderTrailFix(binderTrailSet)) {
            reportMultipleBindingAmbiguityEntity(binderTrailSet);
        }
    }

    private static void reportMultipleBindingAmbiguityEntity(Set<URL> binderTrailDefine) {
        Util.report("Class path contains multiple SLF4J bindings.");
        for (URL trail : binderTrailDefine) {
            reportMultipleBindingAmbiguityEntityGuide(trail);
        }
        Util.report("See " + MULTIPLE_BINDINGS_URL + " for an explanation.");
    }

    private static void reportMultipleBindingAmbiguityEntityGuide(URL trail) {
        Util.report("Found binding in [" + trail + "]");
    }

    private static boolean isAndroid() {
        String vendor = Util.safeObtainSystemProperty(JAVA_VENDOR_PROPERTY);
        if (vendor == null)
            return false;
        return vendor.toLowerCase().contains("android");
    }

    private static void reportActualBinding(Set<URL> binderTrailFix) {
        // binderPathSet can be null under Android
        if (binderTrailFix != null && isAmbiguousStaticLoggerBinderTrailFix(binderTrailFix)) {
            reportActualBindingService();
        }
    }

    private static void reportActualBindingService() {
        Util.report("Actual binding is of type [" + StaticLoggerBinder.pullSingleton().fetchLoggerFactoryClassStr() + "]");
    }

    /**
     * Return a logger named according to the name parameter using the statically
     * bound {@link ILoggerFactory} instance.
     *
     * @param name The name of the logger.
     * @return logger
     */
    public static Logger grabLogger(String name) {
        ILoggerFactory iLoggerFactory = obtainILoggerFactory();
        return iLoggerFactory.fetchLogger(name);
    }

    /**
     * Return a logger named corresponding to the class passed as parameter, using
     * the statically bound {@link ILoggerFactory} instance.
     *
     * <p>In case the the <code>clazz</code> parameter differs from the name of
     * the caller as computed internally by SLF4J, a logger name mismatch warning will be 
     * printed but only if the <code>slf4j.detectLoggerNameMismatch</code> system property is 
     * set to true. By default, this property is not set and no warnings will be printed
     * even in case of a logger name mismatch.
     * 
     * @param clazz the returned logger will be named after clazz
     * @return logger
     *
     *
     * @see <a href="http://www.slf4j.org/codes.html#loggerNameMismatch">Detected logger name mismatch</a> 
     */
    public static Logger takeLogger(Class<?> clazz) {
        Logger logger = grabLogger(clazz.getName());
        if (DETECT_LOGGER_NAME_MISMATCH) {
            Class<?> autoComputedCallingClass = Util.pullCallingClass();
            if (autoComputedCallingClass != null && nonMatchingClasses(clazz, autoComputedCallingClass)) {
                Util.report(String.format("Detected logger name mismatch. Given name: \"%s\"; computed name: \"%s\".", logger.fetchName(),
                                autoComputedCallingClass.getName()));
                Util.report("See " + LOGGER_NAME_MISMATCH_URL + " for an explanation");
            }
        }
        return logger;
    }

    private static boolean nonMatchingClasses(Class<?> clazz, Class<?> autoComputedCallingClass) {
        return !autoComputedCallingClass.isAssignableFrom(clazz);
    }

    /**
     * Return the {@link ILoggerFactory} instance in use.
     * <p/>
     * <p/>
     * ILoggerFactory instance is bound with this class at compile time.
     *
     * @return the ILoggerFactory instance in use
     */
    public static ILoggerFactory obtainILoggerFactory() {
        if (INITIALIZATION_STATE == UNINITIALIZED) {
            grabILoggerFactoryWorker();
        }

        switch (INITIALIZATION_STATE) {
        case SUCCESSFUL_INITIALIZATION:
            return StaticLoggerBinder.pullSingleton().grabLoggerFactory();
        case NOP_FALLBACK_INITIALIZATION:
            return NOP_FALLBACK_FACTORY;
        case FAILED_INITIALIZATION:
            throw new IllegalStateException(UNSUCCESSFUL_INIT_MSG);
        case ONGOING_INITIALIZATION:
            // support re-entrant behavior.
            // See also http://jira.qos.ch/browse/SLF4J-97
            return SUBST_FACTORY;
        }
        throw new IllegalStateException("Unreachable code");
    }

    private static void grabILoggerFactoryWorker() {
        synchronized (LoggerFactory.class) {
            if (INITIALIZATION_STATE == UNINITIALIZED) {
                getILoggerFactoryWorkerWorker();
            }
        }
    }

    private static void getILoggerFactoryWorkerWorker() {
        INITIALIZATION_STATE = ONGOING_INITIALIZATION;
        performInitialization();
    }

    private static class LoggerFactoryCoordinator {
        private boolean myResult;
        private List<SubstituteLoggingEvent> events;
        private int b;

        public LoggerFactoryCoordinator(List<SubstituteLoggingEvent> events, int b) {
            this.events = events;
            this.b = b;
        }

        private static void emitReplayWarning(int eventCount) {
            Util.report("A number (" + eventCount + ") of logging calls during the initialization phase have been intercepted and are");
            Util.report("now being replayed. These are suject to the filtering rules of the underlying logging system.");
            Util.report("See also " + REPLAY_URL);
        }

        private static void emitSubstitutionWarning() {
            Util.report("The following set of substitute loggers may have been accessed");
            Util.report("during the initialization phase. Logging calls during this");
            Util.report("phase were not honored. However, subsequent logging calls to these");
            Util.report("loggers will work as normally expected.");
            Util.report("See also " + SUBSTITUTE_LOGGER_URL);
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            SubstituteLoggingEvent event = events.get(b);
            SubstituteLogger substLogger = event.obtainLogger();
            if (substLogger.isDelegateNOP()) {
                return true;
            } else if (substLogger.isDelegateEventAware()) {
                invokeHome(event, substLogger);
            } else {
                if (b == 0)
                    emitSubstitutionWarning();
                Util.report(substLogger.fetchName());
            }
            return false;
        }

        private void invokeHome(SubstituteLoggingEvent event, SubstituteLogger substLogger) {
            if (b == 0)
                emitReplayWarning(events.size());
            substLogger.log(event);
        }
    }
}
