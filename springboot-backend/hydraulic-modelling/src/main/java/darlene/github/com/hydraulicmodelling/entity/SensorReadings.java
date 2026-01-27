package darlene.github.com.hydraulicmodelling;

// This will hold time series data for each sensor and it is best for anomaly detection
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
@Table(name = "sensor_readings")


public class SensorReadings {
}