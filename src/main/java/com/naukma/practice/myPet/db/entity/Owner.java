package com.naukma.practice.myPet.db.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="owner")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String login;
    private String name;
    private String surname;
    private String phone;



}
