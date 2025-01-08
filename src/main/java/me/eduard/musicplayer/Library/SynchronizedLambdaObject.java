package me.eduard.musicplayer.Library;

@SuppressWarnings("unused")
public final class SynchronizedLambdaObject<Type>{
    private Type value;
    public SynchronizedLambdaObject(){
    }
    public SynchronizedLambdaObject(Type value){
        this.value = value;
    }
    public synchronized Type get(){
        return this.value;
    }
    public synchronized Type setAndGet(Type value){
        this.value = value;
        return this.value;
    }
    public synchronized void set(Type value){
        this.value = value;
    }
    public static<Type> SynchronizedLambdaObject<Type> of(Type value){
        return new SynchronizedLambdaObject<>(value);
    }
    public LambdaObject<Type> toLambdaObject(){
        return LambdaObject.of(this.value);
    }
    @Override
    public synchronized String toString(){
        return String.valueOf(this.value);
    }
}
