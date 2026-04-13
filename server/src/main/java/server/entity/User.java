package server.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String nickname;
    @Column(unique = true, nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_communities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "community_id")
    )
    private List<Community> communities;

    @ManyToMany
    @JoinTable(
            name = "favorite_threads",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "thread_id")
    )
    private List<Thread> favoriteThreads;

    @OneToMany(mappedBy = "owner")
    private List<Thread> threads;

    @OneToMany(mappedBy = "owner")
    private List<Post> posts;

    // No-arg constructor required by JPA
    public User() {}

    // Convenience constructor
    public User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    // Getters and setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Community> getcommunities() {
        if (communities == null) communities = new ArrayList<>();
        return communities;
    }

    public void setcommunities(List<Community> communities) {
        this.communities = communities;
    }

    public List<Thread> getFavoriteThreads() {
        return favoriteThreads;
    }

    public void setFavoriteThreads(List<Thread> favoriteThreads) {
        this.favoriteThreads = favoriteThreads;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public void setThreads(List<Thread> threads) {
        this.threads = threads;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}