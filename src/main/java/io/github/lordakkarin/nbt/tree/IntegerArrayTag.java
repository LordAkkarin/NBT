package io.github.lordakkarin.nbt.tree;

import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.github.lordakkarin.nbt.event.TagType;
import io.github.lordakkarin.nbt.event.TagVisitor;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@NotThreadSafe
public class IntegerArrayTag implements Iterable<Integer>, Tag {
    private int[] value;

    public IntegerArrayTag() {
        this.value = new int[0];
    }

    public IntegerArrayTag(int[] value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Integer> iterator() {
        Integer[] obj = new Integer[this.value.length];

        for (int i = 0; i < obj.length; ++i) {
            obj[i] = this.value[i];
        }

        return new ArrayIterator<>(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(@Nonnull TagVisitor visitor) {
        visitor.visitIntegerArray(this.value.length);

        for (int value : this.value) {
            visitor.visitInteger(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public TagType getType() {
        return TagType.INTEGER_ARRAY;
    }

    @Nonnull
    public int[] getValue() {
        return this.value;
    }

    public void setValue(@Nonnull int[] value) {
        this.value = value;
    }

    public int get(@Nonnegative int index) {
        return this.value[index];
    }

    public void set(@Nonnegative int index, int value) {
        this.value[index] = value;
    }

    @Nonnegative
    public int getLength() {
        return this.value.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerArrayTag)) return false;

        IntegerArrayTag that = (IntegerArrayTag) o;
        return Arrays.equals(this.value, that.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }
}