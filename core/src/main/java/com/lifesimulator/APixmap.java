package com.lifesimulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class APixmap extends Pixmap {
    public APixmap(int width, int height, Format format) {
        super(width, height, format);
    }
    public APixmap(int width, int height, Format format, Color background) {
        super(width, height, format);
        setColor(background);
        fill();
    }
}
