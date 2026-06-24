package com.deep.skill_drill.dto;

import java.time.LocalDateTime;

public class RatingPointDto {
    private LocalDateTime date;
    private Integer ratingAfter;

    public RatingPointDto(LocalDateTime date, Integer ratingAfter) {
        this.date = date;
        this.ratingAfter = ratingAfter;
    }

    public RatingPointDto() {

    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getRatingAfter() {
        return ratingAfter;
    }

    public void setRatingAfter(Integer ratingAfter) {
        this.ratingAfter = ratingAfter;
    }
}
