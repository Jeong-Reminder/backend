package com.example.backend.model.entity;

import com.example.backend.dto.JoinRequestDTO;
import com.example.backend.model.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String studentId; //로그인 아이디 = 학번
    private String password; //비밀번호
    private String name; //이름
    private Integer level; //학년
    private String status; //학적상태

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Profile profile;  // 프로필 정보

    public JoinRequestDTO toDTO() {
        return JoinRequestDTO.builder()
                .studentId(this.studentId)
                .name(this.name)
                .level(this.level)
                .status(this.status)
                .userRole(this.userRole)
                .build();
    }
}
