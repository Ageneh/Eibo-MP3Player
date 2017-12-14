package mvc.view.enums;

public enum Dim {

    MIN_W_SCREEN(900),
    MIN_H_SCREEN(675),

    H_BORDERP_BOTTOM(50),

    W_SONGTITLE(50),

    H_COVER_IMG(1000),
    W_COVER_IMG(H_COVER_IMG.intVal()),
    H_LIST_CELL(30),

    BASE_FONT_SIZE(13),

    //// PADDINGS
    PAD_SIDE_LIST(15),
    PAD_PLAYLIST_WINDOW(20),

    SIZE_CTRL_BTN(30),

    SIZE_COVERIMG_BASE(500),

    W_LEFT_PANEL(100),
    H_LEFT_PANEL_HEADER(60),

    W_ADDPLAYLIST_WINDOW(400),
    H_ADDPLAYLIST_WINDOW(W_ADDPLAYLIST_WINDOW.intVal() * 1.25),

    W_MAX_SLIDER(200);

    private Number val;


    Dim(Number val){
        this.val = val;
    }

    public int intVal() {
        return val.intValue();
    }

    public double doubleVal() {
        return val.doubleValue();
    }
}
