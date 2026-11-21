package com.deep.skill_drill.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.w3c.dom.Text;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "qa_logs")
public class QaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "Text")
    private String question;

    @Column(columnDefinition = "Text")
    private String userAnswer;

    @Column(columnDefinition = "Text")
    private String aiFeedback;

    @ManyToOne()
    @JoinColumn(name = "interview_id")
    @JsonIgnore
    private Interview interview;
}
