package iwaproject.user_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Long totalActiveUsers;
    private Long totalDeletedUsers;
    private Long usersLastMonth;
}
