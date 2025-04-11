/*
 * Copyright © 2025 Khushi Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Token representing a Byte Size value (e.g., "10KB", "1.5MB").
 */
public class ByteSize implements Token {
    private final String original;
    private final long bytes;

    public ByteSize(String value) {
        this.original = value;
        this.bytes = parseBytes(value);
    }

    private long parseBytes(String value) {
        value = value.trim().toUpperCase();
        if (value.endsWith("KB")) {
            return (long) (Double.parseDouble(value.replace("KB", "")) * 1024);
        }
        if (value.endsWith("MB")) {
            return (long) (Double.parseDouble(value.replace("MB", "")) * 1024 * 1024);
        }
        if (value.endsWith("GB")) {
            return (long) (Double.parseDouble(value.replace("GB", "")) * 1024 * 1024 * 1024);
        }
        if (value.endsWith("TB")) {
            return (long) (
                    Double.parseDouble(value.replace("TB", "")) * 1024L * 1024 * 1024 * 1024
            );
        }
        if (value.endsWith("B")) {
            return Long.parseLong(value.replace("B", ""));
        }
        throw new IllegalArgumentException("Invalid byte size unit: " + value);
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("original", original);
        json.addProperty("bytes", bytes);
        return json;
    }
}
