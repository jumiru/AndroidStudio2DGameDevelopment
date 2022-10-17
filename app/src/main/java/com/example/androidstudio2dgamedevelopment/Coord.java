package com.example.androidstudio2dgamedevelopment;

import androidx.annotation.Nullable;

public class Coord implements Comparable {


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
    public boolean equals(@Nullable Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            return (x==((Coord)obj).x && y==((Coord)obj).y);
        }
        return false;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            Coord c = (Coord) obj;
            if (c.x > x) {
                return 1;
            } else if (c.x < x) {
                return -1;
            } else if (c.y > y) {
                return 1;
            } else if (c.y < y) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }
}
