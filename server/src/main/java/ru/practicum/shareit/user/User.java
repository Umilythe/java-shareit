package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", length = 512, nullable = false, unique = true)
    private String email;
}
