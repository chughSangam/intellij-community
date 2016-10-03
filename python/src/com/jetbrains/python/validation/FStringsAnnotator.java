/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.validation;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.text.CharArrayUtil;
import com.jetbrains.python.codeInsight.fstrings.FStringParser;
import com.jetbrains.python.codeInsight.fstrings.FStringParser.Fragment;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.python.psi.PyUtil.StringNodeInfo;

/**
 * @author Mikhail Golubev
 */
public class FStringsAnnotator extends PyAnnotator {
  @Override
  public void visitPyStringLiteralExpression(PyStringLiteralExpression pyString) {
    for (ASTNode node : pyString.getStringNodes()) {
      final StringNodeInfo nodeInfo = new StringNodeInfo(node);
      final String nodeText = node.getText();
      if (nodeInfo.isFormatted()) {
        final int nodeContentEnd = nodeInfo.getContentRange().getEndOffset();
        final FStringParser.ParseResult result = FStringParser.parse(nodeText);
        boolean hasUnclosedBrace = false;
        for (Fragment fragment : result.getFragments()) {
          if (fragment.getDepth() > 2) {
            // Do not report anything about expression fragments nested deeper that three times
            if (fragment.getDepth() == 3) {
              final int fragmentEndOffset = fragment.getRightBraceOffset() < 0 ? nodeContentEnd : fragment.getRightBraceOffset() + 1;
              final TextRange range = TextRange .create(fragment.getLeftBraceOffset(), fragmentEndOffset);
              report("Expression fragment inside f-string is nested too deeply", range, node);
            }
            continue;
          }
          final int fragContentEnd = fragment.getContentEndOffset();
          if (CharArrayUtil.isEmptyOrSpaces(nodeText, fragment.getLeftBraceOffset() + 1, fragment.getContentEndOffset())) {
            report("Empty expressions are not allowed inside f-strings", fragment.getContentRange(), node);
          }
          if (fragment.getRightBraceOffset() == -1) {
            hasUnclosedBrace = true;
          }
          if (fragment.getFirstHashOffset() != -1) {
            final TextRange range = TextRange.create(fragment.getFirstHashOffset(), fragment.getContentEndOffset());
            report("Expression fragments inside f-strings cannot include line comments", range, node);
          }
          for (int i = fragment.getLeftBraceOffset() + 1; i < fragment.getContentEndOffset(); i++) {
            final char c = nodeText.charAt(i);
            if (c == '\\') {
              reportCharacter("Expression fragments inside f-strings cannot include backslashes", i, node);
            }
          }
          // Do not warn about illegal conversion character if '!' is right before closing quotes 
          if (fragContentEnd < nodeContentEnd && nodeText.charAt(fragContentEnd) == '!' && fragContentEnd + 1 < nodeContentEnd) {
            final char conversionChar = nodeText.charAt(fragContentEnd + 1);
            final int offset = fragContentEnd + 1;
            if (fragContentEnd + 1 == fragment.getRightBraceOffset() || conversionChar == ':') {
              reportEmpty("Conversion character is expected: should be one of 's', 'r', 'a'", offset, node);
            }
            else if ("sra".indexOf(conversionChar) < 0) {
              reportCharacter("Illegal conversion character '" + conversionChar + "': should be one of 's', 'r', 'a'", offset, node);
            }
          }
        }
        for (Integer offset : result.getSingleRightBraces()) {
          reportCharacter("Single '}' is not allowed inside f-strings", offset, node);
        }
        if (hasUnclosedBrace) {
          reportEmpty("'}' is expected", nodeContentEnd, node);
        }
      }
    }
  }

  private void report(@NotNull String message, @NotNull TextRange range, @NotNull ASTNode node) {
    getHolder().createErrorAnnotation(range.shiftRight(node.getTextRange().getStartOffset()), message);
  }

  private void reportEmpty(@NotNull String message, int offset, @NotNull ASTNode node) {
    report(message, TextRange.from(offset, 0), node);
  }

  private void reportCharacter(@NotNull String message, int offset, @NotNull ASTNode node) {
    report(message, TextRange.from(offset, 1), node);
  }
}
