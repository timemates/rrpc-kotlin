package app.timemate.rrpc.generator.kotlin.dyser

/**
 * Represents the values of a message in the form of a map of tag numbers to field values.
 *
 * @property values The map containing the field values, indexed by their tag numbers.
 */
public class MessageValues(
    private val values: Map<Int, Any>,
) {
    public companion object {
        public fun create(block: Builder.() -> Unit): MessageValues {
            return Builder().apply(block).build()
        }
    }

    /**
     * Retrieves an integer value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The integer value, or 0 if the field is not present.
     */
    public fun getInt(tag: Int): Int {
        return values[tag] as? Int ?: 0
    }

    /**
     * Retrieves a string value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The string value, or an empty string if the field is not present.
     */
    public fun getString(tag: Int): String {
        return values[tag] as? String ?: ""
    }

    /**
     * Retrieves a long value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The long value, or 0L if the field is not present.
     */
    public fun getLong(tag: Int): Long {
        return values[tag] as? Long ?: 0L
    }

    /**
     * Retrieves a boolean value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The boolean value, or false if the field is not present.
     */
    public fun getBoolean(tag: Int): Boolean {
        return values[tag] as? Boolean == true
    }

    /**
     * Retrieves a double value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The double value, or 0.0 if the field is not present.
     */
    public fun getDouble(tag: Int): Double {
        return values[tag] as? Double ?: 0.0
    }

    /**
     * Retrieves an unsigned long value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The unsigned long value, or 0UL if the field is not present.
     */
    public fun getULong(tag: Int): ULong {
        return values[tag] as? ULong ?: 0UL
    }

    /**
     * Retrieves a float value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The float value, or 0.0f if the field is not present.
     */
    public fun getFloat(tag: Int): Float {
        return values[tag] as? Float ?: 0.0f
    }

    /**
     * Retrieves an unsigned integer value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The unsigned integer value, or 0U if the field is not present.
     */
    public fun getUInt(tag: Int): UInt {
        return values[tag] as? UInt ?: 0U
    }

    /**
     * Retrieves a byte array value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The byte array value, or null if the field is not present.
     */
    public fun getBytes(tag: Int): ByteArray {
        return values[tag] as? ByteArray ?: byteArrayOf()
    }

    /**
     * Retrieves a nested message value by its tag number.
     *
     * @param tag The tag number of the field.
     * @return The nested message value, or null if the field is not present.
     */
    public fun getMessage(tag: Int): MessageValues? {
        return values[tag] as? MessageValues
    }

    public fun <T> getList(tag: Int): List<T> {
        @Suppress("UNCHECKED_CAST")
        return values[tag] as? List<T> ?: emptyList()
    }

    public fun <TKey, TValue> getMap(tag: Int): Map<TKey, TValue> {
        @Suppress("UNCHECKED_CAST")
        return values[tag] as? Map<TKey, TValue> ?: mapOf()
    }

    public fun isNull(tag: Int): Boolean {
        return !values.containsKey(tag) || values[tag] == null
    }

    /**
     * A builder class for constructing a [MessageValues] instance.
     *
     * @property values A mutable map for storing the field values indexed by their tag numbers.
     */
    public class Builder(
        private val values: MutableMap<Int, Any> = mutableMapOf(),
    ) {

        public operator fun set(tag: Int, value: Int) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: String) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: Long) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: Boolean) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: Double) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: ULong) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: Float) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: UInt) {
            values[tag] = value
        }

        public operator fun set(tag: Int, value: ByteArray) {
            values[tag] = value
        }

        /**
         * Sets a nested message value by its tag number.
         *
         * @param tag The tag number of the field.
         * @param builder A function for constructing the nested message value.
         */
        public fun set(tag: Int, builder: Builder.() -> Unit) {
            values[tag] = Builder().apply(builder).build()
        }

        public operator fun <T> set(tag: Int, list: List<T>) {
            values[tag] = list
        }

        public fun setRaw(tag: Int, any: Any?) {
            any?.let { values[tag] = it }
        }

        /**
         * Builds and returns a [MessageValues] instance with the stored field values.
         *
         * @return A [MessageValues] instance containing the field values.
         */
        public fun build(): MessageValues = MessageValues(values.toMap())
    }
}