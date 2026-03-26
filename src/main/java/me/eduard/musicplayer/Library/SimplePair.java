package me.eduard.musicplayer.Library;

import java.lang.reflect.Array;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SimplePair<Key, Value> {

    private Key key;
    private Value value;

    private Consumer<Key> onKeyChange = k -> {};
    private Consumer<Value> onValueChange = v -> {};

    public SimplePair(){

    }
    public SimplePair(Key key, Value value) {
        this();
        this.key = key;
        this.value = value;
    }
    public SimplePair<Key, Value> setKey(Key key){
        this.key = key;
        this.onKeyChange.accept(key);
        return this;
    }
    public SimplePair<Key, Value> setOnKeyChange(Consumer<Key> onChange) {
        this.onKeyChange = onChange;
        return this;
    }
    public SimplePair<Key, Value> setOnValueChange(Consumer<Value> onChange) {
        this.onValueChange = onChange;
        return this;
    }
    public SimplePair<Key, Value> setValue(Value value){
        this.value = value;
        this.onValueChange.accept(value);
        return this;
    }
    public SimplePair<Key, Value> setBoth(Key key, Value value){
        return this.setKey(key).setValue(value);
    }
    public static<Key, Value> SimplePair<Key, Value> of(Key key, Value value){
        return new SimplePair<>(key, value);
    }

    @SafeVarargs
    @SuppressWarnings({"unchecked"})
    public static<Key, Value> SimplePair<Key, Value>[] parseArray(SimplePair<Key, Value>... pairs){
        int length = pairs.length;
        SimplePair<Key, Value>[] array = (SimplePair<Key, Value>[]) Array.newInstance(SimplePair.class, length);
        System.arraycopy(pairs, 0, array, 0, length);
        return array;
    }

    public Key getKey(){
        return this.key;
    }
    public Value getValue(){
        return this.value;
    }

}
