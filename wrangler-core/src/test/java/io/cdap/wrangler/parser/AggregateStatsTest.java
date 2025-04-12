/*
 * Copyright © 2025 Khushi Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
*/

package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testAggregateStatsCalculation() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row().add("data_transfer_size", "10kb").add("response_time", "2s"),
                new Row().add("data_transfer_size", "1.5MB").add("response_time", "500ms"));

        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());

        // Calculation
        long totalBytes = 10 * 1024 + (long) (1.5 * 1024 * 1024); // 10kb + 1.5MB
        double expectedTotalSizeMB = totalBytes / (1024.0 * 1024.0);

        long totalNanoSeconds = 2_000_000_000L + 500_000_000L;
        double expectedTotalTimeSeconds = totalNanoSeconds / 1_000_000_000.0;

        Row output = results.get(0);
        Assert.assertEquals(expectedTotalSizeMB,
                ((Number) output.getValue("total_size_mb")).doubleValue(), 0.001);
        Assert.assertEquals(expectedTotalTimeSeconds,
                ((Number) output.getValue("total_time_sec")).doubleValue(), 0.001);
    }
}
