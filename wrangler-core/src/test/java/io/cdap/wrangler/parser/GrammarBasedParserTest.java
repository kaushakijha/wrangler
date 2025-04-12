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

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.CompileStatus;
import io.cdap.wrangler.api.Compiler;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link GrammarBasedParser}
 */
public class GrammarBasedParserTest {

  @Test
  public void testBasic() throws Exception {
    String[] recipe = new String[] {
        "#pragma version 2.0;",
        "rename :col1 :col2",
        "parse-as-csv :body ',' true;",
        "#pragma load-directives text-reverse, text-exchange;",
        "${macro} ${macro_2}",
        "${macro_${test}}"
    };

    RecipeParser parser = TestingRig.parse(recipe);
    List<Directive> directives = parser.parse();
    Assert.assertEquals(2, directives.size());
  }

  @Test
  public void testLoadableDirectives() throws Exception {
    String[] recipe = new String[] {
        "#pragma version 2.0;",
        "#pragma load-directives text-reverse, text-exchange;",
        "rename col1 col2",
        "parse-as-csv body , true",
        "text-reverse :body;",
        "test prop: { a='b', b=1.0, c=true};",
        "#pragma load-directives test-change,text-exchange, test1,test2,test3,test4;"
    };

    Compiler compiler = new RecipeCompiler();
    CompileStatus status = compiler.compile(new MigrateToV2(recipe).migrate());
    Assert.assertEquals(7, status.getSymbols().getLoadableDirectives().size());
  }

  @Test
  public void testCommentOnlyRecipe() throws Exception {
    String[] recipe = new String[] {
        "// test"
    };

    RecipeParser parser = TestingRig.parse(recipe);
    List<Directive> directives = parser.parse();
    Assert.assertEquals(0, directives.size());
  }

  // ✅ Added test for BYTE_SIZE and TIME_DURATION parsing
  @Test
  public void testByteSizeAndTimeDurationParsing() throws Exception {
    ByteSize byteSize1 = new ByteSize("10KB");
    ByteSize byteSize2 = new ByteSize("1MB");
    ByteSize byteSize3 = new ByteSize("1.5GB");

    TimeDuration time1 = new TimeDuration("5ms");
    TimeDuration time2 = new TimeDuration("2.1s");
    TimeDuration time3 = new TimeDuration("3min");

    // ByteSize parsing assertions
    Assert.assertEquals(10 * 1024, byteSize1.getBytes()); // 10 KB = 10240 bytes
    Assert.assertEquals(1 * 1024 * 1024, byteSize2.getBytes()); // 1 MB = 1048576 bytes
    Assert.assertEquals(1.5 * 1024 * 1024 * 1024, byteSize3.getBytes(), 0.001); // 1.5 GB = 1610612736 bytes

    // TimeDuration parsing assertions
    Assert.assertEquals(5, time1.getMilliseconds()); // 5 ms
    Assert.assertEquals(2100, time2.getMilliseconds()); // 2.1 seconds = 2100 ms
    Assert.assertEquals(180000, time3.getMilliseconds()); // 3 min = 180000 ms
  }

  // ✅ Added test for invalid parsing
  @Test(expected = NumberFormatException.class)
  public void testInvalidByteSize() {
    new ByteSize("XYZ"); // Should throw error
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidTimeDuration() {
    new TimeDuration("2.1XX"); // Should throw error
  }
}