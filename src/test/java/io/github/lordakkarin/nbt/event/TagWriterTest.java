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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 * Provides test cases to verify the correct functionality of {@link TagWriter}.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TagWriterTest {

  @NonNull
  private ByteBuf readResource(@NonNull ReadableByteChannel channel)
      throws IOException {
    ByteBuf target = Unpooled.directBuffer();

    ByteBuffer tmp = ByteBuffer.allocateDirect(128);
    ByteBuf wrapped = Unpooled.wrappedBuffer(tmp);
    int length;

    while ((length = channel.read(tmp)) > 0) {
      tmp.flip();
      target.writeBytes(wrapped, length);

      wrapped.resetReaderIndex();
      tmp.rewind();
    }

    return target;
  }

  /**
   * Tests the generation of a bigger NBT file which contains all known tags.
   */
  @Test
  public void testBig() throws IOException {
    // TODO: We are using a reader here - This means that failure may carry over!
    final TagWriter writer = new TagWriter();

    try (InputStream inputStream = this.getClass().getResourceAsStream("/bigtest.nbt")) {
      TagReader reader = new TagReader(inputStream);
      reader.accept(writer);
    }

    Path targetPath = Files.createTempFile("mvntest_", ".nbt");

    try {
      writer.write(targetPath);

      try (InputStream inputStream = this.getClass().getResourceAsStream("/bigtest.nbt")) {
        ByteBuf expected = this.readResource(Channels.newChannel(inputStream));
        ByteBuf encoded = writer.getBuffer();

        Assert.assertArrayEquals(this.toArray(expected), this.toArray(encoded));
      }
    } finally {
      Files.deleteIfExists(targetPath);
    }
  }

  /**
   * Tests the generation of a hello world NBT file.
   */
  @Test
  public void testHelloWorld() throws IOException {
    TagWriter writer = new TagWriter();

    writer.visitKey("hello world");
    writer.visitCompound();
    {
      writer.visitKey("name");
      writer.visitString("Bananrama");
    }
    writer.visitCompoundEnd();

    try (InputStream inputStream = this.getClass().getResourceAsStream("/hello_world.nbt")) {
      ByteBuf expected = this.readResource(Channels.newChannel(inputStream));
      ByteBuf encoded = writer.getBuffer();

      Assert.assertArrayEquals(this.toArray(expected), this.toArray(encoded));
    }
  }

  @NonNull
  private byte[] toArray(@NonNull ByteBuf buffer) {
    byte[] array = new byte[buffer.readableBytes()];
    buffer.readBytes(array);

    return array;
  }
}
