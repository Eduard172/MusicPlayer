package me.eduard.musicplayer.Library.Exceptions;

public class OperationFailedException extends RuntimeException {

    public OperationFailedException(String error){
        super(error);
    }

}
