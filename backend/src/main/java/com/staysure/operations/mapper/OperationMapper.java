package com.staysure.operations.mapper;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.operations.dto.ComplaintCommentResponse;
import com.staysure.operations.dto.ComplaintHistoryResponse;
import com.staysure.operations.dto.ComplaintResponse;
import com.staysure.operations.dto.FoodFeedbackResponse;
import com.staysure.operations.dto.FoodMenuResponse;
import com.staysure.operations.dto.MaintenanceTaskResponse;
import com.staysure.operations.dto.NotificationResponse;
import com.staysure.operations.dto.NoticeResponse;
import com.staysure.operations.dto.VisitorResponse;
import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.ComplaintComment;
import com.staysure.operations.entity.ComplaintStatusHistory;
import com.staysure.operations.entity.FoodFeedback;
import com.staysure.operations.entity.FoodMenu;
import com.staysure.operations.entity.MaintenanceTask;
import com.staysure.operations.entity.Notification;
import com.staysure.operations.entity.Notice;
import com.staysure.operations.entity.VisitorEntry;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OperationMapper {

    public ComplaintResponse toComplaint(Complaint complaint, List<ComplaintComment> comments, List<ComplaintStatusHistory> history) {
        TenantProfile tenant = complaint.getTenantProfile();
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getComplaintNumber(),
                tenant.getId(),
                userName(tenant.getUser()),
                complaint.getProperty().getId(),
                complaint.getProperty().getName(),
                complaint.getRoom() == null ? null : complaint.getRoom().getId(),
                complaint.getRoom() == null ? null : complaint.getRoom().getRoomNumber(),
                complaint.getCategory(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getPriority(),
                complaint.getStatus(),
                complaint.getResolvedAt(),
                complaint.getClosedAt(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt(),
                comments.stream().map(this::toComment).toList(),
                history.stream().map(this::toHistory).toList()
        );
    }

    public ComplaintCommentResponse toComment(ComplaintComment comment) {
        return new ComplaintCommentResponse(
                comment.getId(),
                comment.getAuthorUser().getId(),
                userName(comment.getAuthorUser()),
                comment.getComment(),
                comment.getCreatedAt()
        );
    }

    public ComplaintHistoryResponse toHistory(ComplaintStatusHistory history) {
        return new ComplaintHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getRemarks(),
                history.getChangedBy() == null ? null : history.getChangedBy().getId(),
                history.getCreatedAt()
        );
    }

    public MaintenanceTaskResponse toMaintenance(MaintenanceTask task) {
        return new MaintenanceTaskResponse(
                task.getId(),
                task.getTaskNumber(),
                task.getComplaint() == null ? null : task.getComplaint().getId(),
                task.getComplaint() == null ? null : task.getComplaint().getComplaintNumber(),
                task.getProperty().getId(),
                task.getProperty().getName(),
                task.getRoom() == null ? null : task.getRoom().getId(),
                task.getRoom() == null ? null : task.getRoom().getRoomNumber(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getAssignedToText(),
                task.getScheduledDate(),
                task.getCompletedAt(),
                task.getRemarks(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public NoticeResponse toNotice(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getProperty().getId(),
                notice.getProperty().getName(),
                notice.getTitle(),
                notice.getContent(),
                notice.getNoticeType(),
                notice.getPriority(),
                notice.getStatus(),
                notice.getPublishedAt(),
                notice.getExpiresAt(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    public FoodMenuResponse toFoodMenu(FoodMenu menu) {
        return new FoodMenuResponse(
                menu.getId(),
                menu.getProperty().getId(),
                menu.getProperty().getName(),
                menu.getMenuDate(),
                menu.getMealType(),
                menu.getItems(),
                menu.getNotes(),
                menu.getCreatedAt(),
                menu.getUpdatedAt()
        );
    }

    public FoodFeedbackResponse toFoodFeedback(FoodFeedback feedback) {
        return new FoodFeedbackResponse(
                feedback.getId(),
                feedback.getTenantProfile().getId(),
                userName(feedback.getTenantProfile().getUser()),
                feedback.getProperty().getId(),
                feedback.getProperty().getName(),
                feedback.getMenuDate(),
                feedback.getMealType(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }

    public VisitorResponse toVisitor(VisitorEntry visitor) {
        return new VisitorResponse(
                visitor.getId(),
                visitor.getVisitorNumber(),
                visitor.getTenantProfile().getId(),
                userName(visitor.getTenantProfile().getUser()),
                visitor.getProperty().getId(),
                visitor.getProperty().getName(),
                visitor.getVisitorName(),
                visitor.getVisitorPhone(),
                visitor.getRelationship(),
                visitor.getVisitDate(),
                visitor.getExpectedArrivalTime(),
                visitor.getExpectedDepartureTime(),
                visitor.getActualArrivalTime(),
                visitor.getActualDepartureTime(),
                visitor.getPurpose(),
                visitor.getStatus(),
                visitor.getRejectionReason(),
                visitor.getApprovedAt(),
                visitor.getCreatedAt(),
                visitor.getUpdatedAt()
        );
    }

    public NotificationResponse toNotification(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    private String userName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
