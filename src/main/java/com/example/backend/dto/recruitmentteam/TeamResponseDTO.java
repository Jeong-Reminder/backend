package com.example.backend.dto.recruitmentteam;

import com.example.backend.dto.member.TechStackResponseDTO;
import com.example.backend.model.entity.member.Profile;
import com.example.backend.model.entity.recruitmentteam.Team;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamResponseDTO {
    private Long id;
    private String teamName;
    private String teamCategory;
    private List<TechStackResponseDTO> techStacks;

    public static TeamResponseDTO toResponseDTO(List<Profile> profiles, Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .teamName(team.getTeamName())
                .teamCategory(team.getTeamCategory())
                .techStacks(TechStackResponseDTO.toResponseDTOList(profiles))
                .build();
    }
}
