package com.example.backend.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String githubLink;   // 깃허브 링크
    private String developmentField; // 개발 분야
    private String developmentTool;  // 개발 도구

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
