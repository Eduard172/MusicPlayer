package me.eduard.musicplayer.Library;

import java.lang.reflect.Array;

public class BasicKeyValuePair<Key, Value> {

    private Key key;
    private Value value;

    public BasicKeyValuePair(){

    }
    public BasicKeyValuePair(Key key, Value value) {
        this();
        this.key = key;
        this.value = value;
    }
    public BasicKeyValuePair<Key, Value> key(Key key){
        this.key = key;
        return this;
    }
    public BasicKeyValuePair<Key, Value> value(Value value){
        this.value = value;
        return this;
    }
    public static<Key, Value> BasicKeyValuePair<Key, Value> of(Key key, Value value){
        return new BasicKeyValuePair<>(key, value);
    }

    @SafeVarargs
    @SuppressWarnings({"unchecked"})
    public static<Key, Value> BasicKeyValuePair<Key, Value>[] parseArray(BasicKeyValuePair<Key, Value>... pairs){
        int length = pairs.length;
        BasicKeyValuePair<Key, Value>[] array = (BasicKeyValuePair<Key, Value>[]) Array.newInstance(BasicKeyValuePair.class, length);
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
