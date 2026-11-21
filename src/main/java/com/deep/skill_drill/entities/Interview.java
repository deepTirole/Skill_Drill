package com.deep.skill_drill.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


@Entity
@Table(name = "interview_session")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobRole;
    private String status;
    private String finalScore;
    private Integer ratingAfter;
    private Integer eloChange;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startTime;

    @Column(updatable = false)
    private LocalDateTime completionTime;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<QaLog> logs = new ArrayList<>();
}
