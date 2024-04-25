package acs.upb.web.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "comments")
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    private String content;

    private Timestamp createdAt;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;
}
