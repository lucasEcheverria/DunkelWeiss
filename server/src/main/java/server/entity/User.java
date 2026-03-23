package server.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String nickname;
    @Column(unique = true, nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_comunidades",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "comunidad_id")
    )
    private List<Comunidad> comunidades;

    @OneToMany(mappedBy = "owner")
    private List<Hilo> threads;

    @OneToMany(mappedBy = "owner")
    private List<Post> posts;

    public String getNickname(){return this.nickname;
    }
}
