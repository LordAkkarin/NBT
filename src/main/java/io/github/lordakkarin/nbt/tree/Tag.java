/*
 * Copyright 2018 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.lordakkarin.nbt.tree;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.lordakkarin.nbt.event.TagType;
import io.github.lordakkarin.nbt.event.TagVisitor;

/**
 * Represents a basic NBT tag.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface Tag {

  /**
   * Passes the entirety of a tag and all of its children to a visitor.
   *
   * @param visitor a visitor.
   */
  void accept(@NonNull TagVisitor visitor);

  /**
   * Retrieves the tag's type.
   *
   * @return a type.
   */
  @NonNull
  TagType getType();
}
