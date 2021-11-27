package utility;

public class Utility {
    public enum MsgLevel {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
    }

    // Terminal output colour
    static final String RESET = "\033[0m";
    static final String RED = "\033[0;31m";
    static final String GREEN = "\033[0;32m";
    static final String YELLOW = "\033[0;33m";
    static final String BLUE = "\033[0;34m";

    public static void PrintLog(String msg, MsgLevel level) {
        switch (level) {
            case INFO -> System.out.println(BLUE + "[Info] " + msg + RESET);
            case SUCCESS -> System.out.println(GREEN + "[Success] " + msg + RESET);
            case WARNING -> System.out.println(YELLOW + "[Warning] " + msg + RESET);
            case ERROR -> System.out.println(RED + "[Error] " + msg + RESET);
        }
    }
}
