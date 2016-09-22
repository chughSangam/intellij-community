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
package org.jetbrains.references

import com.intellij.openapi.application.ex.PathManagerEx
import com.intellij.openapi.util.io.FileUtil

class ReferenceIndexTest : ReferenceIndexTestBase() {
  override fun getTestDataRootPath(): String {
    return FileUtil.toCanonicalPath(PathManagerEx.findFileUnderCommunityHome("jps/jps-builders/testData/referencesIndex").absolutePath, '/')
  }

  fun testIncrementalIndexUpdate() {
    assertIndexOnRebuild("Bar.java", "Foo.java", "FooImpl.java")
    changeFileContent("Bar.java", "Bar_1.java")
    buildAllModules()
    assertIndexEquals("afterMakeIndex.txt")
    changeFileContent("FooImpl.java", "FooImpl_2.java")
    buildAllModules()
    assertIndexEquals("afterSecondMakeIndex.txt")
  }

  fun testFileCaseOnlyRename() {
    assertIndexOnRebuild("Bar.java")
    renameFile("Bar.java", "bar.java")
    buildAllModules()
    assertIndexEquals("afterRename.txt")
  }

  fun testUnusedImports() {
    assertIndexOnRebuild("Bar.java")
  }

  fun testUnusedImports2() {
    assertIndexOnRebuild("Main.java",
                         "com/ru/Some.java",
                         "com/ru/Some2.java",
                         "com/ru/Some3.java")
  }

  fun testDeadCode() {
    assertIndexOnRebuild("Bar.java")
  }

  fun testPackageInfo() {
    assertIndexOnRebuild("myPackage/package-info.java")
  }

  fun testArrayRefs() {
    assertIndexOnRebuild("Array.java", "Foo.java", "Bar.java")
  }

  fun testTypeParameterRefs() {
    assertIndexOnRebuild("TypeParam.java")
  }
}

