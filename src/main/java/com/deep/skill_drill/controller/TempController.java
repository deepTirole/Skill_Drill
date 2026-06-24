package com.deep.skill_drill.controller;

import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.services.RatingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/temp")
public class TempController {

    @Autowired
    private RatingService ratingService;
    private final ChatClient  chatClient;

    public TempController(@Qualifier("openAiChatClient")  ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/rating")
    public ResponseEntity<Integer> startRating(@RequestParam Integer rating) {

        return ResponseEntity.ok().body(ratingService.dynamicRatingUpdate(rating, 0.75));

    }

    @GetMapping("/get")
    public String get(@RequestParam("q") String q) {
        return chatClient.prompt().user(q).call().content();
    }
}
