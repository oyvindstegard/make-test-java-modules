package net.stegard.make.java;

public class Logger {

    public static void log(String format, Object...args) {
        logImpl(format, args);
    }

    public static void log(Object obj) {
        logImpl("%s", obj);
    }

    private static void logImpl(String format, Object...args) {
        StackTraceElement callerFrame = Thread.currentThread().getStackTrace()[3];
        Object[] formatArgs = new Object[2 + args.length];
        formatArgs[0] = callerFrame.getClassName().replaceAll("net.stegard.make.java", "n.s.m.j");
        formatArgs[1] = callerFrame.getMethodName();
        System.arraycopy(args, 0, formatArgs, 2, args.length);
        System.out.println(String.format("%s#%s(): " + format, formatArgs));
    }

}
