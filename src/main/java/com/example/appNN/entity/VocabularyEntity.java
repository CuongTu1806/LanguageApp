package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vocabulary")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class VocabularyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word")
    private String word;

    @Column(name = "pronunciation")
    private String pronunciation;

    @Column(name = "level")
    private String level;

    @Column(name = "pos")
    private String pos;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "meaning_en", length = 500)
    private String meaningEn;

    @Column(name = "example_src")
    private String exampleSrc;

    @Column(name = "example_vi")
    private String exampleVi;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name ="lesson_no")
    private Integer lessonNo;

}
