package darlene.github.com.hydraulicmodelling;

import jakarta.persistence*;
import lombok data;
import lombok.AllArgsConstructor;
import lombok.NoArgsContstructor;
import org.hibernate.annotations.CreationTimeStamp;

// This file is meant to design the schema used to detect anomalies and faults
import java.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")


public class Events {
}