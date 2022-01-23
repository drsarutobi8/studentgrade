package com.students_information.student.domain;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.smallrye.mutiny.Uni;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class StudentTest {

    @Test
    public void testPanacheMocking() {
        PanacheMock.mock(Student.class);

        // Mocked classes always return a default value
        UniAssertSubscriber<Long> countSubscriber = null;
        
        countSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        countSubscriber.assertCompleted().assertItem(0L);

        // Now let's specify the return value
        Mockito.when(Student.count()).thenReturn(Uni.createFrom().item(23L));
        countSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        countSubscriber.assertCompleted().assertItem(23L);

        // Now let's change the return value
        Mockito.when(Student.count()).thenReturn(Uni.createFrom().item(42L));
        countSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        countSubscriber.assertCompleted().assertItem(42L);

        // Mock only with specific parameters
        UniAssertSubscriber<PanacheEntityBase> finderSubscriber = null;
        UniAssertSubscriber<PanacheEntityBase> persistSubscriber = null;
        Student result;

        Student student = new Student();
        student.setSchoolId("schoolId");
        student.setStudentId("studentId");
        student.setName("name");
        student.setAge(1);
        student.setGender("M");
        student.setCreateId("createId");
        student.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

        persistSubscriber = student.persist().subscribe().withSubscriber(UniAssertSubscriber.create());
        result = (Student)persistSubscriber.assertCompleted().getItem();
        Assertions.assertSame(student,result);
 
        StudentPK pk = new StudentPK("schoolId","studentId");
        Mockito.when(Student.findById(pk)).thenReturn(Uni.createFrom().item(student));
        finderSubscriber = Student.findById(pk).subscribe().withSubscriber(UniAssertSubscriber.create());
        result = (Student)finderSubscriber.assertCompleted().getItem();
        Assertions.assertSame(student,result);

        finderSubscriber = Student.findById(42L).subscribe().withSubscriber(UniAssertSubscriber.create());
        result = (Student)finderSubscriber.assertCompleted().getItem();
        Assertions.assertNull(result);

        // Mock throwing
        Mockito.when(Student.findById(12L)).thenThrow(new RuntimeException("Boom"));
        Assertions.assertThrows(RuntimeException.class, () -> Student.findById(12L));
        try {
            Student.findById(12L);
            Assertions.fail();
        }//try
        catch (RuntimeException e) {
            Assertions.assertEquals("Boom", e.getMessage());
        }//catch

        // Check that we called it 3 times
        PanacheMock.verify(Student.class, Mockito.times(3)).count();
        PanacheMock.verify(Student.class, Mockito.atLeastOnce()).findById(Mockito.any());
        PanacheMock.verifyNoMoreInteractions(Student.class);
    }
}
