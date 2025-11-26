package com.hk.chart.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data 
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    // PK: 자동 증가 Long 타입 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 이름 (필수, 고유)
    @Column(nullable = false, unique = true)
    private String username;

    // 해시된 비밀번호 (필수)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 실명
    private String name;

    // 이메일 (고유)
    @Column(unique = true)
    private String email;

    // 전화번호 (고유)
    @Column(unique = true)
    private String phone;

    // 생성 시각 (DB에서 처리, Java 코드에서 insert/update 하지 않음)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 업데이트 시각 (DB에서 처리, Java 코드에서 insert/update 하지 않음)
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}