/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.wuhao.code.check.style.arrangement.kotlin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author Denis Zhdanov
 * @since 9/19/12 6:41 PM
 */
public class KotlinArrangementEntryDependencyInfo {

  @NotNull
  private final KotlinElementArrangementEntry myAnchorEntry;
  @NotNull
  private final List<KotlinArrangementEntryDependencyInfo> myDependentEntries = new ArrayList<>();

  public KotlinArrangementEntryDependencyInfo(@NotNull KotlinElementArrangementEntry entry) {
    myAnchorEntry = entry;
  }

  public void addDependentEntryInfo(@NotNull KotlinArrangementEntryDependencyInfo info) {
    myDependentEntries.add(info);
  }

  @NotNull
  public KotlinElementArrangementEntry getAnchorEntry() {
    return myAnchorEntry;
  }

  @NotNull
  public List<KotlinArrangementEntryDependencyInfo> getDependentEntriesInfos() {
    return myDependentEntries;
  }

  @Override
  public String toString() {
    return myAnchorEntry.toString();
  }
}

