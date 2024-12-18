package review.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfo {
    private String commit;
    private String author;
    private String date;
    private String message;
}
