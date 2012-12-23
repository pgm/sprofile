package org.github.sprofile;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/22/12
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Details {
    private final String[] names;
    private final String[] values;

    public Details(String... keyValues) {
        names = new String[keyValues.length / 2];
        values = new String[keyValues.length / 2];
        for (int i = 0; i < keyValues.length; i += 2) {
            names[i / 2] = keyValues[i];
            values[i / 2] = keyValues[i + 1];
        }
    }
}
