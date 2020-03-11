package yay.linda.mydaybackend.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatsDTO {
    private Map<String, List<TileDataDTO>> tiles;
    private Map<String, Number> summary;
}
