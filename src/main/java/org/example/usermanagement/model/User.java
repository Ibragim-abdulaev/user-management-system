package org.example.usermanagement.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "reset_token")
    private String resetToken;  // Токен для сброса пароля

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry; // Срок действия токена

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<RoleName> roles = new HashSet<>();  // Роли пользователя

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))  // Преобразуем RoleName в SimpleGrantedAuthority
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;  // Используем email как username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Мы не делаем учетную запись устаревшей
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;  // Учетная запись доступна если активна
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Пароль всегда действителен
    }

    @Override
    public boolean isEnabled() {
        return active;  // Учетная запись активна если active = true
    }

    // Вспомогательные методы
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}