package com.makemytrip.makemytrip.repositories;

import com.makemytrip.makemytrip.models.ReviewHelpfulVote;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewHelpfulVoteRepository extends MongoRepository<ReviewHelpfulVote,String> {
    boolean existsByReviewIdAndUserId(String reviewId,String userId);
}
