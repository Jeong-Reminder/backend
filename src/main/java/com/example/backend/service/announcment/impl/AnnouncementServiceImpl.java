package com.example.backend.service.announcment.impl;

import com.example.backend.dto.announcement.AnnouncementRequestDTO;
import com.example.backend.dto.announcement.AnnouncementResponseDTO;
import com.example.backend.dto.announcement.AnnouncementCategory;
import com.example.backend.dto.vote.VoteRequestDTO;
import com.example.backend.model.entity.announcement.Announcement;
import com.example.backend.model.entity.comment.Comment;
import com.example.backend.model.entity.member.Member;
import com.example.backend.model.entity.member.UserRole;
import com.example.backend.model.entity.vote.Vote;
import com.example.backend.model.repository.announcement.AnnouncementRepository;
import com.example.backend.model.repository.member.MemberRepository;
import com.example.backend.service.announcment.AnnouncementService;
import com.example.backend.service.announcment.FileService;
import com.example.backend.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final VoteService voteService;

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getAllAnnouncements(Authentication authentication) {
        return announcementRepository.findAll().stream()
                .map(AnnouncementResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponseDTO getAnnouncementById(Authentication authentication, Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));
        return AnnouncementResponseDTO.toResponseDTO(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getAnnouncementsByCategory(Authentication authentication, AnnouncementCategory category) {
        return announcementRepository.findByAnnouncementCategory(category).stream()
                .map(AnnouncementResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnouncementResponseDTO createAnnouncement(Authentication authentication, AnnouncementRequestDTO announcementRequestDTO) throws IOException {
        String studentId = authentication.getName();
        Member member = memberRepository.findByStudentId(studentId);
        Long managerId = member.getId();
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + managerId));
        if (manager.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 공지사항을 생성할 수 있습니다.");
        }

        List<String> imgPaths = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        try {
            if (announcementRequestDTO.getImg() != null && !announcementRequestDTO.getImg().isEmpty()) {
                imgPaths = saveFiles(announcementRequestDTO.getImg());
            }
            if (announcementRequestDTO.getFile() != null && !announcementRequestDTO.getFile().isEmpty()) {
                filePaths = saveFiles(announcementRequestDTO.getFile());
            }
        } catch (NoSuchFileException e) {
            throw new IOException("파일 저장에 실패했습니다. 파일을 찾을 수 없습니다: " + e.getMessage(), e);
        }

        VoteRequestDTO voteRequestDTO = announcementRequestDTO.getVoteRequest();
        Vote vote = null;
        if (voteRequestDTO != null) {
            vote = voteService.createVote(authentication, voteRequestDTO);
        }

        Announcement announcement = announcementRequestDTO.toEntity(manager, imgPaths, filePaths, vote);
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        return AnnouncementResponseDTO.toResponseDTO(savedAnnouncement);
    }

    @Override
    @Transactional
    public AnnouncementResponseDTO updateAnnouncement(Authentication authentication, Long id, AnnouncementRequestDTO announcementRequestDTO) throws IOException {
        String studentId = authentication.getName();
        Member member = memberRepository.findByStudentId(studentId);
        Long managerId = member.getId();
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + managerId));
        if (manager.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 공지사항을 업데이트 할 수 있습니다.");
        }
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));

        List<String> imgPaths = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        try {
            if (announcementRequestDTO.getImg() != null && !announcementRequestDTO.getImg().isEmpty()) {
                imgPaths = saveFiles(announcementRequestDTO.getImg());
            }
            if (announcementRequestDTO.getFile() != null && !announcementRequestDTO.getFile().isEmpty()) {
                filePaths = saveFiles(announcementRequestDTO.getFile());
            }
        } catch (NoSuchFileException e) {
            throw new IOException("파일 저장에 실패했습니다. 파일을 찾을 수 없습니다: " + e.getMessage(), e);
        }

        VoteRequestDTO voteRequestDTO = announcementRequestDTO.getVoteRequest();
        Vote vote = null;
        if (voteRequestDTO != null) {
            vote = voteService.createVote((Authentication) existingAnnouncement.getManager(), voteRequestDTO);
        }

        existingAnnouncement.update(announcementRequestDTO, imgPaths, filePaths, vote);
        Announcement updatedAnnouncement = announcementRepository.save(existingAnnouncement);

        return AnnouncementResponseDTO.toResponseDTO(updatedAnnouncement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Authentication authentication, Long id) {
        String studentId = authentication.getName();
        Member member = memberRepository.findByStudentId(studentId);
        Long managerId = member.getId();
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + managerId));
        if (manager.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 공지사항을 생성할 수 있습니다.");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));
        announcementRepository.delete(announcement);
    }

    @Override
    @Transactional
    public void hideAnnouncement(Authentication authentication, Long id) {
        String studentId = authentication.getName();
        Member member = memberRepository.findByStudentId(studentId);
        Long managerId = member.getId();
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + managerId));
        if (manager.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 공지사항을 숨길 수 있습니다.");
        }
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));
        announcement.setVisible(false);
        announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public void showAnnouncement(Authentication authentication, Long id) {
        String studentId = authentication.getName();
        Member member = memberRepository.findByStudentId(studentId);
        Long managerId = member.getId();
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + managerId));
        if (manager.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 공지사항을 보이게 할 수 있습니다.");
        }
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));
        announcement.setVisible(true);
        announcementRepository.save(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponseDTO getAnnouncementWithComments(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 공지사항을 찾을 수 없습니다."));

        List<Comment> comments = announcement.getComments() != null ? announcement.getComments() : new ArrayList<>();
        List<Vote> votes = announcement.getVotes() != null ? announcement.getVotes() : new ArrayList<>();

        return AnnouncementResponseDTO.toResponseDTO(announcement);
    }

    // 파일 저장 로직
    private List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadDir = "/tmp/tomcat.9000.5359272715400011388/work/Tomcat/localhost/ROOT/uploads/";

            File uploadFile = new File(uploadDir, fileName);
            if (!uploadFile.getParentFile().exists()) {
                uploadFile.getParentFile().mkdirs();
            }
            file.transferTo(uploadFile);
            filePaths.add(uploadFile.getAbsolutePath());
        }
        return filePaths;
    }
}
