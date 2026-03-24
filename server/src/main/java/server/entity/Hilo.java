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


    public Integer  getId()          { return id; }
    public String   getTitle()       { return title; }
    public String   getDescription() { return description; }
    public User     getOwner()       { return owner; }
    public Comunidad getComunidad()  { return comunidad; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setOwner(User owner)               { this.owner = owner; }
    public void setComunidad(Comunidad comunidad)  { this.comunidad = comunidad; }
}
