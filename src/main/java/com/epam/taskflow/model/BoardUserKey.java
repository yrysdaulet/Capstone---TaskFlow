
package com.epam.taskflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class BoardUserKey implements java.io.Serializable {
    private Long user;
    private Long board;
}