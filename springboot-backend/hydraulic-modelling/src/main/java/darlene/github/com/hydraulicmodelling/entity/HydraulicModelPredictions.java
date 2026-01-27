package darlene.github.com.hydraulicmodelling;

import jakarta.persistence*;
import lombok data;
import lombok.AllArgsConstructor;
import lombok.NoArgsContstructor;
import org.hibernate.annotations.CreationTimeStamp;

// This file is meant to store thoeritical/predicted values from the hydraulic model

import java.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hydraulic_model_predictions ")


public class HydraulicModelPredictions {
}