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

package io.cdap.wrangler.directive;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.List;

/**
 * Aggregates byte sizes and time durations across multiple rows.
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Description("Aggregates byte sizes and time durations, producing a single row with total values.")
public class AggregateStats implements Directive {
    public static final String NAME = "aggregate-stats";

    private String sizeColumn;
    private String timeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;

    private long totalBytes = 0;
    private long totalMilliseconds = 0;
    private int rowCount = 0;

    /**
     * Defines the expected arguments for this directive.
     *
     * @return UsageDefinition object defining required arguments.
     */
    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("sizeColumn", TokenType.COLUMN_NAME);
        builder.define("timeColumn", TokenType.COLUMN_NAME);
        builder.define("outputSizeColumn", TokenType.COLUMN_NAME);
        builder.define("outputTimeColumn", TokenType.COLUMN_NAME);
        builder.define("unitSize", TokenType.BYTE_SIZE, true); // Optional Byte Size
        builder.define("unitTime", TokenType.TIME_DURATION, true); // Optional Time Duration
        return builder.build();
    }

    /**
     * Initializes directive with provided arguments.
     *
     * @param arguments The parsed arguments provided to the directive.
     */
    @Override
    public void initialize(Arguments arguments) {
        sizeColumn = arguments.value("sizeColumn").value().toString();
        timeColumn = arguments.value("timeColumn").value().toString();
        outputSizeColumn = arguments.value("outputSizeColumn").value().toString();
        outputTimeColumn = arguments.value("outputTimeColumn").value().toString();
    }

    /**
     * Executes aggregation logic across rows.
     *
     * @param rows The input rows to process.
     * @param ctx  The execution context.
     * @return A list containing a single aggregated row.
     * @throws DirectiveExecutionException If an error occurs during execution.
     */
    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext ctx) throws DirectiveExecutionException {
        totalBytes = 0;
        totalMilliseconds = 0;
        rowCount = 0;

        for (Row row : rows) {
            Object sizeValue = row.getValue(sizeColumn);
            Object timeValue = row.getValue(timeColumn);

            if (sizeValue != null) {
                totalBytes += parseByteSize(sizeValue.toString());
            }
            if (timeValue != null) {
                totalMilliseconds += parseTimeDuration(timeValue.toString());
            }
            rowCount++;
        }

        // Convert results to MB and seconds
        double totalSizeMB = totalBytes / (1024.0 * 1024);
        double totalTimeSeconds = totalMilliseconds / 1000.0;

        // Return a single aggregated row
        Row aggregatedRow = new Row();
        aggregatedRow.add(outputSizeColumn, totalSizeMB);
        aggregatedRow.add(outputTimeColumn, totalTimeSeconds);

        return Collections.singletonList(aggregatedRow);
    }

    /**
     * Converts size values (e.g., "2GB", "500MB") into bytes.
     *
     * @param size The size string.
     * @return Equivalent bytes.
     */
    private long parseByteSize(String size) {
        if (size == null || size.isEmpty()) {
            return 0;
        }
        size = size.trim().toUpperCase();

        try {
            if (size.endsWith("GB")) {
                return Long.parseLong(size.replace("GB", "").trim()) * 1024 * 1024 * 1024;
            }
            if (size.endsWith("MB")) {
                return Long.parseLong(size.replace("MB", "").trim()) * 1024 * 1024;
            }
            if (size.endsWith("KB")) {
                return Long.parseLong(size.replace("KB", "").trim()) * 1024;
            }
            if (size.endsWith("B")) {
                return Long.parseLong(size.replace("B", "").trim());
            }
            return Long.parseLong(size); // Default to bytes
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid byte size format: " + size, e);
        }
    }

    /**
     * Converts time values (e.g., "5min", "2h") into milliseconds.
     *
     * @param time The time duration string.
     * @return Equivalent milliseconds.
     */
    private long parseTimeDuration(String time) {
        if (time == null || time.isEmpty()) {
            return 0;
        }
        time = time.trim().toLowerCase();

        try {
            if (time.endsWith("h")) {
                return Long.parseLong(time.replace("h", "").trim()) * 60 * 60 * 1000;
            }
            if (time.endsWith("min")) {
                return Long.parseLong(time.replace("min", "").trim()) * 60 * 1000;
            }
            if (time.endsWith("s")) {
                return Long.parseLong(time.replace("s", "").trim()) * 1000;
            }
            if (time.endsWith("ms")) {
                return Long.parseLong(time.replace("ms", "").trim());
            }
            return Long.parseLong(time); // Default to milliseconds
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + time, e);
        }
    }

    /**
     * Cleans up resources if necessary.
     */
    @Override
    public void destroy() {
        // No-op
    }
}
