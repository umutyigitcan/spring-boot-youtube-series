package com.yigit.springbootlearning.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(){super("E-posta veya şifre hatalı");}

    public InvalidCredentialsException(String message){super(message);}

}
