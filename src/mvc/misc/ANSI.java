package mvc.misc;

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
//        System.out.println("\033[0m BLACK");
//        System.out.println("\033[31m RED");
//        System.out.println("\033[32m GREEN");
//        System.out.println("\033[33m YELLOW");
//        System.out.println("\033[34m BLUE");
//        System.out.println("\033[35m MAGENTA");
//        System.out.println("\033[36m CYAN");
//        System.out.println("\033[37m WHITE");
    }

    public String colorize(String str) {
        return esc + str + end;
    }
}
