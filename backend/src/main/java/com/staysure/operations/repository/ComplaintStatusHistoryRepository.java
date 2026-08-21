package com.staysure.operations.repository;

import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {

    List<ComplaintStatusHistory> findAllByComplaintOrderByCreatedAtAsc(Complaint complaint);
}
