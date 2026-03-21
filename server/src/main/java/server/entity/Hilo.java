package server.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "hilos")
public class Hilo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "comunidad_id")
    private Comunidad comunidad;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<Post> posts;
}
