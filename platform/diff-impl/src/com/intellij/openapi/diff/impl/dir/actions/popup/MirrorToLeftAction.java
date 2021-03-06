/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package com.intellij.openapi.diff.impl.dir.actions.popup;

import com.intellij.ide.diff.DirDiffElement;
import com.intellij.ide.diff.DirDiffOperation;
import com.intellij.openapi.diff.impl.dir.DirDiffElementImpl;
import org.jetbrains.annotations.NotNull;

public class MirrorToLeftAction extends SetOperationToBase {
  @Override
  protected boolean isEnabledFor(DirDiffElement element) {
    return true;
  }

  @NotNull
  @Override
  protected DirDiffOperation getOperation(DirDiffElementImpl element) {
    if (element.isSource()) {
      return DirDiffOperation.DELETE;
    }
    else {
      return DirDiffOperation.COPY_FROM;
    }
  }
}
