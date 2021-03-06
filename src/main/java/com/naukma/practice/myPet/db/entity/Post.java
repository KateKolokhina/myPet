package com.naukma.practice.myPet.db.entity;


import lombok.Data;

import javax.persistence.*;


/**
 * Representation of Post table's field in database
 */
@Entity
@Data
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private Host host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;


    @Column(name="post_status")
//    @Enumerated(EnumType.STRING)
//    private PostStatus status;
    private String status;

    private String description;

    @Column(name="max_days")
    private Integer maxDays;
}
