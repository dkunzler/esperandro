package de.devland.esperandro;

import android.content.Context;

/**
 * This interface defines some actions that expose some kind of unsafe operations of the Esperandro-generated classes.
 * Unsafe in this case means that type-safety can not be guaranteed and some Exceptions can occur in contrast to other
 * Esperandro generated methods.
 */
public interface UnsafeActions {
    /**
     * This method retrieves a saved preference value by the String resource id that was generated via
     * {@link de.devland.esperandro.annotations.experimental.GenerateStringResources}. If a string unknown to esperandro
     * is provded an {@link UnknownKeyException} is thrown.
     *
     * @param context An Android context to get a string value to the given resource id.
     * @param prefId  The resource Id of a String that references a preference within the interface extending from this
     *                class
     * @param <V>     The type the returned value will be cast to.
     * @return The value of the preference.
     * @throws UnknownKeyException when a resource Id was given that don't denote a string known to esperandro.
     * @throws ClassCastException  when the saved preference does not match the desired return type.
     */
    <V> V getValue(Context context, int prefId) throws UnknownKeyException;

    /**
     * This method saved a preference under the key denoted by the given String resource id.
     *
     * @param context An Android context to get a string value to the given resource id.
     * @param prefId  The resource Id of a String that references a preference within the interface extending from this
     *                class
     * @param pref    The value to be saved in the given preference key.
     * @param <V>     The type value to be saved.
     * @throws UnknownKeyException when a resource Id was given that don't denote a string known to esperandro.
     * @throws ClassCastException  when the given pref does not match the type of the given preference key.
     */
    <V> void setValue(Context context, int prefId, V pref) throws UnknownKeyException;

    class UnknownKeyException extends Exception {
        private String prefKey;

        public UnknownKeyException(String prefKey) {
            super(prefKey);
            this.prefKey = prefKey;
        }

        public String getPrefKey() {
            return prefKey;
        }
    }
}
