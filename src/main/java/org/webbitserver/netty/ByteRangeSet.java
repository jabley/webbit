package org.webbitserver.netty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * @see http://tools.ietf.org/html/rfc2616#section-14.35.1
 */
class ByteRangeSet {

    @SuppressWarnings("unchecked")
    static final ByteRangeSet NO_RANGE_HEADER = new ByteRangeSet(Collections.EMPTY_LIST);

    private final RangeSpec[] specs;

    ByteRangeSet(List<RangeSpec> specs) {
        this.specs = specs.toArray(new RangeSpec[specs.size()]);
    }

    public static ByteRangeSet parse(String byteRangeSet) {
        StringTokenizer csv = new StringTokenizer(byteRangeSet, ",");

        List<RangeSpec> specs = new ArrayList<RangeSpec>();

        while (csv.hasMoreTokens()) {
            specs.add(new RangeSpec(csv.nextToken()));
        }

        return new ByteRangeSet(specs);
    }

    public int size() {
        return this.specs.length;
    }

    public RangeSpec get(int i) {
        return this.specs[i];
    }

}
