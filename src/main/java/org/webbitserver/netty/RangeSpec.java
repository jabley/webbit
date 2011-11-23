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

    public final int start;

    public final int end;

    public final int length;

    public final Type type;

    public RangeSpec(String byteRangeSpec) {
        int pos = byteRangeSpec.indexOf('-');

        try {
            switch (pos) {
            case -1:
                throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec);
            case 0:
                this.type = Type.SUFFIX_RANGE;

                /*
                 * c.f. http://tools.ietf.org/html/rfc2616#page-139
                 * 
                 * "If the entity is shorter than the specified suffix-length,
                 * the entire entity-body is used."
                 */
                this.start = Integer.parseInt(byteRangeSpec.substring(1));
                this.end = -1;
                break;
            default:
                this.start = Integer.parseInt(byteRangeSpec.substring(0, pos++));

                if (byteRangeSpec.length() == pos) {
                    this.type = Type.PREFIX_RANGE;
                    this.end = -1;
                } else {
                    this.type = Type.BOUNDED_RANGE;

                    /*
                     * c.f. http://tools.ietf.org/html/rfc2616#section-14.35
                     * 
                     * "if the value is greater than or equal to the current
                     * length of the entity-body, last-byte-pos is taken to be
                     * equal to one less than the current length of the entity-
                     * body in bytes."
                     */
                    this.end = Integer.parseInt(byteRangeSpec.substring(pos));
                }

                break;
            }

            if (end == -1) {
                this.length = -1;
            } else {
                this.length = end - start + 1;
            }

        } catch (NumberFormatException e) {
            throw new SyntacticallyInvalidByteRangeException("Invalid range-spec: " + byteRangeSpec + " ; "
                    + e.getMessage());
        }
    }

    /**
     * Returns a representation suitable for use as a Content-Range header
     * value.
     * 
     * @return
     */
    public String asContentRange() {
        return String.format("%d-%d/%s", this.start, this.end, this.length == -1 ? "*" : "" + this.length);
    }

    /**
     * Returns a representation suitable for use as a Content-Range header
     * value.
     * 
     * @param contentLength
     *            the entity size
     * 
     * @return
     */
    public String asContentRange(int contentLength) {
        return String.format("%d-%d/%d", this.start, this.end, contentLength);
    }
}
