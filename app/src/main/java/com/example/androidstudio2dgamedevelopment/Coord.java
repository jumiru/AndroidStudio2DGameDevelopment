package com.example.androidstudio2dgamedevelopment;

public class Coord implements Comparable<Coord> {

    public int x;
    public int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coord() {
        x = 0;
        y = 0;
    }

    public Coord(Coord c) {
        this.x = c.x;
        this.y = c.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coord) {
            Coord c = (Coord) obj;
            return x == c.x && y == c.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public int compareTo(Coord c) {
        if (x != c.x) {
            return Integer.compare(x, c.x);
        }
        return Integer.compare(y, c.y);
    }
}
