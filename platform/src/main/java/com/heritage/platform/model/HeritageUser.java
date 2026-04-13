package com.heritage.platform.model;

import com.heritage.platform.entity.ContributorStatus;
import com.heritage.platform.entity.Role;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class HeritageUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String avatar;

    @Column
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.VIEWER;

    private boolean accountNonLocked = true;
    private int failedAttempts = 0;
    private LocalDateTime lockTime;

    @Column(name = "contributor_status")
    private ContributorStatus contributorStatus = ContributorStatus.NONE;

    @Column(name = "contributor_reason")
    private String contributorReason;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();




    //pbi4
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}