/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.webbitserver.netty;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ByteRangeTest {

    @Test
    public void parseReturnsANonNullResult() {
        ByteRangeSet brs = ByteRangeSet.parse("0-499", 10000);
        assertNotNull(brs);
    }

    @Test
    public void parsePrefixRange() {
        ByteRangeSet brs = ByteRangeSet.parse("0-499", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(499, spec.end);
        assertEquals(500, spec.length);
    }

    @Test
    public void parseBoundedRange() {
        ByteRangeSet brs = ByteRangeSet.parse("500-999", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(500, spec.start);
        assertEquals(999, spec.end);
        assertEquals(500, spec.length);
    }

    @Test
    public void parseLast500BytesSuffixRange() {
        ByteRangeSet brs = ByteRangeSet.parse("-500", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(9500, spec.start);
        assertEquals(9999, spec.end);
        assertEquals(500, spec.length);
    }

    @Test
    public void parseLast500BytesPrefixRange() {
        ByteRangeSet brs = ByteRangeSet.parse("9500-", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(9500, spec.start);
        assertEquals(9999, spec.end);
        assertEquals(500, spec.length);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void malformedByteRangeFailsLoudly() {
        ByteRangeSet.parse("-200-", 10000);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void randomStringByteRangeFailsLoudly() {
        ByteRangeSet.parse("sdfasdfkasdf;iasd", 10000);
    }

    /**
     * {@code curl -v -H "Range: bytes=100-199,300-399" http://apache.org/}
     * returns a multipart/byteranges response.
     * 
     * @throws Exception
     */
    @Test
    public void canParseMultipleByteRangeSpecs() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("0-199,400-599", 10000);
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
    public void canMergeAdjacentRangeSpecs() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("0-199,200-399", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(399, spec.end);
        assertEquals(400, spec.length);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void boundedRangeWithStartAfterEndIsInvalid() throws Exception {
        ByteRangeSet.parse("500-499", 10000);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void negativeSuffixRangeIsInvalid() throws Exception {
        ByteRangeSet.parse("--500", 10000);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void emptyRangeIsInvalid() throws Exception {
        ByteRangeSet.parse("", 10000);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void whitespaceRangeIsInvalid() throws Exception {
        ByteRangeSet.parse("   ", 10000);
    }

    @Test(expected = SyntacticallyInvalidByteRangeException.class)
    public void nullRangeIsInvalid() throws Exception {
        ByteRangeSet.parse(null, 10000);
    }

    @Test
    public void byteRangeSetContainingPrefixRangeStartingAtZeroMergesToSingleRangeSpec() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("0-499,9500-,0-", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(0, spec.start);
        assertEquals(9999, spec.end);
        assertEquals(10000, spec.length);
    }

    @Test
    public void rangeWithEndGreaterThanContentLengthHasEndTreatedAsContentLengthMinusOne() throws Exception {
        ByteRangeSet brs = ByteRangeSet.parse("9500-10000", 10000);
        assertEquals(1, brs.size());
        RangeSpec spec = brs.get(0);
        assertEquals(9500, spec.start);
        assertEquals(9999, spec.end);
        assertEquals(500, spec.length);
    }

}
