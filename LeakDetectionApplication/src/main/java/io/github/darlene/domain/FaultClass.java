package.io.github.darlene.LeakDetectionApplication.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Lombok getter annotator thus no need for writing the getters method
@Getter
@RequiredAllArgsConstructor;

public enum FaultClass {
    NORMAL("Pipeline operating within the normal parameters"),
    LEAK("Abrasive leak signature detected in pressure profile"),
    BLOCKAGE("Partial blockage detected - flow restriction present");

    private final String description;

}