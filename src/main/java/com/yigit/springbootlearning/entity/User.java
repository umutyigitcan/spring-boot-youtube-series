package com.yigit.springbootlearning.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false,length = 100)
    private String name;

    @NotBlank
    @Column(nullable = false,length = 100)
    private String surname;

    @NotBlank
    @Column(name="password_hash",nullable = false,length = 255)
    private String passwordHash;

    @NotBlank
    @Email
    @Column(nullable = false,unique = true,length = 254)
    private String email;

    protected User(){

    }

    public User(String name,String surname,String passwordHash, String email){
        this.name=name;
        this.surname=surname;
        this.passwordHash=passwordHash;
        this.email=email;
    }


    public Long getId(){return  id;}

    public String getName(){return name;}
    public void setName(String name){this.name=name;}

    public String getSurname(){return  surname;}
    public void setSurname(String surname){this.surname=surname;}

    public String getPasswordHash(){return passwordHash;}
    public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}










}
