package darlene.github.com.hydraulicmodelling;

import jakarta.persistence*;
import lombok data;
import lombok.AllArgsConstructor;
import lombok.NoArgsContstructor;
import org.hibernate.annotations.CreationTimeStamp;

//This file is meant to store the extracted ML features for each event
import java.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event_features")


public class EventFeatures {
}