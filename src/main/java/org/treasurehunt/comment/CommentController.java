package org.treasurehunt.comment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.treasurehunt.hunt.service.HuntService;

@RestController
@RequestMapping("comments")
public class CommentController {

    private final HuntService huntService;

    public CommentController(HuntService huntService) {
        this.huntService = huntService;
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        huntService.deleteComment(commentId);

        return ResponseEntity.ok(null);
    }
}
