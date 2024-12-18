package review.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitDiff {
    private CommitInfo commitInfo;
    private String diffInfo;
}
