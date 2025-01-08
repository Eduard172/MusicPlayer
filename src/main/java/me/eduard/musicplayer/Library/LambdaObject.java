package me.eduard.musicplayer.Library;

/**
 * This object's purpose is to easily modify values within lambda expressions without the need to create arrays or other bypasses.
 */
@SuppressWarnings("unused")
public class LambdaObject<Type> {
    private Type value;
    public LambdaObject(){
    }
    public LambdaObject(Type value){
        this.value = value;
    }
    public Type get(){
        return this.value;
    }
    public Type setAndGet(Type value){
        this.value = value;
        return this.value;
    }
    public void set(Type value){
        this.value = value;
    }
    public static<Type> LambdaObject<Type> of(Type value){
        return new LambdaObject<>(value);
    }
    public static<Type> LambdaObject<Type> of(){
        return new LambdaObject<>();
    }
    public SynchronizedLambdaObject<Type> toSynchronizedLambdaObject(){
        return SynchronizedLambdaObject.of(this.value);
    }
    @Override
    public String toString(){
        return String.valueOf(this.value);
    }
}