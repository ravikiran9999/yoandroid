package com.orion.android.common.util;

import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;

/**
 * Created by Ramesh on 13/3/16.
 */
public class SpannableHelper {

    private SpannableHelper() {
        //Default constructor
    }

    public static CharSequence applySpannable(final CharSequence text, final String token, final CharacterStyle... cs) {
        final int tokenLen = token.length();
        final int start = text.toString().indexOf(token) + tokenLen;
        final int end = text.toString().indexOf(token, start);

        if (start > -1 && end > -1) {
            final SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            for (final CharacterStyle c : cs) {
                ssb.setSpan(c, start, end, 0);
            }

            ssb.delete(end, end + tokenLen);
            ssb.delete(start - tokenLen, start);

            return ssb;
        }
        return text;
    }
}
