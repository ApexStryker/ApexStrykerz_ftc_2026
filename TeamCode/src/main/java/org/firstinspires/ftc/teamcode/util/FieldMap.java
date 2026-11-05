package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.teamcode.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The fixed field from Constants.Field, for planning in AUTO: where the robot's CENTRE may go
 * (on our side, inside the walls, clear of the hive frame and the flowers), and a route between
 * two points that goes around them. No hardware, so it can be tested on a laptop.
 *
 *   FieldMap map = FieldMap.compete();
 *   List<double[]> route = map.route(x, y, toX, toY);   // corners to drive through, ending at (toX, toY)
 *
 * Positions are inches in ARENA COORDINATES (see Constants.Auto).
 */
public class FieldMap {

    private final List<double[]> boxes = new ArrayList<>();   // keep-out for the robot's centre
    private final List<String> names = new ArrayList<>();
    private final double minX, maxX, minY, maxY;               // where the robot's centre may be
    private final double grid;

    public FieldMap(double minX, double maxX, double minY, double maxY, double grid) {
        this.minX = minX; this.maxX = maxX; this.minY = minY; this.maxY = maxY; this.grid = grid;
    }

    /** The BIOBUZZ field for AUTO: our side only, walls, hive frame and flowers from Constants.Field. */
    public static FieldMap compete() { return compete(false); }

    /**
     * The same, mirrored left-right (y -> 144 - y) when mirrored is true: for starting in the
     * mirror slot on the left-hand wall ("Auto Compete Left").
     */
    public static FieldMap compete(boolean mirrored) {
        double half = Constants.Field.ROBOT_HALF_IN, grow = half + Constants.Field.CLEARANCE_IN;
        FieldMap map = new FieldMap(half, Constants.Field.OUR_SIDE_MAX_X - half,
                half, Constants.Auto.ARENA_SIZE_IN - half, Constants.Field.GRID_IN);
        for (int i = 0; i < Constants.Field.OBSTACLES.length; i++) {
            double[] b = forSlot(Constants.Field.OBSTACLES[i], mirrored);
            map.addObstacle(Constants.Field.OBSTACLE_NAMES[i], b[0] - grow, b[1] - grow, b[2] + grow, b[3] + grow);
        }
        return map;
    }

    /** A box {x1, y1, x2, y2} as is, or mirrored left-right (y -> 144 - y). */
    public static double[] forSlot(double[] box, boolean mirrored) {
        if (!mirrored) return box.clone();
        double size = Constants.Auto.ARENA_SIZE_IN;
        return new double[] { box[0], size - box[3], box[2], size - box[1] };
    }

    /** A box the robot's centre must stay out of (already grown by the robot's size). */
    public void addObstacle(String name, double x1, double y1, double x2, double y2) {
        boxes.add(new double[] { x1, y1, x2, y2 });
        names.add(name);
    }

    /** What is in the way of the robot's centre at (x, y): an obstacle or wall name, or null if free. */
    public String blockedBy(double x, double y) {
        final double e = 1e-6;
        if (x < minX - e) return "our wall";
        if (x > maxX + e) return "the middle line";
        if (y < minY - e) return "the right-hand wall";
        if (y > maxY + e) return "the left-hand wall";
        for (int i = 0; i < boxes.size(); i++) {
            double[] b = boxes.get(i);
            if (x > b[0] && x < b[2] && y > b[1] && y < b[3]) return names.get(i);
        }
        return null;
    }

    public boolean isFree(double x, double y) { return blockedBy(x, y) == null; }

    /** What the straight line between two points runs into, or null if it is clear. */
    public String segmentBlockedBy(double x1, double y1, double x2, double y2) {
        int n = Math.max(1, (int) Math.ceil(Math.hypot(x2 - x1, y2 - y1) / 0.5));
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            String b = blockedBy(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
            if (b != null) return b;
        }
        return null;
    }

    public boolean segmentClear(double x1, double y1, double x2, double y2) {
        return segmentBlockedBy(x1, y1, x2, y2) == null;
    }

    /**
     * Corners to drive through from (fromX, fromY) to (toX, toY), ending exactly at the target,
     * or null if the target is blocked or cannot be reached. A straight line if that is clear.
     * The start may be inside a keep-out box (e.g. pushed there by a bump): the route leaves it
     * the shortest way.
     */
    public List<double[]> route(double fromX, double fromY, double toX, double toY) {
        if (!isFree(toX, toY)) return null;
        List<double[]> out = new ArrayList<>();
        if (segmentClear(fromX, fromY, toX, toY)) {
            out.add(new double[] { toX, toY });
            return out;
        }
        // A* over a grid of cell centres, 8 neighbours.
        int nx = (int) Math.floor((maxX - minX) / grid) + 1, ny = (int) Math.floor((maxY - minY) / grid) + 1;
        int start = cell(fromX, fromY, nx, ny), goal = cell(toX, toY, nx, ny);
        double[] cost = new double[nx * ny];
        int[] from = new int[nx * ny];
        Arrays.fill(cost, Double.MAX_VALUE);
        Arrays.fill(from, -1);
        cost[start] = 0;
        PriorityQueue<double[]> open = new PriorityQueue<>((a, b) -> Double.compare(a[0], b[0]));
        open.add(new double[] { 0, start });
        while (!open.isEmpty()) {
            int c = (int) open.poll()[1];
            if (c == goal) break;
            int cx = c % nx, cy = c / nx;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int ax = cx + dx, ay = cy + dy;
                    if ((dx == 0 && dy == 0) || ax < 0 || ay < 0 || ax >= nx || ay >= ny) continue;
                    int a = ay * nx + ax;
                    // Never through a blocked spot (diagonals must not cut a box's corner),
                    // except while still getting out of one (a bumped start).
                    if (isFree(cellX(cx), cellY(cy))
                            && !segmentClear(cellX(cx), cellY(cy), cellX(ax), cellY(ay))) continue;
                    double step = (dx != 0 && dy != 0) ? grid * Math.sqrt(2) : grid;
                    double nc = cost[c] + step;
                    if (nc < cost[a]) {
                        cost[a] = nc;
                        from[a] = c;
                        open.add(new double[] { nc + Math.hypot(cellX(ax) - toX, cellY(ay) - toY), a });
                    }
                }
            }
        }
        if (from[goal] < 0 && goal != start) return null;
        List<double[]> cells = new ArrayList<>();
        for (int c = goal; c != start && c >= 0; c = from[c]) cells.add(new double[] { cellX(c % nx), cellY(c / nx) });
        Collections.reverse(cells);
        cells.set(cells.size() - 1, new double[] { toX, toY });
        // Keep only the corners: from each point jump to the furthest one in plain sight.
        double px = fromX, py = fromY;
        int i = 0;
        while (i < cells.size()) {
            int far = i;
            for (int j = cells.size() - 1; j > i; j--) {
                if (segmentClear(px, py, cells.get(j)[0], cells.get(j)[1])) { far = j; break; }
            }
            out.add(cells.get(far));
            px = cells.get(far)[0];
            py = cells.get(far)[1];
            i = far + 1;
        }
        return out;
    }

    /** Length of a route from (x, y), in inches. */
    public static double length(double x, double y, List<double[]> route) {
        double total = 0;
        for (double[] p : route) {
            total += Math.hypot(p[0] - x, p[1] - y);
            x = p[0];
            y = p[1];
        }
        return total;
    }

    private int cell(double x, double y, int nx, int ny) {
        int cx = (int) Math.round((x - minX) / grid), cy = (int) Math.round((y - minY) / grid);
        cx = Math.max(0, Math.min(nx - 1, cx));
        cy = Math.max(0, Math.min(ny - 1, cy));
        return cy * nx + cx;
    }

    private double cellX(int cx) { return minX + cx * grid; }
    private double cellY(int cy) { return minY + cy * grid; }
}
