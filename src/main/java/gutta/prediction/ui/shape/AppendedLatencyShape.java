package gutta.prediction.ui.shape;

class AppendedLatencyShape extends SyntheticLatencyShape {

    private static final long serialVersionUID = 820437604965312898L;

    public AppendedLatencyShape(int startX, int y, int endX) {
        int centerY = y + (HEIGHT / 2);

        this.moveTo(startX, centerY);
        this.lineTo(endX, centerY);
        this.moveTo(endX, y);
        this.lineTo(endX, (y + HEIGHT));
    }

}
