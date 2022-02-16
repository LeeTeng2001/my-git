package helper;

public final class Utility {
    public enum MsgLevel {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
    }

    // Terminal output colour
    public static final String RESET = "\033[0m";
    static final String RED = "\033[0;31m";
    static final String GREEN = "\033[0;32m";
    static final String YELLOW = "\033[0;33m";
    static final String BLUE = "\033[0;34m";

    // Special public color
    public static final String RED_BOLD = "\033[1;31m";
    public static final String YELLOW_BOLD = "\033[1;33m";

    // Useful for debugging and can turn this into log in the future
    public static void printLog(String msg, MsgLevel level) {
        switch (level) {
            case INFO -> System.out.println(BLUE + "[Info] " + msg + RESET);
            case SUCCESS -> System.out.println(GREEN + "[Success] " + msg + RESET);
            case WARNING -> System.out.println(YELLOW + "[Warning] " + msg + RESET);
            case ERROR -> System.out.println(RED + "[Error] " + msg + RESET);
        }
    }

    // Missing method from byte operation
    public static int indexOfByte(byte[] array, byte target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    // Overloaded function that does the same thing, why can't java have default parameter val
    public static int indexOfByte(byte[] array, byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
