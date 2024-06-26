package com.example.backend.model.repository.recruitmentteam;

import com.example.backend.model.entity.recruitmentteam.ApplicationStatus;
import com.example.backend.model.entity.recruitmentteam.TeamApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    TeamApplication findByMemberIdAndRecruitmentId(Long memberId, Long recruitmentId);

    List<TeamApplication> findByRecruitmentIdAndMemberIdAndApplicationStatus(Long recruitmentId, Long memberId, ApplicationStatus applicationStatus);

    List<TeamApplication> findByRecruitmentIdAndMemberId(Long recruitmentId, Long memberId);

    TeamApplication findByRecruitment_Announcement_IdAndMemberId(Long announcementId, Long memberId);

    TeamApplication findByRecruitment_Announcement_IdAndMemberIdAndApplicationStatus(Long announcementId, Long memberId, ApplicationStatus applicationStatus);
}