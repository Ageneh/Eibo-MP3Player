package mvc.model.extension.enums;

public enum Interval {

    REFRESH_RATE(1000),
    LATENCY(25);

    private int val;

    Interval(int val){
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
