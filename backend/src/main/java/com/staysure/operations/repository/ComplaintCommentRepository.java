package com.staysure.operations.repository;

import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.ComplaintComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintCommentRepository extends JpaRepository<ComplaintComment, Long> {

    List<ComplaintComment> findAllByComplaintOrderByCreatedAtAsc(Complaint complaint);
}
