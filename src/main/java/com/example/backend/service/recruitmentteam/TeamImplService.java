package com.example.backend.service.recruitmentteam;

import com.example.backend.dto.recruitmentteam.TeamRequestDTO;
import com.example.backend.dto.recruitmentteam.TeamResponseDTO;
import com.example.backend.model.entity.member.Member;
import com.example.backend.model.entity.member.Profile;
import com.example.backend.model.entity.recruitmentteam.AcceptMember;
import com.example.backend.model.entity.recruitmentteam.Recruitment;
import com.example.backend.model.entity.recruitmentteam.Team;
import com.example.backend.model.entity.recruitmentteam.TeamMember;
import com.example.backend.model.repository.recruitmentteam.RecruitmentRepository;
import com.example.backend.model.repository.recruitmentteam.TeamMemberRepository;
import com.example.backend.model.repository.recruitmentteam.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.example.backend.model.repository.member.MemberRepository;

@Service
@RequiredArgsConstructor
public class TeamImplService implements TeamService{

    private final MemberRepository memberRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public TeamResponseDTO createTeam(Authentication authentication, TeamRequestDTO teamRequestDTO) {
        Long memberId = Long.valueOf(authentication.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유저정보가 없습니다."));

        Recruitment recruitment = recruitmentRepository.findById(teamRequestDTO.getRecruitmentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 없습니다."));

        if (!member.getId().equals(recruitment.getMember().getId())) {
            throw new IllegalStateException("팀생성 권한이 없습니다.");
        }

        List<AcceptMember> acceptMembers = recruitment.getAcceptMembers();

        Team team = teamRequestDTO.toEntity(recruitment);
        Team saveTeam = teamRepository.save(team);

        List<TeamMember> teamMembers = teamRequestDTO.toTeamMemberEntity(saveTeam, acceptMembers);
        teamMemberRepository.saveAll(teamMembers);

        List<Profile> profiles = new ArrayList<>();
        for(TeamMember teamMember : teamMembers) {
            profiles.add(teamMember.getMember().getProfile());
        }

        return TeamResponseDTO.toResponseDTO(profiles, saveTeam);
    }
}