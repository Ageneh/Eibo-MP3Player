package misc;

public enum ANSI {

    BLUE("\033[34m"),
    RED("\033[31m"),
    GREEN("\033[32m"),
    YELLOW("\033[33m"),
    BLACK("\033[0m"),
    MAGENTA("\033[35m"),
    CYAN("\033[36m"),
    WHITE("\033[37m");

    private String esc;
    private final String end = "\033[0m";

    ANSI(String esc){
        this.esc = esc;
    }

    public String colorize(String str) {
        return esc + str + end;
    }

    public void print(String str) {
        System.out.print(esc + str + end);
    }

    public void println(String str) {
        this.print(str + System.lineSeparator());
    }
}
