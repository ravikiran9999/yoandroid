/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yo.android.util;

public class SmartDialNameMatcher {


    /**
     * Strips a phone number of unnecessary characters (spaces, dashes, etc.)
     *
     * @param number Phone number we want to normalize
     * @return Phone number consisting of digits from 0-9
     */
    public static String normalizeNumber(String number, SmartDialMap map) {
        return normalizeNumber(number, 0, map);
    }

    /**
     * Strips a phone number of unnecessary characters (spaces, dashes, etc.)
     *
     * @param number Phone number we want to normalize
     * @param offset Offset to start from
     * @return Phone number consisting of digits from 0-9
     */
    public static String normalizeNumber(String number, int offset, SmartDialMap map) {
        final StringBuilder s = new StringBuilder();
        for (int i = offset; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (map.isValidDialpadNumericChar(ch)) {
                s.append(ch);
            }
        }
        return s.toString();
    }

}