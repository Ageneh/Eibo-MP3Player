package mvc.model.extension.enums;

public enum Skip {

    FORWARD(3000),
    BACKWARD(FORWARD.skipVal * (-1)),
    NEXT(1),
    PREVIOUS(-1);

    private int skipVal;

    Skip(int skipVal){
        this.skipVal = skipVal;
    }

    public int getSkipVal(){
        return this.skipVal;
    }

}
