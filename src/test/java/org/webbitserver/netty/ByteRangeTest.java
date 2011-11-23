package org.webbitserver.netty;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class ByteRangeTest {

    @Test
    public void parseReturnsANonNullResult() {
        ByteRangeSet brs = ByteRangeSet.parse("0-199");
        assertNotNull(brs);
    }

    @Test
    public void parsePrefixRange() {
        ByteRangeSet brs = ByteRangeSet.parse("0-199");
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(199, spec.end);
        assertEquals(200, spec.length);
    }

    @Test
    public void parseBoundedRange() {
        ByteRangeSet brs = ByteRangeSet.parse("200-399");
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(200, spec.start);
        assertEquals(399, spec.end);
        assertEquals(200, spec.length);
    }

    @Test
    public void parseSuffixRange() {
        ByteRangeSet brs = ByteRangeSet.parse("200-");
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(200, spec.start);
        assertEquals(-1, spec.end);
        assertEquals(-1, spec.length);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void malformedByteRangeFailsLoudly() {
        ByteRangeSet.parse("-200-");
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void randomStringByteRangeFailsLoudly() {
        ByteRangeSet.parse("sdfasdfkasdf;iasd");
    }

    /**
     * {@code curl -v -H "Range: bytes=100-199,300-399" http://apache.org/}
     * returns a multipart/byteranges response.
     * 
     * @throws Exception
     */
    @Test
    public void canParseMultipleByteRangeSpecs() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("0-199,400-599");
        assertEquals(2, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(199, spec.end);
        assertEquals(200, spec.length);
        spec = brs.get(1);
        assertEquals(400, spec.start);
        assertEquals(599, spec.end);
        assertEquals(200, spec.length);
    }

    /**
     * {@code curl -v -H "Range: bytes=100-199,200-399" http://apache.org/}
     * returns a single text/html response with partial content.
     * 
     * @throws Exception
     */
    @Test
    @Ignore("Not bothering to implement this yet")
    public void canMergeAdjacentRangeSpecs() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("0-199,200-399");
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(399, spec.end);
        assertEquals(400, spec.length);
    }

}
