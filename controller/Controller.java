package controller;

import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.PolygonOutlineRasterizer;
import graphics.rasterizer.PolygonRasterizer;
import graphics.rasterizer.TrivialLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;


import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/** Handles mouse and keyboard input and renders the polygon. */
public class Controller {

    private Line previewLine;
    private Line previewLine2;

    private final Canvas canvas;
    private final Polygon polygon;
    private final LineRasterizer rasterizer;
    private final PolygonRasterizer polygonRasterizer;

    private Point lastPoint;
    private int movingIndex = -1;

    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private static final int PREVIEW_COLOR = Color.RED.getRGB();

    public Controller(Canvas canvas) {
        this.canvas = canvas;
        this.polygon = new Polygon(new ArrayList<>());
        this.rasterizer = new TrivialLineRasterizer(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(rasterizer);
    }

    public void init() {

        canvas.clear();

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    startMove(e);
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    startLine(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                movingIndex = -1;

                if (e.getButton() == MouseEvent.BUTTON1 && previewLine != null) {

                    if (polygon.getLines().size() >= 2) {
                        previewLine = null;
                        previewLine2 = null;
                        insertPoint(e);
                    } else {
                        Point end = getPoint(e);
                        polygon.getLines().add(new Line(LINE_COLOR ,previewLine.getPoint1(), end));
                        lastPoint = end;
                        if (polygon.getLines().size() == 2) {
                            Point firstPoint = polygon.getLines().getFirst().getPoint1();
                            polygon.getLines().add(new Line(LINE_COLOR, end, firstPoint));
                        }
                        previewLine = null;
                        render();
                    }
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (previewLine != null) {
                    if (polygon.getLines().size() >= 2) {
                        showEdgePreview(getPoint(e));
                    } else   {
                        previewLine = new Line(PREVIEW_COLOR, previewLine.getPoint1(), getPoint(e) );
                    }
                    render();
                } else if (movingIndex != -1) {
                    List<Point> points = getPoints();
                    points.set(movingIndex, getPoint(e));
                    setPoints(points);
                    render();

                }
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    polygon.getLines().clear();
                    previewLine = null;
                    previewLine2 = null;
                    lastPoint = null;
                    render();
                }
            }
        });

        // Zajistí, že plátno dostane události klávesnice po kliknutí.
        canvas.repaint();
    }

    public void render() {
        canvas.clear();
        polygonRasterizer.rasterize(polygon);
        if (previewLine != null) {
            rasterizer.rasterize(previewLine);
        }
        if (previewLine2 != null) {
            rasterizer.rasterize(previewLine2);
        }
        canvas.repaint();
    }

    private void startLine(MouseEvent e) {
        Point currentPoint = getPoint(e);
        if (polygon.getLines().size() >= 2) {
            showEdgePreview(currentPoint);
            render();
            return;
        }
        Point startPoint = lastPoint;
        if (startPoint == null) {
            startPoint = currentPoint;
        }
        previewLine = new Line(PREVIEW_COLOR, startPoint, currentPoint);
        render();
    }

    private void startMove(MouseEvent e) {
        List<Point> points = getPoints();
        if (points.isEmpty()) {
            return;
        }
        Point currentPoint = getPoint(e);
        movingIndex = 0;
        double minDistance = distance(currentPoint, points.getFirst());
        for (int i = 1; i < points.size(); i++) {
            double currentDistance = distance(currentPoint, points.get(i));
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                movingIndex = i;
            }
        }
    }

    private void insertPoint(MouseEvent e) {
        List<Point> points = getPoints();
        Point currentPoint = getPoint(e);
        int bestEdge = findClosestEdge(points, currentPoint);
        points.add(bestEdge + 1, currentPoint);
        setPoints(points);
        render();

    }
    private int findClosestEdge(List<Point> points, Point currentPoint) {
        int bestEdge = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            Point a  = points.get(i);
            Point b = points.get((i + 1) % points.size());
            double d = distanceToEdge (currentPoint, a, b);
            if (d < bestDistance) {
                bestDistance = d;
                bestEdge = i;
            }
        }
        return bestEdge;
    }
    private void showEdgePreview(Point current) {
        List<Point> points = getPoints();
        int edge = findClosestEdge(points, current);
        Point a = points.get(edge);
        Point b = points.get((edge + 1) % points.size());
        previewLine = new Line(PREVIEW_COLOR, a, current);
        previewLine2 = new Line(PREVIEW_COLOR, b, current);
    }

    private List<Point> getPoints() {
        List<Point> points = new ArrayList<>();
        List<Line> lines = polygon.getLines();
        for (Line line : lines) {
            points.add(line.getPoint1());
        }
        if (!lines.isEmpty() && lines.size() < 3) {
            points.add(lines.getLast().getPoint2());
        }
        return points;
    }

    private void setPoints(List<Point> points) {
        polygon.getLines().clear();
        for (int i = 0; i < points.size() - 1; i++) {
            polygon.getLines().add(new Line(LINE_COLOR, points.get(i), points.get(i + 1)));
        }
        if (points.size() >= 3) {
            polygon.getLines().add(new Line(LINE_COLOR, points.getLast(), points.getFirst()));
        }
        lastPoint = points.getLast();
    }

    private double distance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double distanceToEdge(Point p, Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return distance(p, a);
        }
        double t = ((p.getX() - a.getX()) * dx + (p.getY() - a.getY()) * dy) / lengthSquared;
        if (t < 0) {
            t = 0;
        }
        if (t > 1) {
            t = 1;
        }
        Point nearest = new Point((int) Math.round(a.getX() + t * dx), (int) Math.round(a.getY() + t * dy));
        return distance(p, nearest);
    }

    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }
}