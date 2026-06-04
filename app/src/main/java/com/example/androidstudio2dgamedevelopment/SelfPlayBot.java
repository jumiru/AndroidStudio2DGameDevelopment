package com.example.androidstudio2dgamedevelopment;

import java.util.ArrayDeque;

/**
 * Stateless strategy engine for the self-play / watch mode.
 *
 * Priority order:
 *   1. Direct merge (≥4 cells) – largest group, with chain lookahead bonus
 *   2. Dissolve to enable a merge
 *   3. Jump to an unreachable merge position
 *   4. Crowded-board cleanup (Color Clear, Delete Line, Bomb, Dissolve worst)
 *   5. Normal clustering toward same-value neighbours
 */
public class SelfPlayBot {

    // Must match BONUS_BUY_COSTS order in GameBoard
    static final int IDX_UNDO        = 0;
    static final int IDX_DISSOLVE    = 1;
    static final int IDX_SWAP        = 2;
    static final int IDX_BOMB        = 3;
    static final int IDX_SHIFT_LINE  = 4;
    static final int IDX_JUMP        = 5;
    static final int IDX_DEL_LINE    = 6;
    static final int IDX_COLOR_CLEAR = 7;

    private static final int MIN_COMBO_SIZE = 4;

    // -------------------------------------------------------------------------
    // Public decision type
    // -------------------------------------------------------------------------

    public static class BotDecision {
        public enum Type { NORMAL, JUMP, DISSOLVE, BOMB, COLOR_CLEAR, DEL_LINE }

        public final Type type;
        /** p1 = source cell (NORMAL/JUMP), dissolve/bomb/color_clear target, del_line index */
        public final int p1x, p1y;
        /** p2 = target cell (NORMAL/JUMP) */
        public final int p2x, p2y;
        /** DEL_LINE: true = delete row, false = delete column */
        public final boolean flag;

        private BotDecision(Type t, int p1x, int p1y, int p2x, int p2y, boolean flag) {
            this.type = t;
            this.p1x = p1x; this.p1y = p1y;
            this.p2x = p2x; this.p2y = p2y;
            this.flag = flag;
        }

        static BotDecision normal(int sx, int sy, int tx, int ty) {
            return new BotDecision(Type.NORMAL, sx, sy, tx, ty, false);
        }
        static BotDecision jump(int sx, int sy, int tx, int ty) {
            return new BotDecision(Type.JUMP, sx, sy, tx, ty, false);
        }
        static BotDecision dissolve(int x, int y) {
            return new BotDecision(Type.DISSOLVE, x, y, 0, 0, false);
        }
        static BotDecision bomb(int cx, int cy) {
            return new BotDecision(Type.BOMB, cx, cy, 0, 0, false);
        }
        static BotDecision colorClear(int x, int y) {
            return new BotDecision(Type.COLOR_CLEAR, x, y, 0, 0, false);
        }
        /** lineIdx = row index (flag=true) or col index (flag=false) */
        static BotDecision delLine(int lineIdx, boolean isRow) {
            return new BotDecision(Type.DEL_LINE, lineIdx, 0, 0, 0, isRow);
        }
    }

    // -------------------------------------------------------------------------
    // Main entry point
    // -------------------------------------------------------------------------

    /**
     * @param board       mutable scratch copy of the board (restored on return)
     * @param bonusCounts counts for each bonus in BONUS_BUY_COSTS order
     * @param freeCells   number of empty cells right now
     */
    public static BotDecision computeDecision(int[][] board, int w, int h,
                                              int[] bonusCounts, int freeCells) {
        float freeRatio = (float) freeCells / (w * h);

        // 1. Direct merge
        BotDecision d = findBestMergeDecision(board, w, h);
        if (d != null) return d;

        // 2. Dissolve to enable a merge
        if (bonusCounts[IDX_DISSOLVE] > 0) {
            d = findDissolveToEnableMerge(board, w, h);
            if (d != null) return d;
        }

        // 3. Jump to an unreachable merge position
        if (bonusCounts[IDX_JUMP] > 0) {
            d = findJumpToEnableMerge(board, w, h);
            if (d != null) return d;
        }

        // 4. Crowded-board cleanup
        if (freeRatio < 0.42f) {
            if (bonusCounts[IDX_COLOR_CLEAR] > 0) {
                d = findColorClearTarget(board, w, h, 4);
                if (d != null) return d;
            }
            if (freeRatio < 0.30f) {
                if (bonusCounts[IDX_DEL_LINE] > 0) {
                    d = findDelLineTarget(board, w, h, 3);
                    if (d != null) return d;
                }
                if (bonusCounts[IDX_BOMB] > 0) {
                    d = findBombTarget(board, w, h, 5);
                    if (d != null) return d;
                }
            }
            if (freeRatio < 0.25f && bonusCounts[IDX_DISSOLVE] > 0) {
                d = findDissolveWorstCell(board, w, h);
                if (d != null) return d;
            }
        }

        // 5. Normal clustering
        return findClusterMove(board, w, h);
    }

    // -------------------------------------------------------------------------
    // 1. Best direct merge (largest group, chain lookahead)
    // -------------------------------------------------------------------------

    private static BotDecision findBestMergeDecision(int[][] board, int w, int h) {
        BotDecision best = null;
        int bestScore = -1;

        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                int val = board[sx][sy];
                if (val < 0) continue;

                // Keep source empty during simulation so it isn't counted in the group
                board[sx][sy] = -1;
                boolean[][] reach = bfsEmpty(board, w, h, sx, sy);

                for (int ty = 0; ty < h; ty++) {
                    for (int tx = 0; tx < w; tx++) {
                        if (tx == sx && ty == sy) continue;
                        if (!reach[tx][ty]) continue;

                        board[tx][ty] = val;
                        int grp = countGroup(board, w, h, tx, ty, val);
                        int chain = 0;
                        if (grp >= MIN_COMBO_SIZE) chain = chainLookahead(board, w, h, tx, ty, val, grp);
                        board[tx][ty] = -1;

                        if (grp >= MIN_COMBO_SIZE) {
                            int score = (1 << Math.min(grp - MIN_COMBO_SIZE, 20)) * (val + 1) + chain;
                            if (score > bestScore) { bestScore = score; best = BotDecision.normal(sx, sy, tx, ty); }
                        }
                    }
                }

                board[sx][sy] = val; // restore after all targets checked
            }
        }
        return best;
    }

    // -------------------------------------------------------------------------
    // 2. Dissolve to enable a merge
    // -------------------------------------------------------------------------

    private static BotDecision findDissolveToEnableMerge(int[][] board, int w, int h) {
        BotDecision best = null;
        int bestScore = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = board[x][y];
                if (val < 0) continue;
                // Don't dissolve a cell that's already part of a merge group
                if (countGroup(board, w, h, x, y, val) >= MIN_COMBO_SIZE) continue;

                board[x][y] = -1;
                int score = maxGroupAroundCell(board, w, h, x, y);
                board[x][y] = val;

                if (score >= MIN_COMBO_SIZE && score > bestScore) {
                    bestScore = score;
                    best = BotDecision.dissolve(x, y);
                }
            }
        }
        return best;
    }

    /** Max group size among neighbours after a cell at (rx,ry) was cleared. */
    private static int maxGroupAroundCell(int[][] board, int w, int h, int rx, int ry) {
        boolean[][] vis = new boolean[w][h];
        int max = 0;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nx = rx + d[0], ny = ry + d[1];
            if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
            if (board[nx][ny] < 0 || vis[nx][ny]) continue;
            int g = countGroupMark(board, w, h, nx, ny, board[nx][ny], vis);
            if (g > max) max = g;
        }
        return max;
    }

    // -------------------------------------------------------------------------
    // 3. Jump to an otherwise unreachable merge position
    // -------------------------------------------------------------------------

    private static BotDecision findJumpToEnableMerge(int[][] board, int w, int h) {
        BotDecision best = null;
        int bestScore = 0;

        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                int val = board[sx][sy];
                if (val < 0) continue;

                board[sx][sy] = -1; // keep empty during simulation
                boolean[][] reach = bfsEmpty(board, w, h, sx, sy);

                for (int ty = 0; ty < h; ty++) {
                    for (int tx = 0; tx < w; tx++) {
                        if (tx == sx && ty == sy) continue;
                        if (board[tx][ty] != -1) continue;
                        if (reach[tx][ty]) continue; // reachable normally, skip

                        board[tx][ty] = val;
                        int grp = countGroup(board, w, h, tx, ty, val);
                        board[tx][ty] = -1;

                        if (grp >= MIN_COMBO_SIZE) {
                            int score = (1 << Math.min(grp - MIN_COMBO_SIZE, 20)) * (val + 1);
                            if (score > bestScore) { bestScore = score; best = BotDecision.jump(sx, sy, tx, ty); }
                        }
                    }
                }

                board[sx][sy] = val; // restore after all targets checked
            }
        }
        return best;
    }

    // -------------------------------------------------------------------------
    // 4a. Color Clear – remove the most numerous scattered value
    // -------------------------------------------------------------------------

    private static BotDecision findColorClearTarget(int[][] board, int w, int h, int minCells) {
        int[] counts = new int[40];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (board[x][y] >= 0 && board[x][y] < 40) counts[board[x][y]]++;

        int bestVal = -1, bestCount = 0;
        for (int v = 0; v < 40; v++) {
            if (counts[v] >= minCells && counts[v] > bestCount) {
                // Only clear if they can't merge naturally (max group < 4)
                if (findMaxGroupSizeOfValue(board, w, h, v) < MIN_COMBO_SIZE) {
                    bestCount = counts[v];
                    bestVal = v;
                }
            }
        }
        if (bestVal < 0) return null;

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (board[x][y] == bestVal) return BotDecision.colorClear(x, y);
        return null;
    }

    // -------------------------------------------------------------------------
    // 4b. Delete Line – remove most-filled row or column
    // -------------------------------------------------------------------------

    private static BotDecision findDelLineTarget(int[][] board, int w, int h, int minFilled) {
        int bestScore = 0, bestIdx = -1;
        boolean bestIsRow = true;

        for (int y = 0; y < h; y++) {
            int cnt = 0;
            for (int x = 0; x < w; x++) if (board[x][y] >= 0) cnt++;
            if (cnt > bestScore) { bestScore = cnt; bestIdx = y; bestIsRow = true; }
        }
        for (int x = 0; x < w; x++) {
            int cnt = 0;
            for (int y = 0; y < h; y++) if (board[x][y] >= 0) cnt++;
            if (cnt > bestScore) { bestScore = cnt; bestIdx = x; bestIsRow = false; }
        }
        if (bestIdx >= 0 && bestScore >= minFilled) return BotDecision.delLine(bestIdx, bestIsRow);
        return null;
    }

    // -------------------------------------------------------------------------
    // 4c. Bomb – center of densest 3×3 area
    // -------------------------------------------------------------------------

    private static BotDecision findBombTarget(int[][] board, int w, int h, int minCells) {
        int bestX = -1, bestY = -1, best = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (board[x][y] < 0) continue;
                int cnt = 0;
                for (int dy = -1; dy <= 1; dy++)
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h && board[nx][ny] >= 0) cnt++;
                    }
                if (cnt > best) { best = cnt; bestX = x; bestY = y; }
            }
        }
        if (bestX >= 0 && best >= minCells) return BotDecision.bomb(bestX, bestY);
        return null;
    }

    // -------------------------------------------------------------------------
    // 4d. Dissolve worst cell (panic, board almost full)
    // -------------------------------------------------------------------------

    private static BotDecision findDissolveWorstCell(int[][] board, int w, int h) {
        // Remove the lowest-value cell that has fewest same-value neighbours
        int worstScore = Integer.MAX_VALUE;
        int bestX = -1, bestY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = board[x][y];
                if (val < 0) continue;
                if (countGroup(board, w, h, x, y, val) >= MIN_COMBO_SIZE) continue; // keep merge-ready
                // score: lower value = worse (more expendable), fewer same-value neighbours = more isolated
                int sameNeighbours = 0;
                int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
                for (int[] d : dirs) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h && board[nx][ny] == val) sameNeighbours++;
                }
                int score = val * 10 + sameNeighbours;
                if (score < worstScore) { worstScore = score; bestX = x; bestY = y; }
            }
        }
        if (bestX >= 0) return BotDecision.dissolve(bestX, bestY);
        return null;
    }

    // -------------------------------------------------------------------------
    // 5. Clustering – approach nearest same-value cell
    // -------------------------------------------------------------------------

    private static BotDecision findClusterMove(int[][] board, int w, int h) {
        BotDecision best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                int val = board[sx][sy];
                if (val < 0) continue;
                int curDist = nearestSameValueDist(board, w, h, sx, sy, val, -1, -1);
                if (curDist == Integer.MAX_VALUE) continue;

                board[sx][sy] = -1; // keep empty so nearestSameValueDist doesn't see it
                boolean[][] reach = bfsEmpty(board, w, h, sx, sy);

                for (int ty = 0; ty < h; ty++) {
                    for (int tx = 0; tx < w; tx++) {
                        if (tx == sx && ty == sy || !reach[tx][ty]) continue;
                        // excX/excY still passed for safety but board is already cleared
                        int newDist = nearestSameValueDist(board, w, h, tx, ty, val, sx, sy);
                        int score = (curDist - newDist) * 100 + (val + 1);
                        if (score > bestScore) { bestScore = score; best = BotDecision.normal(sx, sy, tx, ty); }
                    }
                }

                board[sx][sy] = val; // restore after all targets checked
            }
        }
        if (best != null) return best;
        return findAnyMove(board, w, h);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static int chainLookahead(int[][] board, int w, int h,
                                      int tx, int ty, int val, int grp) {
        boolean[][] inGroup = new boolean[w][h];
        markGroup(board, w, h, tx, ty, val, inGroup);
        int higherVal = val + 2;
        int count = 0;
        outer:
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!inGroup[x][y]) continue;
                int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
                for (int[] d : dirs) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h
                            && board[nx][ny] == higherVal && !inGroup[nx][ny]) {
                        count++;
                        if (count >= MIN_COMBO_SIZE - 1) break outer;
                    }
                }
            }
        }
        return (count >= MIN_COMBO_SIZE - 1) ? count * 500 : 0;
    }

    private static int nearestSameValueDist(int[][] board, int w, int h,
                                            int ox, int oy, int val, int excX, int excY) {
        int min = Integer.MAX_VALUE;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (x == ox && y == oy) continue;
                if (x == excX && y == excY) continue;
                if (board[x][y] == val) min = Math.min(min, Math.abs(x-ox) + Math.abs(y-oy));
            }
        return min;
    }

    private static BotDecision findAnyMove(int[][] board, int w, int h) {
        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                int val = board[sx][sy];
                if (val < 0) continue;
                board[sx][sy] = -1;
                boolean[][] reach = bfsEmpty(board, w, h, sx, sy);
                board[sx][sy] = val;
                for (int ty = 0; ty < h; ty++)
                    for (int tx = 0; tx < w; tx++)
                        if (reach[tx][ty] && !(tx == sx && ty == sy))
                            return BotDecision.normal(sx, sy, tx, ty);
            }
        }
        return null;
    }

    private static int findMaxGroupSizeOfValue(int[][] board, int w, int h, int val) {
        boolean[][] vis = new boolean[w][h];
        int max = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (board[x][y] == val && !vis[x][y]) {
                    int g = countGroupMark(board, w, h, x, y, val, vis);
                    if (g > max) max = g;
                }
            }
        return max;
    }

    // BFS: marks all empty cells reachable from (sx,sy). Caller sets board[sx][sy]=-1 first.
    private static boolean[][] bfsEmpty(int[][] board, int w, int h, int sx, int sy) {
        boolean[][] vis = new boolean[w][h];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        vis[sx][sy] = true;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] c = q.poll();
            enqueue(board, w, h, c[0]-1, c[1], vis, q);
            enqueue(board, w, h, c[0]+1, c[1], vis, q);
            enqueue(board, w, h, c[0], c[1]-1, vis, q);
            enqueue(board, w, h, c[0], c[1]+1, vis, q);
        }
        return vis;
    }

    private static void enqueue(int[][] b, int w, int h, int x, int y,
                                 boolean[][] vis, ArrayDeque<int[]> q) {
        if (x >= 0 && x < w && y >= 0 && y < h && !vis[x][y] && b[x][y] < 0) {
            vis[x][y] = true;
            q.add(new int[]{x, y});
        }
    }

    private static int countGroup(int[][] board, int w, int h, int x, int y, int val) {
        return countGroupMark(board, w, h, x, y, val, new boolean[w][h]);
    }

    private static int countGroupMark(int[][] board, int w, int h, int x, int y,
                                      int val, boolean[][] vis) {
        if (x < 0 || x >= w || y < 0 || y >= h || vis[x][y] || board[x][y] != val) return 0;
        vis[x][y] = true;
        return 1 + countGroupMark(board, w, h, x-1, y, val, vis)
                 + countGroupMark(board, w, h, x+1, y, val, vis)
                 + countGroupMark(board, w, h, x, y-1, val, vis)
                 + countGroupMark(board, w, h, x, y+1, val, vis);
    }

    private static void markGroup(int[][] board, int w, int h, int x, int y,
                                  int val, boolean[][] out) {
        if (x < 0 || x >= w || y < 0 || y >= h || out[x][y] || board[x][y] != val) return;
        out[x][y] = true;
        markGroup(board, w, h, x-1, y, val, out);
        markGroup(board, w, h, x+1, y, val, out);
        markGroup(board, w, h, x, y-1, val, out);
        markGroup(board, w, h, x, y+1, val, out);
    }
}
