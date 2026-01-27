package darlene.github.com.hydraulicmodelling;

import jakarta.persistence*;
import lombok data;
import lombok.AllArgsConstructor;
import lombok.NoArgsContstructor;
import org.hibernate.annotations.CreationTimeStamp;


import java.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")


public class Users{
    @Id
    @GeneratedValue(strategy = GeneratedType.AUTO)
    private Long Id;

    @Column(name = "first_name", nullable=false)
    private String firstName;

    @Column(name = "last_name", nullable=false)
    private String lastName;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(name = "user_role", nullable = false)
    private Role userRole;

}