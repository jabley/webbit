package org.webbitserver.netty;

class RangeSpec {

    enum Type {

        /**
         * bytes=-2
         */
        SUFFIX_RANGE,

        /**
         * bytes=2-
         */
        PREFIX_RANGE,

        /**
         * bytes=2-199
         */
        BOUNDED_RANGE;
    }

    /**
     * Start byte offset, inclusive.
     */
    public final int start;

    /**
     * End byte offset, inclusive.
     */
    public final int end;

    /**
     * Length of the range.
     */
    public final int length;

    /**
     * Content length of the entity.
     */
    public final int contentLength;

    public final Type type;

    public RangeSpec(String byteRangeSpec, int contentLength) {
        int pos = byteRangeSpec.indexOf('-');

        try {
            switch (pos) {
            case -1:
                throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec);
            case 0:
                this.type = Type.SUFFIX_RANGE;

                int number = Integer.parseInt(byteRangeSpec.substring(1));
                if (number < 1) {
                    throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec);
                }

                /*
                 * c.f. http://tools.ietf.org/html/rfc2616#page-139
                 * 
                 * "If the entity is shorter than the specified suffix-length,
                 * the entire entity-body is used."
                 * 
                 * => use Math.max to set the final field.
                 */
                this.start = Math.max(contentLength - number, 0);
                this.end = contentLength - 1;
                break;
            default:
                this.start = Integer.parseInt(byteRangeSpec.substring(0, pos++));

                if (byteRangeSpec.length() == pos) {
                    this.type = Type.PREFIX_RANGE;
                    this.end = contentLength - 1;
                } else {
                    this.type = Type.BOUNDED_RANGE;

                    /*
                     * c.f. http://tools.ietf.org/html/rfc2616#section-14.35
                     * 
                     * "if the value is greater than or equal to the current
                     * length of the entity-body, last-byte-pos is taken to be
                     * equal to one less than the current length of the entity-
                     * body in bytes."
                     * 
                     * => use Math.min
                     */
                    this.end = Math.min(contentLength - 1, Integer.parseInt(byteRangeSpec.substring(pos)));

                    if (start > end) {
                        throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec);
                    }
                }

                break;
            }

            this.contentLength = contentLength;
            this.length = getRangeLength();

        } catch (NumberFormatException e) {
            throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec + " ; "
                    + e.getMessage());
        }
    }

    /**
     * Private constructor used to create the results of merge operations.
     * 
     * @param start
     * @param end
     * @param contentLength
     */
    private RangeSpec(int start, int end, int contentLength) {
        this.start = start;
        this.end = end;
        this.length = getRangeLength();
        this.contentLength = contentLength;
        this.type = (end == contentLength - 1) ? Type.PREFIX_RANGE : Type.BOUNDED_RANGE;
    }

    /**
     * Returns a representation suitable for use as a Content-Range header
     * value.
     * 
     * @param contentLength
     *            the entity size
     * @return a non-null String
     */
    public String asContentRange() {
        return String.format("%d-%d/%d", this.start, this.end, contentLength);
    }

    public RangeSpec attemptMerge(RangeSpec other) {
        if (intersects(other) || adjacent(other)) {
            return new RangeSpec(Math.min(start, other.start), Math.max(end, other.end), length);
        }

        return null;
    }

    public boolean intersects(RangeSpec other) {
        return (other.start <= end && other.end >= end) || (start <= other.end && end >= other.end);
    }

    public boolean adjacent(RangeSpec other) {
        return (other.start == end + 1) || (start == other.end + 1);
    }

    private int getRangeLength() {
        return end - start + 1;
    }
}
