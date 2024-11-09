package gutta.prediction.ui.shape;

class PrependedOverheadShape extends SyntheticOverheadShape {

    private static final long serialVersionUID = 6916723154906612646L;

    public PrependedOverheadShape(int startX, int y, int endX) {
        int centerY = y + (HEIGHT / 2);

        this.moveTo(startX, y);
        this.lineTo(startX, (y + HEIGHT));
        this.moveTo(startX, centerY);
        this.lineTo(endX, centerY);
    }

}
