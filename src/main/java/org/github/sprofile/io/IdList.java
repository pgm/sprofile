package org.github.sprofile.io;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/22/12
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
class IdList {
    final int ids[];

    public IdList(int ids[]) {
        this.ids = ids;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(ids);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdList other = (IdList) obj;
        if (!Arrays.equals(ids, other.ids))
            return false;
        return true;
    }
}
