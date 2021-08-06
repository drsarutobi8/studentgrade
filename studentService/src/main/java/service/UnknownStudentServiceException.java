package service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnknownStudentServiceException extends Exception{
    private String studentId;

}
