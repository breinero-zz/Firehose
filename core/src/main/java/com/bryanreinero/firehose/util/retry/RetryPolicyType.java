package com.bryanreinero.firehose.util.retry;

/**
 * Created by bryan on 10/23/15.
 */
public enum RetryPolicyType {
    unknown(0x00), regularInterval(0x01);

    private final int value;
    private static final RetryPolicyType[] LOOKUP_TABLE = new RetryPolicyType[ unknown.getValue() + 1];

    static {
        for (final RetryPolicyType cur : RetryPolicyType.values()) {
            LOOKUP_TABLE[cur.getValue()] = cur;
        }
    }

    RetryPolicyType ( int value ) { this.value = value; }

    /**
     * Get the int value of this BSON type.
     *
     * @return the int value of this type.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the {@code BsonType} that corresponds to the given int value.
     *
     * @param value the String value of the desired Retry Policy type.
     * @return the corresponding {@code BsonType}
     */
    public static RetryPolicyType findByValue(final int value ) {
        return LOOKUP_TABLE[value & 0xFF];
    }

}
