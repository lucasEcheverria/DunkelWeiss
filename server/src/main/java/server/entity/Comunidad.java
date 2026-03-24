package server.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "comunidades")
public class Comunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "comunidades")
    private List<User> users;

    @OneToMany(mappedBy = "comunidad", cascade = CascadeType.ALL)
    private List<Hilo> threads;


    public String getNombre() { return this.nombre;}
}
