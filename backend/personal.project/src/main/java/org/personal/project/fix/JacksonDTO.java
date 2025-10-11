package org.personal.project.fix;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
//@NoArgsConstructor
//@AllArgsConstructor
public class JacksonDTO {

    private String body;
}
