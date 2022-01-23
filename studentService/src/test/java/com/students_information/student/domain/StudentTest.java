package com.students_information.student.domain;

import java.util.ArrayList;
import java.util.List;

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
        UniAssertSubscriber<Boolean> booleanSubscriber = null;
        UniAssertSubscriber<Long> longSubscriber = null;
        UniAssertSubscriber<PanacheEntityBase> entitySubscriber = null;
        UniAssertSubscriber<List<Student>> listSubscriber = null;

        longSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        longSubscriber.assertCompleted().assertItem(0L);

        // Now let's specify the return value
        Mockito.when(Student.count()).thenReturn(Uni.createFrom().item(23L));
        longSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        longSubscriber.assertCompleted().assertItem(23L);

        // Now let's change the return value
        Mockito.when(Student.count()).thenReturn(Uni.createFrom().item(42L));
        longSubscriber = Student.count().subscribe().withSubscriber(UniAssertSubscriber.create());
        longSubscriber.assertCompleted().assertItem(42L);

        Student student1 = new Student();
        student1.setSchoolId("schoolId");
        student1.setStudentId("studentId1");
        student1.setName("name1");
        student1.setAge(1);
        student1.setGender("M");
        student1.setCreateId("createId");
        student1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

        Student student2 = new Student();
        student2.setSchoolId("schoolId");
        student2.setStudentId("studentId2");
        student2.setName("name2");
        student2.setAge(1);
        student2.setGender("M");
        student2.setCreateId("createId");
        student2.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

        // TEST CREATE 1
        entitySubscriber = student1.persist().subscribe().withSubscriber(UniAssertSubscriber.create());
        entitySubscriber.assertCompleted().assertItem(student1);

        // TEST CREATE 2
        entitySubscriber = student2.persist().subscribe().withSubscriber(UniAssertSubscriber.create());
        entitySubscriber.assertCompleted().assertItem(student2);

        // TEST READ
        Mockito.when(Student.findById(student1.getPK())).thenReturn(Uni.createFrom().item(student1));
        entitySubscriber = Student.findById(student1.getPK()).subscribe().withSubscriber(UniAssertSubscriber.create());
        entitySubscriber.assertCompleted().assertItem(student1);

        // TEST READ UNKNOWN
        entitySubscriber = Student.findById(42L).subscribe().withSubscriber(UniAssertSubscriber.create());
        entitySubscriber.assertCompleted().assertItem(null);
        // entity = (Student)entitySubscriber.assertCompleted().getItem();
        // Assertions.assertNull(entity);

        // TEST UPDATE
        student1.setName("student11");
        student1.persist();
        entitySubscriber = student1.persist().subscribe().withSubscriber(UniAssertSubscriber.create());
        entitySubscriber.assertCompleted().assertItem(student1);

        List<Student> studentList = new ArrayList<Student>();
        studentList.add(student1);
        studentList.add(student2);

        // TEST FINDER BY TENANT
        Mockito.when(Student.findBySchoolId("schoolId")).thenReturn(Uni.createFrom().item(studentList));
        listSubscriber = Student.findBySchoolId("schoolId").subscribe().withSubscriber(UniAssertSubscriber.create());
        listSubscriber.assertCompleted().assertItem(studentList);

        // MOCK THROWING
        Mockito.when(Student.findById(12L)).thenThrow(new RuntimeException("Boom"));
        Assertions.assertThrows(RuntimeException.class, () -> Student.findById(12L));
        try {
            Student.findById(12L);
            Assertions.fail();
        }//try
        catch (RuntimeException e) {
            Assertions.assertEquals("Boom", e.getMessage());
        }//catch

        // TEST DELETE
        Mockito.when(Student.deleteById(student1.getPK())).thenReturn(Uni.createFrom().item(true));
        booleanSubscriber = Student.deleteById(student1.getPK()).subscribe().withSubscriber(UniAssertSubscriber.create());
        booleanSubscriber.assertCompleted().assertItem(true);

        //TEST DELETE BY TENANT
        Mockito.when(Student.deleteBySchoolId(student2.getSchoolId())).thenReturn(Uni.createFrom().item(1L));
        longSubscriber = Student.deleteBySchoolId(student2.getSchoolId()).subscribe().withSubscriber(UniAssertSubscriber.create());
        longSubscriber.assertCompleted().assertItem(1L);

        // CHECK ACTIVITES
        PanacheMock.verify(Student.class, Mockito.times(3)).count();
        PanacheMock.verify(Student.class, Mockito.atLeastOnce()).deleteById(Mockito.any());
        PanacheMock.verify(Student.class, Mockito.atLeastOnce()).deleteBySchoolId(Mockito.any());
        PanacheMock.verify(Student.class, Mockito.atLeastOnce()).findById(Mockito.any());
        PanacheMock.verify(Student.class, Mockito.atLeastOnce()).findBySchoolId(Mockito.any());
        PanacheMock.verifyNoMoreInteractions(Student.class);
    }
}
