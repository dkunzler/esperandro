package de.devland.esperandro;

import android.content.Context;

public interface UnsafeActions {
    <V> V getValue(Context context, int prefId);
    <V> void setValue(Context context, int prefId, V pref);

    class UnknownKeyException {
        private String prefKey;

        public UnknownKeyException(String prefKey) {
            this.prefKey = prefKey;
        }

        public String getPrefKey() {
            return prefKey;
        }
    }
}
