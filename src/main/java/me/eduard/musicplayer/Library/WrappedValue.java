package me.eduard.musicplayer.Library;

import java.util.function.Consumer;

/**
 * A simple class which wraps a value of a generic type.
 * <p>
 *     It's useful when using in lambda expressions or any other task that simple variables cannot do not behave as expected.
 * </p>
 */
@SuppressWarnings("unused")
public final class WrappedValue<Type> {
    private Type value;
    private Consumer<Type> onValueChange = v -> {};
    public WrappedValue(){
    }
    public WrappedValue(Type value){
        this.value = value;
    }
    public Type get(){
        return this.value;
    }
    public void setOnValueChange(Consumer<Type> onValueChange){
        this.onValueChange = onValueChange;
    }
    public Type setAndGet(Type value){
        this.value = value;
        this.onValueChange.accept(value);
        return this.value;
    }
    public void set(Type value){
        this.value = value;
        this.onValueChange.accept(value);
    }
    public static<Type> WrappedValue<Type> of(Type value){
        return new WrappedValue<>(value);
    }
    public static<Type> WrappedValue<Type> of(){
        return new WrappedValue<>();
    }
    @Override
    public String toString(){
        return String.valueOf(this.value);
    }
}