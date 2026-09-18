package com.yigit.springbootlearning.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(){super("Kullanıcı bulunamadı");}

    public UserNotFoundException(Long id ){super("Kullanıcı bulunamadı: id="+id);}

}
