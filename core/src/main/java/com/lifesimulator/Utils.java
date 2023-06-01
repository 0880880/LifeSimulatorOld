package com.lifesimulator;

import com.badlogic.gdx.utils.Array;

public class Utils {

    public static void sort(Array<Creature> creatures) {
        creatures.sort((o1, o2) -> Float.compare(o1.x, o2.x));
    }

    public static boolean evaluate(Creature creature) {
        return creature.x > 230 && creature.y > 50 && creature.y < 206;
    }

}
