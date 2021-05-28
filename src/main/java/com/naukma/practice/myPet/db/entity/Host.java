package com.naukma.practice.myPet.db.entity;

import lombok.Data;

import javax.persistence.*;


@Entity
@Data
@Table(name="host")
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String name;
    private String surname;
    private String adress;
    private Double rating;

    @Column(name="max_animals")
    private Integer maxAnimals;
    private String phone;


    public static Host createHost(String login, String name, String surname, String adress,
                                  Double rating, Integer maxAnimals, String phone) {
        Host host = new Host();
        host.setLogin(login);
        host.setName(name);
        host.setSurname(surname);
        host.setAdress(adress);
        host.setRating(rating);
        host.setMaxAnimals(maxAnimals);
        host.setPhone(phone);
//        user.setAccess("disable");
        return host;
    }

}
