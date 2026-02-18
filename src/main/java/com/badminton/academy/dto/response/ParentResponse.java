package com.badminton.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParentResponse extends UserResponse {

    private String parentPhoneNumber;
    private Set<Long> childrenIds;
    private Integer totalChildren;
}
