package com.education.gptask.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    @Transient
    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users;
    @Override
    public String getAuthority() {
        return getStatus();
    }
}
