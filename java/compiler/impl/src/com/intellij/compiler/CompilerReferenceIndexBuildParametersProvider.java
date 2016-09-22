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
package com.intellij.compiler;

import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.find.bytecode.CompilerReferenceService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.backwardRefs.BackwardReferenceIndexWriter;

import java.util.Collections;
import java.util.List;

public class CompilerReferenceIndexBuildParametersProvider extends BuildProcessParametersProvider {
  @NotNull
  @Override
  public List<String> getVMArguments() {
    return CompilerReferenceService.isEnabled()
           ? Collections.singletonList("-D" + BackwardReferenceIndexWriter.PROP_KEY + "=true")
           : Collections.emptyList();
  }
}