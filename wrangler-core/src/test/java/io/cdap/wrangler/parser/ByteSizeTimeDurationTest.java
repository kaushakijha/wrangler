/*
 * Copyright © 2025 The Author
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

package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTimeDurationTest {

    @Test
    public void testByteSizeParsing() {
        Assert.assertEquals(10240, new ByteSize("10KB").getBytes()); // 10 KB = 10240 bytes
        Assert.assertEquals(1048576, new ByteSize("1MB").getBytes()); // 1 MB = 1048576 bytes
        Assert.assertEquals(1073741824, new ByteSize("1GB").getBytes()); // 1 GB = 1073741824 bytes
    }

    @Test
    public void testTimeDurationParsing() {
        Assert.assertEquals(5000, new TimeDuration("5s").getMilliseconds()); // 5 seconds = 5000 ms
        Assert.assertEquals(1500, new TimeDuration("1500ms").getMilliseconds()); // 1500ms
        Assert.assertEquals(120000, new TimeDuration("2min").getMilliseconds()); // 2 min = 120000 ms
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidByteSize() {
        new ByteSize("XYZ"); // Invalid input should throw an error
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("abc"); // Invalid input should throw an error
    }
}
