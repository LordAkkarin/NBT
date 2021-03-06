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
package io.github.lordakkarin.nbt.event;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Stack;

/**
 * Provides a visitor which validates the structure of the passed data against format conventions.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ValidationVisitor extends AbstractTagVisitor {

  private final Stack<Integer> listCounter = new Stack<>();
  private final Stack<TagType> listType = new Stack<>();
  private final Stack<TagType> stack = new Stack<>();
  private String key;

  public ValidationVisitor(@Nullable TagVisitor next) {
    super(next);
  }

  private void verifyListStack() {
    int length = this.listCounter.pop() - 1;

    if (length <= 0) {
      TagType type = this.stack.pop();

      if (type == TagType.LIST) {
        this.listType.pop();
      }
    } else {
      this.listCounter.push(length);
    }
  }

  private void verifyValue(@NonNull TagType type) {
    if (!this.stack.isEmpty()) {
      TagType parent = this.stack.peek();

      switch (parent) {
        case BYTE_ARRAY:
          if (type != TagType.BYTE) {
            throw new IllegalStateException(
                "Invalid array element: Expected tag of type BYTE but got " + type);
          }

          this.verifyListStack();
          break;
        case LIST:
          TagType listType = this.listType.peek();

          if (type != listType) {
            throw new IllegalStateException(
                "Invalid list element: Expected tag of type " + listType + " but got " + type);
          }

          this.verifyListStack();
          break;
        case COMPOUND:
          if (this.key == null) {
            throw new IllegalStateException("Invalid compound element: Expected key but got value");
          }

          this.key = null;
          break;
        case INTEGER_ARRAY:
          if (type != TagType.INTEGER) {
            throw new IllegalStateException(
                "Invalid array element: Expected tag of type INTEGER but got " + type);
          }

          this.verifyListStack();
          break;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitByte(byte value) {
    this.verifyValue(TagType.BYTE);

    super.visitByte(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitByteArray(int length) {
    this.verifyValue(TagType.BYTE_ARRAY);
    this.stack.push(TagType.BYTE_ARRAY);
    this.listCounter.push(length);

    super.visitByteArray(length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitCompound() {
    this.verifyValue(TagType.COMPOUND);
    this.stack.push(TagType.COMPOUND);

    super.visitCompound();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitCompoundEnd() {
    if (this.stack.peek() != TagType.COMPOUND) {
      throw new IllegalStateException("Did not expected END in container tag " + this.stack.peek());
    }

    this.stack.pop();

    super.visitCompoundEnd();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitDouble(double value) {
    this.verifyValue(TagType.DOUBLE);

    super.visitDouble(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitFloat(float value) {
    this.verifyValue(TagType.FLOAT);

    super.visitFloat(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitInteger(int value) {
    this.verifyValue(TagType.INTEGER);

    super.visitInteger(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitIntegerArray(int length) {
    this.verifyValue(TagType.BYTE_ARRAY);
    this.stack.push(TagType.INTEGER_ARRAY);
    this.listCounter.push(length);

    super.visitIntegerArray(length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitKey(@NonNull String name) {
    this.key = name;

    super.visitKey(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitList(@Nullable TagType type, int length) {
    this.verifyValue(TagType.LIST);
    this.stack.push(TagType.LIST);
    this.listType.push(type);
    this.listCounter.push(length);

    super.visitList(type, length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitLong(long value) {
    this.verifyValue(TagType.LONG);

    super.visitLong(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitRoot(@NonNull String name) {
    if (!this.stack.isEmpty()) {
      throw new IllegalStateException("Unexpected root tag");
    }

    this.verifyValue(TagType.COMPOUND);
    this.stack.push(TagType.COMPOUND);

    super.visitRoot(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitShort(short value) {
    this.verifyValue(TagType.SHORT);

    super.visitShort(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitString(@NonNull String value) {
    this.verifyValue(TagType.STRING);

    super.visitString(value);
  }
}
