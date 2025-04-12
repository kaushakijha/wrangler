/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.CompileException;
import io.cdap.wrangler.api.CompileStatus;
import io.cdap.wrangler.api.Compiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Unit tests for validating the behavior of the RecipeCompiler.
 * These tests ensure various directive recipes compile correctly or fail as
 * expected.
 */
public class RecipeCompilerTest {

  // Compiler instance used for testing
  private static final Compiler compiler = new RecipeCompiler();

  /**
   * Test that a complex multi-line recipe compiles successfully and returns
   * correct symbols.
   */
  @Test
  public void testSuccessCompilation() throws Exception {
    try {
      CompileStatus status = compiler.compile(
          "parse-as-csv :body ' ' true;\n" +
              "set-column :abc, :edf;\n" +
              "send-to-error exp:{ window < 10 } ;\n" +
              "parse-as-simple-date :col 'yyyy-mm-dd' :col 'test' :col2,:col4,:col9 10 exp:{test < 10};\n");

      Assert.assertNotNull(status.getSymbols());
      Assert.assertEquals(4, status.getSymbols().size());
    } catch (CompileException e) {
      Assert.fail("Compilation failed unexpectedly.");
    }
  }

  /**
   * Test recipe compilation with embedded macros to verify macro skipping works.
   */
  @Test
  public void testMacroSkippingDuringParsing() throws Exception {
    String[] recipe = {
        "parse-as-csv :body ',' true;",
        "${macro1}",
        "${macro${number}}",
        "parse-as-csv :body '${delimiter}' true;"
    };

    CompileStatus status = TestingRig.compile(recipe);
    Assert.assertTrue(status.isSuccess());
  }

  /**
   * Test that a recipe with a single macro compiles successfully.
   */
  @Test
  public void testSingleMacroLikeWranglerPlugin() throws Exception {
    String[] recipe = { "${directives}" };
    CompileStatus status = TestingRig.compile(recipe);
    Assert.assertTrue(status.isSuccess());
  }

  /**
   * Test that multiple #pragma directives and macros compile correctly.
   */
  @Test
  public void testSparedPragmaLoadDirectives() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5;",
        "${directives}",
        "#pragma load-directives root1,root2,root3;"
    };
    TestingRig.compileSuccess(recipe);
  }

  /**
   * Test that nested macros do not break compilation.
   */
  @Test
  public void testNestedMacros() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5;",
        "${directives_${number}}"
    };
    TestingRig.compileSuccess(recipe);
  }

  /**
   * Missing semicolon in pragma should result in failure.
   */
  @Test
  public void testSemiColonMissing() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5",
        "${directives_${number}}"
    };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Missing opening brace in macro should result in failure.
   */
  @Test
  public void testMissingOpenBraceOnMacro() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5;",
        "$directives}"
    };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Missing closing brace in macro should result in failure.
   */
  @Test
  public void testMissingCloseBraceOnMacro() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5;",
        "${directives"
    };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Missing both braces in macro should result in failure.
   */
  @Test
  public void testMissingBothBraceOnMacro() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4,test5;",
        "${directives"
    };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Pragma without '#' should fail compilation.
   */
  @Test
  public void testMissingPragmaHash() throws Exception {
    String[] recipe = { "pragma load-directives test1,test2,test3,test4,test5;" };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Typo in pragma directive should fail compilation.
   */
  @Test
  public void testTypograhicalErrorPragmaLoadDirectives() throws Exception {
    String[] recipe = { "pragma test1,test2,test3,test4,test5;" };
    TestingRig.compileFailure(recipe);
  }

  /**
   * Test compiling a directive inside an if block with nested macros.
   */
  @Test
  public void testWithIfStatement() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2;",
        "${macro_1}",
        "if ((test > 10) && (window < 20)) { parse-as-csv :body ',' true; if (window > 10) { send-to-error exp:{test > 10}; } }"
    };
    TestingRig.compileSuccess(recipe);
  }

  /**
   * Test compilation of a complex multi-step recipe.
   */
  @Test
  public void testComplexExpression() throws Exception {
    String[] recipe = {
        "parse-as-csv body , true",
        "drop body",
        "merge body_1 body_2 Full_Name ' '",
        "drop body_1,body_2",
        "find-and-replace body_4 s/Washington//g",
        "send-to-error empty(body_4)",
        "send-to-error body_5 =~ \"DC.*\"",
        "filter-rows-on regex-match body_5 as"
    };
    CompileStatus compile = TestingRig.compile(recipe);
    Assert.assertTrue(compile.isSuccess());
  }

  /**
   * Duplicate of the previous test with different method name.
   */
  @Test
  public void test() throws Exception {
    String[] recipe = {
        "parse-as-csv body , true",
        "drop body",
        "merge body_1 body_2 Full_Name ' '",
        "drop body_1,body_2",
        "find-and-replace body_4 s/Washington//g",
        "send-to-error empty(body_4)",
        "send-to-error body_5 =~ \"DC.*\"",
        "filter-rows-on regex-match body_5 as"
    };
    CompileStatus compile = TestingRig.compile(recipe);
    Assert.assertTrue(compile.isSuccess());
  }

  /**
   * Test single line directive string with semicolon.
   */
  @Test
  public void testSingleLineDirectives() throws Exception {
    String[] recipe = { "parse-as-csv :body '\t' true; drop :body;" };
    CompileStatus compile = TestingRig.compile(recipe);
    Assert.assertTrue(compile.isSuccess());
  }

  /**
   * Test recipe with invalid directive name.
   */
  @Test
  public void testError() throws Exception {
    String[] recipe = { "parse-as-abababa-csv :body '\t' true; drop :body;" };
    CompileStatus compile = TestingRig.compile(recipe);
    Assert.assertTrue(compile.isSuccess()); // Note: If you expect failure, use compileFailure instead.
  }

  /**
   * Validate that the pragma #load-directives is correctly parsed and symbol
   * count matches.
   */
  @Test
  public void testRecipePragmaWithCompiler() throws Exception {
    String[] recipe = {
        "#pragma load-directives test1,test2,test3,test4;",
        "${directives}"
    };
    CompileStatus compile = TestingRig.compile(recipe);
    Set<String> loadableDirectives = compile.getSymbols().getLoadableDirectives();
    Assert.assertEquals(4, loadableDirectives.size());
  }
}