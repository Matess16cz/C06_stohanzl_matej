package graphics.rasterizer;

import graphics.Raster;
import model.Line;

public class TrivialLineRasterizer implements LineRasterizer {

    private final Raster<Integer> raster;

    public TrivialLineRasterizer(Raster<Integer> raster) {
        this.raster = raster;
    }

    @Override
    public void rasterize(Line line) {
        rasterize(
                line.getPoint1().getX(), line.getPoint2().getX(),
                line.getPoint1().getY(), line.getPoint2().getY(),
                line.getColor()
        );
    }

    // Algoritmus: triviální rasterizace úsečky
    // Rovnice: y = k * x + q
    private void rasterize(int x1, int x2, int y1, int y2, int color) {
        // Vertikální úsečka: nelze použít směrnici (dělení nulou).
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1, y, color);
            }
            return;
        }

        if (x2 < x1) {
            int tempX = x1;
            x1 = x2;
            x2 = tempX;
            int tempY = y1;
            y1 = y2;
            y2 = tempY;
        }

        float k = (y2 - y1) / (float) (x2 - x1);

        if (Math.abs(y2 - y1) > Math.abs(x2 - x1)) {
            if (y2 < y1) {
                int tempX = x1;
                x1 = x2;
                x2 = tempX;
                int tempY = y1;
                y1 = y2;
                y2 = tempY;
            }
            k = (x2 - x1) / (float) (y2 - y1);
            float q = x1 - k * y1;
            for (int y = y1; y <= y2; y++) {
                int x = Math.round(k * y + q);
                raster.setPixel(x, y, color);
            }
        } else {
            float q = y1 - k * x1;
            for (int x = x1; x <= x2; x++) {
                int y = Math.round(k * x + q);
                raster.setPixel(x, y, color);
            }
        }
    }
}
