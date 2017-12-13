package mvc.view.enums;

public enum Dim {

    H_CELL(30),
    MIN_W_SCREEN(900),
    MIN_H_SCREEN(675),
    H_ROOT_BOTTOM(50),
    H_COVER_IMG(1000),
    W_COVER_IMG(H_COVER_IMG.intVal()),
    BASE_FONT_SIZE(13),
    PAD_SIDE_LIST(15),
    PAD_PLAYLIST_WINDOW(20),
    SIZE_CTRL_BTN(30),
    SIZE_COVERIMG_BASE(500),
    W_LEFT_PANEL(100),

    W_ADDPLAYLIST_WINDOW(400),
    H_ADDPLAYLIST_WINDOW(W_ADDPLAYLIST_WINDOW.intVal() * 1.25);

    private double val;

    Dim(int val){
        this.val = val;
    }

    Dim(double val){
        this.val = val;
    }

    public int intVal() {
        return (int) val;
    }

    public double doubleVal() {
        return (int) val;
    }
}
