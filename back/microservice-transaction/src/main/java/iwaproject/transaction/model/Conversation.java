package iwaproject.transaction.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "id_client", nullable = false)
    private Integer idClient;

    public Integer getIdClient() {
        return idClient;
    }

    @Column(name = "id_provider", nullable = false)
    private Integer idProvider;

    public Integer getIdProvider() {
        return idProvider;
    }




}