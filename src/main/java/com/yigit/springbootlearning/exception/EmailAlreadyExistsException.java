package com.yigit.springbootlearning.exception;

public class EmailAlreadyExistsException extends RuntimeException {


    public EmailAlreadyExistsException(){super("Bu e-posta zaten kullanılıyor");}

}
