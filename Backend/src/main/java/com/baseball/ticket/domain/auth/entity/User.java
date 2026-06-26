package com.baseball.ticket.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwd;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phonenum;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private int loginFailCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String address;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 로그인 실패 횟수 증가
    public void increaseLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.isLocked = true;
        }
    }

    // 로그인 성공 시 초기화
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }
}
