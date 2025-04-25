package edu.ucsb.cs156.example.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity(name = "RECOMMENDATIONREQUEST")
public class RecommendationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    String requesterEmail;
    String professorEmail;
    String explanation;
    LocalDateTime dateRequested;
    LocalDateTime dateNeeded;
    boolean done;
}


