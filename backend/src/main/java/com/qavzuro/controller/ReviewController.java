package com.qavzuro.controller;

import com.qavzuro.domain.Review;
import com.qavzuro.dto.request.CreateReviewRequest;
import com.qavzuro.dto.request.ModerateReviewRequest;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.ReviewService;
import com.qavzuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> forProduct(@PathVariable String productId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.listForProduct(productId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> create(@PathVariable String productId, @Valid @RequestBody CreateReviewRequest req) {
        String userId = currentUserService.getUserId();
        var user = userService.getById(userId);
        String displayName = user.getFirstName() + " " + user.getLastName().charAt(0) + ".";
        return ResponseEntity.ok(reviewService.create(userId, displayName, productId, req));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAuthority('REVIEW_MODERATE')")
    public ResponseEntity<Page<Review>> pending(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.listPendingModeration(PageRequest.of(page, size)));
    }

    @PatchMapping("/{id}/moderate")
    @PreAuthorize("hasAuthority('REVIEW_MODERATE')")
    public ResponseEntity<Review> moderate(@PathVariable String id, @Valid @RequestBody ModerateReviewRequest req) {
        return ResponseEntity.ok(reviewService.moderate(currentUserService.getUserId(), id, req.getStatus()));
    }
}
