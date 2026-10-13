package graphics.rasterizer;

import model.Line;
import model.Polygon;

import java.util.List;

public class PolygonOutlineRasterizer implements PolygonRasterizer {

    private final LineRasterizer lineRasterizer;

    public PolygonOutlineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    @Override
    public void rasterize(Polygon polygon) {
        List<Line> lines = polygon.getLines();
        rasterizeLines(lines);

        // Uzavírací úsečku kreslíme až po dokončení polygonu.
        if (lines.size() > 1) {
            Line last = lines.getLast();
            Line first = lines.getFirst();
            lineRasterizer.rasterize(
                    new Line(last.getColor(), last.getPoint2(), first.getPoint1())
            );
        }
    }

    private void rasterizeLines(List<Line> lines) {
        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }
    }
}
