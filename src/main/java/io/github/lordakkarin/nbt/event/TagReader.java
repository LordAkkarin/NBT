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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Reads an NBT encoded (and optionally gzipped) stream of data and passes it to one or more
 * instances of {@link TagVisitor}.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TagReader {

  private final ByteBuf buffer;

  public TagReader(@NonNull ReadableByteChannel channel) throws IOException {
    this.buffer = Unpooled.directBuffer();

    {
      ByteBuffer tmp = ByteBuffer.allocateDirect(128);
      ByteBuf wrapped = Unpooled.wrappedBuffer(tmp);
      int length;

      while ((length = channel.read(tmp)) > 0) {
        tmp.flip();
        this.buffer.writeBytes(wrapped, length);

        wrapped.resetReaderIndex();
        tmp.rewind();
      }
    }
  }

  public TagReader(@NonNull InputStream inputStream) throws IOException {
    this(Channels.newChannel(inputStream));
  }

  public TagReader(@NonNull Path path) throws IOException {
    this(FileChannel.open(path, StandardOpenOption.READ));
  }

  public TagReader(@NonNull File file) throws IOException {
    this(file.toPath());
  }

  /**
   * Parses the encoded data and passes it to the specified visitor.
   *
   * @param visitor a visitor.
   */
  public void accept(@NonNull TagVisitor visitor) {
    this.buffer.markReaderIndex();

    try {
      TagType tagType = TagType.byTypeId(this.buffer.readByte());

      if (tagType != TagType.COMPOUND) {
        throw new IllegalStateException("Malformed NBT data: Expected compound but got " + tagType);
      }

      visitor.visitRoot(this.readString());

      while (this.buffer.isReadable()) {
        TagType elementType = TagType.byTypeId(this.buffer.readByte());

        if (elementType == TagType.END) {
          visitor.visitCompoundEnd();
          break;
        }

        visitor.visitKey(this.readString());
        this.visitValue(visitor, elementType);
      }
    } finally {
      this.buffer.resetReaderIndex();
    }
  }

  /**
   * Reads an UTF-8 encoded string from the buffer.
   *
   * @return a string.
   */
  @NonNull
  private String readString() {
    int length = this.buffer.readUnsignedShort();

    byte[] encoded = new byte[length];
    this.buffer.readBytes(encoded);

    return new String(encoded, StandardCharsets.UTF_8);
  }

  /**
   * Visits the raw value based on a given type.
   *
   * @param visitor a visitor.
   * @param tagType a tag type.
   */
  private void visitValue(@NonNull TagVisitor visitor, @NonNull TagType tagType) {
    switch (tagType) {
      case BYTE:
        visitor.visitByte(this.buffer.readByte());
        break;
      case SHORT:
        visitor.visitShort(this.buffer.readShort());
        break;
      case INTEGER:
        visitor.visitInteger(this.buffer.readInt());
        break;
      case LONG:
        visitor.visitLong(this.buffer.readLong());
        break;
      case FLOAT:
        visitor.visitFloat(this.buffer.readFloat());
        break;
      case DOUBLE:
        visitor.visitDouble(this.buffer.readDouble());
        break;
      case BYTE_ARRAY: {
        int length = this.buffer.readInt();
        visitor.visitByteArray(length);

        for (int i = 0; i < length; ++i) {
          visitor.visitByte(this.buffer.readByte());
        }

        break;
      }
      case STRING:
        visitor.visitString(this.readString());
        break;
      case LIST: {
        TagType elementType = TagType.byTypeId(this.buffer.readByte());
        int length = this.buffer.readInt();

        visitor.visitList(elementType, length);

        for (int i = 0; i < length; ++i) {
          this.visitValue(visitor, elementType);
        }

        break;
      }
      case COMPOUND:
        visitor.visitCompound();

        while (true) {
          TagType elementType = TagType.byTypeId(this.buffer.readByte());

          if (elementType == TagType.END) {
            visitor.visitCompoundEnd();
            break;
          }

          visitor.visitKey(this.readString());
          this.visitValue(visitor, elementType);
        }

        break;
      case INTEGER_ARRAY: {
        int length = this.buffer.readInt();
        visitor.visitIntegerArray(length);

        for (int i = 0; i < length; ++i) {
          visitor.visitInteger(this.buffer.readInt());
        }
      }
      default:
        throw new IllegalStateException("Did not expected tag of type " + tagType + " here");
    }
  }
}
